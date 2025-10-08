package dev.geco.gsit.mcv.v1_21_9.model;

import com.mojang.datafixers.util.Pair;
import dev.geco.gsit.GSitMain;
import dev.geco.gsit.mcv.v1_21_9.entity.SeatEntity;
import dev.geco.gsit.model.PoseType;
import dev.geco.gsit.model.Seat;
import dev.geco.gsit.service.PoseService;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Statistic;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.MainHand;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

public class Pose implements dev.geco.gsit.model.Pose {

    private final GSitMain gSitMain = GSitMain.getInstance();
    private final Seat seat;
    private final Player seatPlayer;
    private final PoseType poseType;
    private Set<Player> nearbyPlayers = new HashSet<>();
    private final ServerPlayer serverPlayer;
    protected final Mannequin mannequin;
    private OptionalInt leftShoulderCache;
    private OptionalInt rightShoulderCache;
    private final Direction direction;
    protected ClientboundRemoveEntitiesPacket removeNpcPacket;
    protected ClientboundAddEntityPacket createNpcPacket;
    protected ClientboundSetEntityDataPacket metaNpcPacket;
    protected ClientboundUpdateAttributesPacket attributeNpcPacket;
    protected ClientboundBundlePacket bundle;
    private NonNullList<ItemStack> equipmentSlotCache;
    private net.minecraft.world.item.ItemStack mainSlotCache;
    private float directionCache;
    protected int renderRange;
    private final Listener listener;

    public Pose(Seat seat, PoseType poseType) {
        this.seat = seat;
        seatPlayer = (Player) seat.getEntity();
        this.poseType = poseType;

        serverPlayer = ((CraftPlayer) seatPlayer).getHandle();

        renderRange = seatPlayer.getWorld().getSimulationDistance() * 16;

        Location seatLocation = seat.getLocation();

        direction = getDirection();

        mannequin = createMannequin();
        double scale = serverPlayer.getScale();
        double offset = seatLocation.getY() + gSitMain.getSitService().getBaseOffset();
        boolean isLayPoseType = poseType == PoseType.LAY || poseType == PoseType.LAY_BACK;
        if(isLayPoseType) offset += 0.1125d * scale;
        if(poseType == PoseType.BELLYFLOP) offset += -0.19 * scale;
        float sleepYaw = direction == Direction.SOUTH ? 270 : direction == Direction.WEST ? 180 : direction == Direction.EAST ? 0 : 90;
        mannequin.absSnapTo(
            seatLocation.getX() + (isLayPoseType ? (direction == Direction.EAST ? 1.5 : direction == Direction.WEST ? -1.5 : 0) : 0),
            offset,
            seatLocation.getZ() + (isLayPoseType ? (direction == Direction.NORTH ? -1.5 : direction == Direction.SOUTH ? 1.5 : 0) : 0),
            isLayPoseType ? sleepYaw : 0f,
            poseType == PoseType.SPIN ? -90f : 0f
        );

        createNpcPacket = new ClientboundAddEntityPacket(mannequin.getId(), mannequin.getUUID(), mannequin.getX(), mannequin.getY(), mannequin.getZ(), mannequin.getXRot(), mannequin.getYRot(), mannequin.getType(), 0, mannequin.getDeltaMovement(), mannequin.getYHeadRot());
        removeNpcPacket = new ClientboundRemoveEntitiesPacket(mannequin.getId());

        listener = new Listener() {
            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void playerInteractEvent(PlayerInteractEvent Event) { if(Event.getPlayer() == seatPlayer && !gSitMain.getConfigService().P_INTERACT) Event.setCancelled(true); }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void playerInteractEntityEvent(PlayerInteractEntityEvent Event) { if(Event.getPlayer() == seatPlayer) Event.setCancelled(true); }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void entityDamageByEntityEvent(EntityDamageByEntityEvent Event) { if(Event.getDamager() == seatPlayer && !gSitMain.getConfigService().P_INTERACT) Event.setCancelled(true); }

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void entityDamageEvent(EntityDamageEvent Event) { if(Event.getEntity() == seatPlayer) playAnimation(ClientboundAnimatePacket.CRITICAL_HIT); }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void projectileLaunchEvent(ProjectileLaunchEvent Event) { if(Event.getEntity().getShooter() == seatPlayer && !gSitMain.getConfigService().P_INTERACT) Event.setCancelled(true); }

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void playerAnimationEvent(PlayerAnimationEvent Event) { if(Event.getPlayer() == seatPlayer && Event.getAnimationType() == PlayerAnimationType.ARM_SWING) playAnimation(Event.getPlayer().getMainHand().equals(MainHand.RIGHT) ? ClientboundAnimatePacket.SWING_MAIN_HAND : ClientboundAnimatePacket.SWING_OFF_HAND); }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void inventoryClickEvent(InventoryClickEvent Event) { if(Event.getWhoClicked() == seatPlayer && seatPlayer.getGameMode() == GameMode.CREATIVE) Event.setCancelled(true); }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void playerDropItemEvent(PlayerDropItemEvent Event) { if(Event.getPlayer() == seatPlayer && seatPlayer.getGameMode() == GameMode.CREATIVE) Event.setCancelled(true); }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void entityPotionEffectEvent(EntityPotionEffectEvent Event) { if(Event.getEntity() == seatPlayer) serverPlayer.setInvisible(true); }
        };
    }

