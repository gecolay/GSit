package dev.geco.gsit.mcv.v1_20_5.object;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import dev.geco.gsit.GSitMain;
import dev.geco.gsit.object.GSeat;
import dev.geco.gsit.object.IGPose;
import dev.geco.gsit.service.PoseService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
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
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class GPose implements IGPose {

    private final GSitMain gSitMain = GSitMain.getInstance();
    private final GSeat seat;
    private final Player seatPlayer;
    private final Pose pose;
    private Set<Player> nearbyPlayers = new HashSet<>();
    private final ServerPlayer serverPlayer;
    protected final ServerPlayer playerNpc;
    private final Location blockLocation;
    private final Block bedBlock;
    private final BlockPos bedPos;
    private final Direction direction;
    protected ClientboundBlockUpdatePacket setBedPacket;
    protected ClientboundPlayerInfoUpdatePacket addNpcInfoPacket;
    protected ClientboundPlayerInfoRemovePacket removeNpcInfoPacket;
    protected ClientboundRemoveEntitiesPacket removeNpcPacket;
    protected ClientboundAddEntityPacket createNpcPacket;
    protected ClientboundSetEntityDataPacket metaNpcPacket;
    protected ClientboundUpdateAttributesPacket attributeNpcPacket;
    protected ClientboundTeleportEntityPacket teleportNpcPacket;
    protected ClientboundMoveEntityPacket.PosRot rotateNpcPacket;
    protected ClientboundBundlePacket bundle;
    private NonNullList<ItemStack> equipmentSlotCache;
    private net.minecraft.world.item.ItemStack mainSlotCache;
    private float directionCache;
    protected int renderRange;
    private final Listener listener;

    public GPose(GSeat seat, Pose pose) {
        this.seat = seat;
        seatPlayer = (Player) seat.getEntity();
        this.pose = pose;

        serverPlayer = ((CraftPlayer) seatPlayer).getHandle();

        renderRange = seatPlayer.getWorld().getSimulationDistance() * 16;

        Location seatLocation = seat.getLocation();
        blockLocation = seatLocation.clone();
        blockLocation.setY(blockLocation.getWorld().getMinHeight());
        bedBlock = blockLocation.getBlock();
        bedPos = new BlockPos(blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ());

        playerNpc = createNPC();
        double offset = seatLocation.getY() + gSitMain.getSitService().getBaseOffset();
        double scale = serverPlayer.getScale();
        if(pose == org.bukkit.entity.Pose.SLEEPING) offset += 0.1125d * scale;
        playerNpc.moveTo(seatLocation.getX(), offset, seatLocation.getZ(), 0f, 0f);

        direction = getDirection();
        if(pose == org.bukkit.entity.Pose.SLEEPING) setBedPacket = new ClientboundBlockUpdatePacket(bedPos, Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.FACING, direction.getOpposite()).setValue(BedBlock.PART, BedPart.HEAD));
        addNpcInfoPacket = new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME), Collections.singletonList(playerNpc));
        removeNpcInfoPacket = new ClientboundPlayerInfoRemovePacket(Collections.singletonList(playerNpc.getUUID()));
        removeNpcPacket = new ClientboundRemoveEntitiesPacket(playerNpc.getId());
        createNpcPacket = new ClientboundAddEntityPacket(playerNpc);
        if(pose == org.bukkit.entity.Pose.SLEEPING) teleportNpcPacket = new ClientboundTeleportEntityPacket(playerNpc);
        if(pose == org.bukkit.entity.Pose.SPIN_ATTACK) rotateNpcPacket = new ClientboundMoveEntityPacket.PosRot(playerNpc.getId(), (short) 0, (short) 0, (short) 0, (byte) 0, getFixedRotation(-90f), true);

        listener = new Listener() {
            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void playerInteractEvent(PlayerInteractEvent Event) { if(Event.getPlayer() == seatPlayer && !gSitMain.getConfigService().P_INTERACT) Event.setCancelled(true); }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void playerInteractEntityEvent(PlayerInteractEntityEvent Event) { if(Event.getPlayer() == seatPlayer) Event.setCancelled(true); }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void entityDamageByEntityEvent(EntityDamageByEntityEvent Event) { if(Event.getDamager() == seatPlayer && !gSitMain.getConfigService().P_INTERACT) Event.setCancelled(true); }

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void entityDamageEvent(EntityDamageEvent Event) { if(Event.getEntity() == seatPlayer) playAnimation(1); }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void projectileLaunchEvent(ProjectileLaunchEvent Event) { if(Event.getEntity().getShooter() == seatPlayer && !gSitMain.getConfigService().P_INTERACT) Event.setCancelled(true); }

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void playerAnimationEvent(PlayerAnimationEvent Event) { if(Event.getPlayer() == seatPlayer && Event.getAnimationType() == PlayerAnimationType.ARM_SWING) playAnimation(Event.getPlayer().getMainHand().equals(MainHand.RIGHT) ? 0 : 3); }

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

        playerNpc.setGlowingTag(serverPlayer.hasGlowingTag());
        if(serverPlayer.hasGlowingTag()) serverPlayer.setGlowingTag(false);

        playerNpc.getEntityData().set(EntityDataSerializers.POSE.createAccessor(6), net.minecraft.world.entity.Pose.values()[pose.ordinal()]);
        if(pose == Pose.SPIN_ATTACK) playerNpc.getEntityData().set(EntityDataSerializers.BYTE.createAccessor(8), (byte) 4);
        if(pose == Pose.SLEEPING) playerNpc.getEntityData().set(EntityDataSerializers.OPTIONAL_BLOCK_POS.createAccessor(14), Optional.of(bedPos));
        playerNpc.getEntityData().set(EntityDataSerializers.BYTE.createAccessor(17), serverPlayer.getEntityData().get(EntityDataSerializers.BYTE.createAccessor(17)));
        playerNpc.getEntityData().set(EntityDataSerializers.BYTE.createAccessor(18), serverPlayer.getEntityData().get(EntityDataSerializers.BYTE.createAccessor(18)));
        playerNpc.getEntityData().set(EntityDataSerializers.COMPOUND_TAG.createAccessor(19), serverPlayer.getEntityData().get(EntityDataSerializers.COMPOUND_TAG.createAccessor(19)));
        playerNpc.getEntityData().set(EntityDataSerializers.COMPOUND_TAG.createAccessor(20), serverPlayer.getEntityData().get(EntityDataSerializers.COMPOUND_TAG.createAccessor(20)));
        serverPlayer.getEntityData().set(EntityDataSerializers.COMPOUND_TAG.createAccessor(19), new CompoundTag());
        serverPlayer.getEntityData().set(EntityDataSerializers.COMPOUND_TAG.createAccessor(20), new CompoundTag());

        serverPlayer.setInvisible(true);

        setEquipmentVisibility(false);

        if(pose == Pose.SLEEPING) {
            if(gSitMain.getConfigService().P_LAY_NIGHT_SKIP) seatPlayer.setSleepingIgnored(true);
            if(gSitMain.getConfigService().P_LAY_REST) seatPlayer.setStatistic(Statistic.TIME_SINCE_REST, 0);
        }

        metaNpcPacket = new ClientboundSetEntityDataPacket(playerNpc.getId(), playerNpc.getEntityData().isDirty() ? playerNpc.getEntityData().packDirty() : playerNpc.getEntityData().getNonDefaultValues());
        attributeNpcPacket = new ClientboundUpdateAttributesPacket(playerNpc.getId(), serverPlayer.getAttributes().getSyncableAttributes());

        List<Packet<? super ClientGamePacketListener>> packages = new ArrayList<>();

        packages.add(addNpcInfoPacket);
        packages.add(createNpcPacket);
        if(pose == Pose.SLEEPING) packages.add(setBedPacket);
        packages.add(metaNpcPacket);
        packages.add(attributeNpcPacket);
        if(pose == Pose.SLEEPING) packages.add(teleportNpcPacket);
        if(pose == Pose.SPIN_ATTACK) packages.add(rotateNpcPacket);

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

            if(pose != Pose.SPIN_ATTACK) updateDirection();

            serverPlayer.setInvisible(true);

            updateEquipment();

            setEquipmentVisibility(false);

            updateSkin();

            if(pose != Pose.SLEEPING || !gSitMain.getConfigService().P_LAY_SNORING_SOUNDS) return;

            long tick = serverPlayer.getPlayerTime();

            if((!gSitMain.getConfigService().P_LAY_SNORING_NIGHT_ONLY || (tick >= 12500 && tick <= 23500)) && tick % 90 == 0) {
                for(Player nearbyPlayer : this.nearbyPlayers) nearbyPlayer.playSound(seat.getLocation(), Sound.ENTITY_FOX_SLEEP, SoundCategory.PLAYERS, 1.5f, 0);
            }
        });
    }

    private void addViewerPlayer(Player player) { sendPacket(player, bundle); }

    @Override
    public void remove() {
        ((SeatEntity) ((CraftEntity) seat.getSeatEntity()).getHandle()).setRunnable(null);

        HandlerList.unregisterAll(listener);
        seatPlayer.removeScoreboardTag(PoseService.POSE_TAG);

        for(Player nearbyPlayer : nearbyPlayers) removeViewerPlayer(nearbyPlayer);

        if(pose == Pose.SLEEPING && gSitMain.getConfigService().P_LAY_NIGHT_SKIP) seatPlayer.setSleepingIgnored(false);

        if(!serverPlayer.activeEffects.containsKey(MobEffects.INVISIBILITY)) serverPlayer.setInvisible(false);

        setEquipmentVisibility(true);

        serverPlayer.getEntityData().set(EntityDataSerializers.COMPOUND_TAG.createAccessor(19), playerNpc.getEntityData().get(EntityDataSerializers.COMPOUND_TAG.createAccessor(19)));
        serverPlayer.getEntityData().set(EntityDataSerializers.COMPOUND_TAG.createAccessor(20), playerNpc.getEntityData().get(EntityDataSerializers.COMPOUND_TAG.createAccessor(20)));

        serverPlayer.setGlowingTag(playerNpc.hasGlowingTag());
    }

    private void removeViewerPlayer(Player player) {
        ServerPlayer removePlayer = ((CraftPlayer) player).getHandle();
        sendPacket(removePlayer, removeNpcInfoPacket);
        sendPacket(removePlayer, removeNpcPacket);

        player.sendBlockChange(blockLocation, bedBlock.getBlockData());
    }

    private Set<Player> getNearbyPlayers() {
        Set<Player> currentNearbyPlayers = new HashSet<>();
        seatPlayer.getWorld().getPlayers().stream().filter(player -> seat.getLocation().distanceSquared(player.getLocation()) <= renderRange * renderRange && player.canSee(seatPlayer)).forEach(currentNearbyPlayers::add);
        return currentNearbyPlayers;
    }

    private float fixYaw(float yaw) { return (yaw < 0f ? 360f + yaw : yaw) % 360f; }

    private void updateDirection() {
        if(pose == Pose.SWIMMING) {
            byte fixedRotation = getFixedRotation(seatPlayer.getLocation().getYaw());
            if(directionCache == fixedRotation) return;
            directionCache = fixedRotation;

            ClientboundRotateHeadPacket rotateHeadPacket = new ClientboundRotateHeadPacket(playerNpc, fixedRotation);
            ClientboundMoveEntityPacket.PosRot moveEntityPacket = new ClientboundMoveEntityPacket.PosRot(playerNpc.getId(), (short) 0, (short) 0, (short) 0, fixedRotation, (byte) 0, true);

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

        byte fixedRotation = getFixedRotation(playerYaw >= 315 ? playerYaw - 360 : playerYaw <= 45 ? playerYaw : playerYaw >= 180 ? -45 : 45);

        ClientboundRotateHeadPacket rotateHeadPacket = new ClientboundRotateHeadPacket(playerNpc, fixedRotation);

        for(Player nearbyPlayer : nearbyPlayers) sendPacket(nearbyPlayer, rotateHeadPacket);
    }

    private void updateSkin() {
        playerNpc.setInvisible(serverPlayer.activeEffects.containsKey(MobEffects.INVISIBILITY));

        SynchedEntityData entityData = playerNpc.getEntityData();
        entityData.set(EntityDataSerializers.BYTE.createAccessor(17), serverPlayer.getEntityData().get(EntityDataSerializers.BYTE.createAccessor(17)));
        entityData.set(EntityDataSerializers.BYTE.createAccessor(18), serverPlayer.getEntityData().get(EntityDataSerializers.BYTE.createAccessor(18)));
        if(!entityData.isDirty()) return;

        ClientboundSetEntityDataPacket entityDataPacket = new ClientboundSetEntityDataPacket(playerNpc.getId(), entityData.packDirty());

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

        ClientboundSetEquipmentPacket setEquipmentPacket = new ClientboundSetEquipmentPacket(playerNpc.getId(), equipmentList);

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
        ClientboundAnimatePacket animatePacket = new ClientboundAnimatePacket(playerNpc, animationId);
        for(Player nearbyPlayer : nearbyPlayers) sendPacket(nearbyPlayer, animatePacket);
    }

    private byte getFixedRotation(float rotation) { return (byte) (rotation * 256f / 360f); }

    private Direction getDirection() {
        float yaw = seat.getLocation().getYaw();
        return (yaw >= 135f || yaw < -135f) ? Direction.NORTH : (yaw >= -135f && yaw < -45f) ? Direction.EAST : (yaw >= -45f && yaw < 45f) ? Direction.SOUTH : yaw >= 45f ? Direction.WEST : Direction.NORTH;
    }

    private ServerPlayer createNPC() {
        MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel serverLevel = ((CraftWorld) seat.getLocation().getWorld()).getHandle();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), seatPlayer.getName());
        gameProfile.getProperties().putAll(serverPlayer.getGameProfile().getProperties());
        ClientInformation clientInformation = serverPlayer.clientInformation();
        ServerPlayer npc = new ServerPlayer(minecraftServer, serverLevel, gameProfile, clientInformation);
        npc.connection = serverPlayer.connection;
        return npc;
    }

    private void sendPacket(Player player, Packet<?> packet) { sendPacket(((CraftPlayer) player).getHandle(), packet); }

    private void sendPacket(ServerPlayer player, Packet<?> packet) { player.connection.send(packet); }

    @Override
    public GSeat getSeat() { return seat; }

    @Override
    public Player getPlayer() { return seatPlayer; }

    @Override
    public Pose getPose() { return pose; }

    @Override
    public String toString() { return seat.toString(); }

}