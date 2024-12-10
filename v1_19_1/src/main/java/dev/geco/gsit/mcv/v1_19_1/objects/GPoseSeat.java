package dev.geco.gsit.mcv.v1_19_1.objects;

import java.util.*;

import com.mojang.authlib.*;
import com.mojang.datafixers.util.*;

import org.bukkit.*;
import org.bukkit.block.data.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.craftbukkit.v1_19_R1.*;
import org.bukkit.craftbukkit.v1_19_R1.entity.*;

import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.*;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.*;
import net.minecraft.server.*;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.manager.*;
import dev.geco.gsit.objects.*;

public class GPoseSeat implements IGPoseSeat {

    private final GSitMain GPM = GSitMain.getInstance();

    private final GSeat seat;
    private final Player seatPlayer;
    private final Pose pose;

    private List<Player> nearPlayers = new ArrayList<>();

    private final ServerPlayer serverPlayer;
    protected final ServerPlayer playerNpc;

    private final Location blockLocation;

    private final BlockData bedData;
    private final BlockPos bedPos;

    private final Direction direction;

    protected ClientboundBlockUpdatePacket setBedPacket;
    protected ClientboundPlayerInfoPacket addNpcInfoPacket;
    protected ClientboundPlayerInfoPacket removeNpcInfoPacket;
    protected ClientboundRemoveEntitiesPacket removeNpcPacket;
    protected ClientboundAddPlayerPacket createNpcPacket;
    protected ClientboundSetEntityDataPacket metaNpcPacket;
    protected ClientboundTeleportEntityPacket teleportNpcPacket;
    protected ClientboundMoveEntityPacket.PosRot rotateNpcPacket;

    private NonNullList<net.minecraft.world.item.ItemStack> equipmentSlotCache;
    private net.minecraft.world.item.ItemStack mainSlotCache;
    private float directionCache;
    protected int renderRange;

    private UUID task;

    private final Listener listener;