    @Override
    public void spawn() {
        nearbyPlayers = getNearbyPlayers();

        mannequin.setGlowingTag(serverPlayer.hasGlowingTag());
        if(serverPlayer.hasGlowingTag()) serverPlayer.setGlowingTag(false);

        mannequin.setPose(net.minecraft.world.entity.Pose.values()[poseType.getPlayerPose().ordinal()]);
        if(poseType == PoseType.SPIN) mannequin.getEntityData().set(EntityDataSerializers.BYTE.createAccessor(8), (byte) 4);
        mannequin.getEntityData().set(EntityDataSerializers.BYTE.createAccessor(15), serverPlayer.getEntityData().get(EntityDataSerializers.BYTE.createAccessor(15)));
        mannequin.getEntityData().set(EntityDataSerializers.BYTE.createAccessor(16), serverPlayer.getEntityData().get(EntityDataSerializers.BYTE.createAccessor(16)));
        leftShoulderCache = serverPlayer.getEntityData().get(EntityDataSerializers.OPTIONAL_UNSIGNED_INT.createAccessor(19));
        rightShoulderCache = serverPlayer.getEntityData().get(EntityDataSerializers.OPTIONAL_UNSIGNED_INT.createAccessor(20));
        serverPlayer.getEntityData().set(EntityDataSerializers.OPTIONAL_UNSIGNED_INT.createAccessor(19), OptionalInt.empty());
        serverPlayer.getEntityData().set(EntityDataSerializers.OPTIONAL_UNSIGNED_INT.createAccessor(20), OptionalInt.empty());

        serverPlayer.setInvisible(true);

        setEquipmentVisibility(false);

        if(poseType == PoseType.LAY || poseType == PoseType.LAY_BACK) {
            if(gSitMain.getConfigService().P_LAY_NIGHT_SKIP) seatPlayer.setSleepingIgnored(true);
            if(gSitMain.getConfigService().P_LAY_REST) seatPlayer.setStatistic(Statistic.TIME_SINCE_REST, 0);
        }

        metaNpcPacket = new ClientboundSetEntityDataPacket(mannequin.getId(), mannequin.getEntityData().isDirty() ? mannequin.getEntityData().packDirty() : mannequin.getEntityData().getNonDefaultValues());
        attributeNpcPacket = new ClientboundUpdateAttributesPacket(mannequin.getId(), serverPlayer.getAttributes().getSyncableAttributes());

        List<Packet<? super ClientGamePacketListener>> packages = new ArrayList<>();

        packages.add(createNpcPacket);
        packages.add(metaNpcPacket);
        packages.add(attributeNpcPacket);

        bundle = new ClientboundBundlePacket(packages);

        for(Player nearbyPlayer : nearbyPlayers) addViewerPlayer(nearbyPlayer);

        Bukkit.getPluginManager().registerEvents(listener, gSitMain);

        ((SeatEntity) ((CraftEntity) seat.getSeatEntity()).getHandle()).setRunnable(() -> {
            Set<Player> currentNearbyPlayers = getNearbyPlayers();

            for(Player nearbyPlayer : currentNearbyPlayers) {
                if(nearbyPlayers.contains(nearbyPlayer)) continue;
                nearbyPlayers.add(nearbyPlayer);
                addViewerPlayer(nearbyPlayer);
            }

            for(Player nearbyPlayer : new ArrayList<>(nearbyPlayers)) {
                if(currentNearbyPlayers.contains(nearbyPlayer)) continue;
                nearbyPlayers.remove(nearbyPlayer);
                removeViewerPlayer(nearbyPlayer);
            }

            if(poseType != PoseType.SPIN) updateDirection();

            serverPlayer.setInvisible(true);

            updateEquipment();

            setEquipmentVisibility(false);

            updateSkin();

            if((poseType != PoseType.LAY && poseType != PoseType.LAY_BACK) || !gSitMain.getConfigService().P_LAY_SNORING_SOUNDS) return;

            long tick = serverPlayer.getPlayerTime();

            if((!gSitMain.getConfigService().P_LAY_SNORING_NIGHT_ONLY || (tick >= 12500 && tick <= 23500)) && tick % 90 == 0) {
                for(Player nearbyPlayer : this.nearbyPlayers) nearbyPlayer.playSound(seat.getLocation(), Sound.ENTITY_FOX_SLEEP, SoundCategory.PLAYERS, 1.5f, 0);
            }
        });
    }