    public GPoseSeat(GSeat Seat, Pose Pose) {

        seat = Seat;
        seatPlayer = (Player) Seat.getEntity();
        pose = Pose;

        Location seatLocation = seat.getLocation();

        serverPlayer = ((CraftPlayer) seatPlayer).getHandle();

        renderRange = seatPlayer.getWorld().getSimulationDistance() * 16;

        playerNpc = createNPC();
        playerNpc.moveTo(seatLocation.getX(), seatLocation.getY() + (pose == org.bukkit.entity.Pose.SLEEPING ? 0.3125d : pose == org.bukkit.entity.Pose.SPIN_ATTACK ? 0.2d : 0d), seatLocation.getZ(), 0f, 0f);

        blockLocation = seatLocation.clone();
        blockLocation.setY(blockLocation.getWorld().getMinHeight());

        bedData = blockLocation.getBlock().getBlockData();
        bedPos = new BlockPos(blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ());

        direction = getDirection();

        if(pose == org.bukkit.entity.Pose.SLEEPING) setBedPacket = new ClientboundBlockUpdatePacket(bedPos, Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.FACING, direction.getOpposite()).setValue(BedBlock.PART, BedPart.HEAD));
        addNpcInfoPacket = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, playerNpc);
        removeNpcInfoPacket = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, playerNpc);
        removeNpcPacket = new ClientboundRemoveEntitiesPacket(playerNpc.getId());
        createNpcPacket = new ClientboundAddPlayerPacket(playerNpc);
        if(pose == org.bukkit.entity.Pose.SLEEPING) teleportNpcPacket = new ClientboundTeleportEntityPacket(playerNpc);
        if(pose == org.bukkit.entity.Pose.SPIN_ATTACK) rotateNpcPacket = new ClientboundMoveEntityPacket.PosRot(playerNpc.getId(), (short) 0, (short) 0, (short) 0, (byte) 0, getFixedRotation(-90f), true);

        listener = new Listener() {

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void PIntE(PlayerInteractEvent Event) { if(Event.getPlayer() == seatPlayer && !GPM.getCManager().P_INTERACT) Event.setCancelled(true); }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void PIntE(PlayerInteractEntityEvent Event) { if(Event.getPlayer() == seatPlayer) Event.setCancelled(true); }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void EDamBEE(EntityDamageByEntityEvent Event) { if(Event.getDamager() == seatPlayer && !GPM.getCManager().P_INTERACT) Event.setCancelled(true); }

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void EDamE(EntityDamageEvent Event) { if(Event.getEntity() == seatPlayer) playAnimation(1); }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void PLauE(ProjectileLaunchEvent Event) { if(Event.getEntity().getShooter() == seatPlayer && !GPM.getCManager().P_INTERACT) Event.setCancelled(true); }

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void PAniE(PlayerAnimationEvent Event) { if(Event.getPlayer() == seatPlayer && Event.getAnimationType() == PlayerAnimationType.ARM_SWING) playAnimation(Event.getPlayer().getMainHand().equals(MainHand.RIGHT) ? 0 : 3); }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void ICliE(InventoryClickEvent Event) { if(Event.getWhoClicked() == seatPlayer && seatPlayer.getGameMode() == GameMode.CREATIVE) Event.setCancelled(true); }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void PDroIE(PlayerDropItemEvent Event) { if(Event.getPlayer() == seatPlayer && seatPlayer.getGameMode() == GameMode.CREATIVE) Event.setCancelled(true); }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void EPotEE(EntityPotionEffectEvent Event) { if(Event.getEntity() == seatPlayer) serverPlayer.setInvisible(true); }
        };
    }

    @Override
    public void spawn() {

        nearPlayers = getNearPlayers();

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

            if(GPM.getCManager().P_LAY_NIGHT_SKIP) seatPlayer.setSleepingIgnored(true);

            if(GPM.getCManager().P_LAY_REST) seatPlayer.setStatistic(Statistic.TIME_SINCE_REST, 0);
        }

        metaNpcPacket = new ClientboundSetEntityDataPacket(playerNpc.getId(), playerNpc.getEntityData(), false);

        for(Player nearPlayer : nearPlayers) spawnToPlayer(nearPlayer);

        Bukkit.getPluginManager().registerEvents(listener, GPM);

        startUpdate();
    }

    private void spawnToPlayer(Player Player) {

        ServerPlayer spawnPlayer = ((CraftPlayer) Player).getHandle();

        sendPacket(spawnPlayer, addNpcInfoPacket);
        sendPacket(spawnPlayer, createNpcPacket);
        if(pose == Pose.SLEEPING) sendPacket(spawnPlayer, setBedPacket);
        sendPacket(spawnPlayer, metaNpcPacket);
        if(pose == Pose.SLEEPING) sendPacket(spawnPlayer, teleportNpcPacket);
        if(pose == Pose.SPIN_ATTACK) sendPacket(spawnPlayer, rotateNpcPacket);

        GPM.getTManager().runDelayed(() -> {
            sendPacket(spawnPlayer, removeNpcInfoPacket);
        }, seatPlayer, 15);
    }

    @Override
    public void remove() {

        stopUpdate();

        HandlerList.unregisterAll(listener);
        seatPlayer.removeScoreboardTag(PoseManager.POSE_TAG);

        for(Player nearPlayer : nearPlayers) removeToPlayer(nearPlayer);

        if(pose == Pose.SLEEPING && GPM.getCManager().P_LAY_NIGHT_SKIP) seatPlayer.setSleepingIgnored(false);

        serverPlayer.setInvisible(false);

        setEquipmentVisibility(true);

        serverPlayer.getEntityData().set(EntityDataSerializers.COMPOUND_TAG.createAccessor(19), playerNpc.getEntityData().get(EntityDataSerializers.COMPOUND_TAG.createAccessor(19)));
        serverPlayer.getEntityData().set(EntityDataSerializers.COMPOUND_TAG.createAccessor(20), playerNpc.getEntityData().get(EntityDataSerializers.COMPOUND_TAG.createAccessor(20)));

        serverPlayer.setGlowingTag(playerNpc.hasGlowingTag());
    }

    private void removeToPlayer(Player Player) {

        ServerPlayer removePlayer = ((CraftPlayer) Player).getHandle();
        sendPacket(removePlayer, removeNpcInfoPacket);
        sendPacket(removePlayer, removeNpcPacket);

        Player.sendBlockChange(blockLocation, bedData);
    }

    private List<Player> getNearPlayers() {

        List<Player> playerList = new ArrayList<>();
        seatPlayer.getWorld().getPlayers().stream().filter(player -> seat.getLocation().distance(player.getLocation()) <= renderRange && player.canSee(seatPlayer)).forEach(playerList::add);
        return playerList;
    }

    private void startUpdate() {

        task = GPM.getTManager().runAtFixedRate(() -> {

            List<Player> playerList = getNearPlayers();

            for(Player nearPlayer : playerList) {

                if(nearPlayers.contains(nearPlayer)) continue;

                nearPlayers.add(nearPlayer);

                spawnToPlayer(nearPlayer);
            }

            for(Player nearPlayer : new ArrayList<>(nearPlayers)) {

                if(playerList.contains(nearPlayer)) continue;

                nearPlayers.remove(nearPlayer);

                removeToPlayer(nearPlayer);
            }

            if(pose != Pose.SPIN_ATTACK) updateDirection();

            serverPlayer.setInvisible(true);

            updateEquipment();

            setEquipmentVisibility(false);

            updateSkin();

            if(pose != Pose.SLEEPING || !GPM.getCManager().P_LAY_SNORING_SOUNDS) return;

            long tick = serverPlayer.getPlayerTime();

            if((!GPM.getCManager().P_LAY_SNORING_NIGHT_ONLY || (tick >= 12500 && tick <= 23500)) && tick % 90 == 0) {

                for(Player nearPlayer : nearPlayers) nearPlayer.playSound(seat.getLocation(), Sound.ENTITY_FOX_SLEEP, SoundCategory.PLAYERS, 1.5f, 0);
            }
        }, false, 5, 1);
    }

    private void stopUpdate() { GPM.getTManager().cancel(task); }

    private float fixYaw(float Yaw) { return (Yaw < 0f ? 360f + Yaw : Yaw) % 360f; }

    private void updateDirection() {

        if(pose == Pose.SWIMMING) {

            byte fixedRotation = getFixedRotation(seatPlayer.getLocation().getYaw());

            if(directionCache == fixedRotation) return;

            directionCache = fixedRotation;

            ClientboundRotateHeadPacket rotateHeadPacket = new ClientboundRotateHeadPacket(playerNpc, fixedRotation);
            ClientboundMoveEntityPacket.PosRot moveEntityPacket = new ClientboundMoveEntityPacket.PosRot(playerNpc.getId(), (short) 0, (short) 0, (short) 0, fixedRotation, (byte) 0, true);

            for(Player nearPlayer : nearPlayers) {

                ServerPlayer player = ((CraftPlayer) nearPlayer).getHandle();

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

        for(Player nearPlayer : nearPlayers) sendPacket(nearPlayer, rotateHeadPacket);
    }

    private void updateSkin() {

        playerNpc.setInvisible(serverPlayer.activeEffects.containsKey(MobEffects.INVISIBILITY));

        SynchedEntityData entityData = playerNpc.getEntityData();

        entityData.set(EntityDataSerializers.BYTE.createAccessor(17), serverPlayer.getEntityData().get(EntityDataSerializers.BYTE.createAccessor(17)));
        entityData.set(EntityDataSerializers.BYTE.createAccessor(18), serverPlayer.getEntityData().get(EntityDataSerializers.BYTE.createAccessor(18)));

        if(!entityData.isDirty()) return;

        ClientboundSetEntityDataPacket entityDataPacket = new ClientboundSetEntityDataPacket(playerNpc.getId(), entityData, false);

        for(Player nearPlayer : nearPlayers) sendPacket(nearPlayer, entityDataPacket);
    }

    private void updateEquipment() {

        net.minecraft.world.item.ItemStack mainItemStack = serverPlayer.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND);

        if(equipmentSlotCache != null && equipmentSlotCache.equals(serverPlayer.getInventory().getContents()) && mainSlotCache == mainItemStack) return;

        equipmentSlotCache = NonNullList.create();
        equipmentSlotCache.addAll(serverPlayer.getInventory().getContents());
        mainSlotCache = mainItemStack;

        List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> equipmentList = new ArrayList<>();

        for(net.minecraft.world.entity.EquipmentSlot equipmentSlot : net.minecraft.world.entity.EquipmentSlot.values()) {

            net.minecraft.world.item.ItemStack itemStack = serverPlayer.getItemBySlot(equipmentSlot);

            if(itemStack != null) equipmentList.add(Pair.of(equipmentSlot, itemStack));
        }

        ClientboundSetEquipmentPacket setEquipmentPacket = new ClientboundSetEquipmentPacket(playerNpc.getId(), equipmentList);

        for(Player nearPlayer : nearPlayers) sendPacket(nearPlayer, setEquipmentPacket);

        serverPlayer.containerMenu.sendAllDataToRemote();
    }

    private void setEquipmentVisibility(boolean Visibility) {

        List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> equipmentList = new ArrayList<>();

        for(net.minecraft.world.entity.EquipmentSlot equipmentSlot : net.minecraft.world.entity.EquipmentSlot.values()) {

            net.minecraft.world.item.ItemStack itemStack = Visibility ? serverPlayer.getItemBySlot(equipmentSlot) : null;

            equipmentList.add(Pair.of(equipmentSlot, itemStack != null ? itemStack : net.minecraft.world.item.ItemStack.EMPTY));
        }

        ClientboundSetEquipmentPacket setEquipmentPacket = new ClientboundSetEquipmentPacket(serverPlayer.getId(), equipmentList);

        for(Player nearPlayer : nearPlayers) sendPacket(nearPlayer, setEquipmentPacket);
    }

    private void playAnimation(int Arm) {

        ClientboundAnimatePacket animatePacket = new ClientboundAnimatePacket(playerNpc, Arm);

        for(Player nearPlayer : nearPlayers) sendPacket(nearPlayer, animatePacket);
    }

    private byte getFixedRotation(float Yaw) { return (byte) (Yaw * 256f / 360f); }

    private Direction getDirection() {

        float yaw = seat.getLocation().getYaw();

        return (yaw >= 135f || yaw < -135f) ? Direction.NORTH : (yaw >= -135f && yaw < -45f) ? Direction.EAST : (yaw >= -45f && yaw < 45f) ? Direction.SOUTH : yaw >= 45f ? Direction.WEST : Direction.NORTH;
    }

    private ServerPlayer createNPC() {

        MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();

        ServerLevel serverLevel = ((CraftWorld) seat.getLocation().getWorld()).getHandle();

        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), seatPlayer.getName());

        gameProfile.getProperties().putAll(serverPlayer.getGameProfile().getProperties());

        return new ServerPlayer(minecraftServer, serverLevel, gameProfile, null);
    }

    private void sendPacket(Player Player, Packet<?> Packet) { sendPacket(((CraftPlayer) Player).getHandle(), Packet); }

    private void sendPacket(ServerPlayer Player, Packet<?> Packet) { Player.connection.send(Packet); }

    @Override
    public GSeat getSeat() { return seat; }

    @Override
    public Player getPlayer() { return seatPlayer; }

    @Override
    public Pose getPose() { return pose; }

    @Override
    public String toString() { return seat.toString(); }

}