    private void addViewerPlayer(Player player) {
        sendPacket(player, bundle);
    }

    @Override
    public void remove() {
        ((SeatEntity) ((CraftEntity) seat.getSeatEntity()).getHandle()).setRunnable(null);

        HandlerList.unregisterAll(listener);
        seatPlayer.removeScoreboardTag(PoseService.POSE_TAG);

        for(Player nearbyPlayer : nearbyPlayers) removeViewerPlayer(nearbyPlayer);

        if((poseType == PoseType.LAY || poseType == PoseType.LAY_BACK) && gSitMain.getConfigService().P_LAY_NIGHT_SKIP) seatPlayer.setSleepingIgnored(false);

        if(!serverPlayer.activeEffects.containsKey(MobEffects.INVISIBILITY)) serverPlayer.setInvisible(false);

        setEquipmentVisibility(true);

        serverPlayer.getEntityData().set(EntityDataSerializers.OPTIONAL_UNSIGNED_INT.createAccessor(19), leftShoulderCache);
        serverPlayer.getEntityData().set(EntityDataSerializers.OPTIONAL_UNSIGNED_INT.createAccessor(20), rightShoulderCache);

        serverPlayer.setGlowingTag(mannequin.hasGlowingTag());
    }

    private void removeViewerPlayer(Player player) {
        ServerPlayer removePlayer = ((CraftPlayer) player).getHandle();
        sendPacket(removePlayer, removeNpcPacket);
    }

    private Set<Player> getNearbyPlayers() {
        Set<Player> currentNearbyPlayers = new HashSet<>();
        seatPlayer.getWorld().getPlayers().stream().filter(player -> seat.getLocation().distanceSquared(player.getLocation()) <= renderRange * renderRange && player.canSee(seatPlayer)).forEach(currentNearbyPlayers::add);
        return currentNearbyPlayers;
    }

    private float fixYaw(float yaw) { return (yaw < 0f ? 360f + yaw : yaw) % 360f; }

    private void updateDirection() {
        if(poseType == PoseType.BELLYFLOP) {
            byte fixedRotation = getFixedRotation(seatPlayer.getLocation().getYaw());
            if(directionCache == fixedRotation) return;
            directionCache = fixedRotation;

            ClientboundRotateHeadPacket rotateHeadPacket = new ClientboundRotateHeadPacket(mannequin, fixedRotation);
            ClientboundMoveEntityPacket.PosRot moveEntityPacket = new ClientboundMoveEntityPacket.PosRot(mannequin.getId(), (short) 0, (short) 0, (short) 0, fixedRotation, (byte) 0, true);

            for(Player nearbyPlayer : nearbyPlayers) {
                ServerPlayer player = ((CraftPlayer) nearbyPlayer).getHandle();
                sendPacket(player, rotateHeadPacket);
                sendPacket(player, moveEntityPacket);
            }

            return;
        }

        float playerYaw = seatPlayer.getLocation().getYaw();
        if(directionCache == playerYaw) return;
        directionCache = playerYaw;

        if(direction == Direction.WEST) playerYaw -= 90;
        if(direction == Direction.EAST) playerYaw += 90;
        if(direction == Direction.NORTH) playerYaw -= 180;

        playerYaw = fixYaw(playerYaw);

        playerYaw = playerYaw >= 315 ? playerYaw - 360 : playerYaw <= 45 ? playerYaw : playerYaw >= 180 ? -45 : 45;

        if(direction == Direction.SOUTH) playerYaw -= 90;
        if(direction == Direction.WEST) playerYaw += 180;
        if(direction == Direction.NORTH) playerYaw += 90;

        ClientboundRotateHeadPacket rotateHeadPacket = new ClientboundRotateHeadPacket(mannequin, getFixedRotation(playerYaw));

        for(Player nearbyPlayer : nearbyPlayers) sendPacket(nearbyPlayer, rotateHeadPacket);
    }

    private void updateSkin() {
        mannequin.setInvisible(serverPlayer.activeEffects.containsKey(MobEffects.INVISIBILITY));

        SynchedEntityData entityData = mannequin.getEntityData();
        mannequin.getEntityData().set(EntityDataSerializers.BYTE.createAccessor(15), serverPlayer.getEntityData().get(EntityDataSerializers.BYTE.createAccessor(15)));
        mannequin.getEntityData().set(EntityDataSerializers.BYTE.createAccessor(16), serverPlayer.getEntityData().get(EntityDataSerializers.BYTE.createAccessor(16)));
        mannequin.setProfile(ResolvableProfile.createResolved(serverPlayer.gameProfile));
        if(!entityData.isDirty()) return;

        ClientboundSetEntityDataPacket entityDataPacket = new ClientboundSetEntityDataPacket(mannequin.getId(), entityData.packDirty());

        for(Player nearbyPlayer : nearbyPlayers) sendPacket(nearbyPlayer, entityDataPacket);
    }

    private void updateEquipment() {
        net.minecraft.world.item.ItemStack mainItemStack = serverPlayer.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND);

        if(equipmentSlotCache != null && equipmentSlotCache.equals(serverPlayer.getInventory().getContents()) && mainSlotCache == mainItemStack) return;

        equipmentSlotCache = NonNullList.create();
        equipmentSlotCache.addAll(serverPlayer.getInventory().getContents());
        mainSlotCache = mainItemStack;

        List<Pair<EquipmentSlot, ItemStack>> equipmentList = new ArrayList<>();

        for(net.minecraft.world.entity.EquipmentSlot equipmentSlot : net.minecraft.world.entity.EquipmentSlot.values()) {
            net.minecraft.world.item.ItemStack itemStack = serverPlayer.getItemBySlot(equipmentSlot);
            equipmentList.add(Pair.of(equipmentSlot, itemStack));
        }

        ClientboundSetEquipmentPacket setEquipmentPacket = new ClientboundSetEquipmentPacket(mannequin.getId(), equipmentList);

        for(Player nearbyPlayer : nearbyPlayers) sendPacket(nearbyPlayer, setEquipmentPacket);

        serverPlayer.containerMenu.sendAllDataToRemote();
    }

    private void setEquipmentVisibility(boolean visibility) {
        List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> equipmentList = new ArrayList<>();

        for(net.minecraft.world.entity.EquipmentSlot equipmentSlot : net.minecraft.world.entity.EquipmentSlot.values()) {
            net.minecraft.world.item.ItemStack itemStack = visibility ? serverPlayer.getItemBySlot(equipmentSlot) : null;
            equipmentList.add(Pair.of(equipmentSlot, itemStack != null ? itemStack : net.minecraft.world.item.ItemStack.EMPTY));
        }

        ClientboundSetEquipmentPacket setEquipmentPacket = new ClientboundSetEquipmentPacket(serverPlayer.getId(), equipmentList);

        for(Player nearbyPlayer : nearbyPlayers) sendPacket(nearbyPlayer, setEquipmentPacket);
    }

    private void playAnimation(int animationId) {
        ClientboundAnimatePacket animatePacket = new ClientboundAnimatePacket(mannequin, animationId);
        for(Player nearbyPlayer : nearbyPlayers) sendPacket(nearbyPlayer, animatePacket);
    }

    private byte getFixedRotation(float rotation) { return (byte) (rotation * 256f / 360f); }

    private Direction getDirection() {
        float yaw = seat.getLocation().getYaw();
        return (yaw >= 135f || yaw < -135f) ? Direction.NORTH : (yaw >= -135f && yaw < -45f) ? Direction.EAST : (yaw >= -45f && yaw < 45f) ? Direction.SOUTH : yaw >= 45f ? Direction.WEST : Direction.NORTH;
    }

    private Mannequin createMannequin() {
        Mannequin playerMannequin = new Mannequin(EntityType.MANNEQUIN, serverPlayer.level());
        playerMannequin.setProfile(ResolvableProfile.createResolved(serverPlayer.gameProfile));
        playerMannequin.setHideDescription(true);
        playerMannequin.setImmovable(true);
        return playerMannequin;
    }

    private void sendPacket(Player player, Packet<?> packet) { sendPacket(((CraftPlayer) player).getHandle(), packet); }

    private void sendPacket(ServerPlayer player, Packet<?> packet) { player.connection.send(packet); }

    @Override
    public Seat getSeat() { return seat; }

    @Override
    public Player getPlayer() { return seatPlayer; }

    @Override
    public PoseType getPoseType() { return poseType; }

    @Override
    public String toString() { return seat.toString(); }

}