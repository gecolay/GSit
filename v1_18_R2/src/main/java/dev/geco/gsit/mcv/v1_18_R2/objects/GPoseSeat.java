package dev.geco.gsit.mcv.v1_18_R2.objects;

import java.util.*;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;

import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.craftbukkit.v1_18_R2.*;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class GPoseSeat implements IGPoseSeat {

    private final GSitMain GPM = GSitMain.getInstance();

    private final GSeat s;

    private final Pose p;

    private Set<Player> a = new HashSet<>();

    private final ServerPlayer cp;

    protected final ServerPlayer f;

    private final Location bl;

    private final BlockData bd;

    private final BlockPos bp;

    private final Direction d;

    protected ClientboundBlockUpdatePacket set_bed;
    protected ClientboundPlayerInfoPacket add_npc;
    protected ClientboundPlayerInfoPacket remove_npc;
    protected ClientboundRemoveEntitiesPacket remove_entity;
    protected ClientboundAddPlayerPacket create_npc;
    protected ClientboundSetEntityDataPacket meta_npc;
    protected ClientboundTeleportEntityPacket set_npc;
    protected ClientboundMoveEntityPacket.PosRot rot_npc;

    private BukkitRunnable r;

    private final Listener li;

    public GPoseSeat(GSeat Seat, Pose Pose) {

        s = Seat;
        p = Pose;

        Location l = s.getLocation();

        cp = ((CraftPlayer) s.getPlayer()).getHandle();

        f = createNPC();
        f.moveTo(l.getX(), l.getY() + (p == org.bukkit.entity.Pose.SLEEPING ? 0.3125d : p == org.bukkit.entity.Pose.SPIN_ATTACK ? 0.2d : 0d), l.getZ(), 0f, 0f);

        bl = l.clone();

        bl.setY(bl.getWorld().getMinHeight());

        bd = bl.getBlock().getBlockData();

        bp = new BlockPos(bl.getBlockX(), bl.getBlockY(), bl.getBlockZ());

        d = getDirection();

        BlockState bs = Blocks.WHITE_BED.defaultBlockState();

        bs = bs.setValue(BedBlock.FACING, d.getOpposite());
        bs = bs.setValue(BedBlock.PART, BedPart.HEAD);

        setMeta();

        if(p == org.bukkit.entity.Pose.SLEEPING) set_bed = new ClientboundBlockUpdatePacket(bp, bs);
        add_npc = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, f);
        remove_npc = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, f);
        remove_entity = new ClientboundRemoveEntitiesPacket(f.getId());
        create_npc = new ClientboundAddPlayerPacket(f);
        meta_npc = new ClientboundSetEntityDataPacket(f.getId(), f.getEntityData(), false);
        if(p == org.bukkit.entity.Pose.SLEEPING) set_npc = new ClientboundTeleportEntityPacket(f);
        if(p == org.bukkit.entity.Pose.SPIN_ATTACK) rot_npc = new ClientboundMoveEntityPacket.PosRot(f.getId(), (short) 0, (short) 0, (short) 0, (byte) 0, getFixedRotation(-90.0f), true);

        li = new Listener() {

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void PIntE(PlayerInteractEvent e) {
                if(e.getPlayer() == s.getPlayer() && !GPM.getCManager().P_INTERACT) {
                    e.setCancelled(true);
                }
            }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void PIntE(PlayerInteractEntityEvent e) {
                if(e.getPlayer() == s.getPlayer()) {
                    e.setCancelled(true);
                }
            }

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void EDamBEE(EntityDamageByEntityEvent e) {
                if(e.getDamager() == s.getPlayer() && !GPM.getCManager().P_INTERACT) {
                    e.setCancelled(true);
                }
            }

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void EDamE(EntityDamageEvent e) {
                if(e.getEntity() == s.getPlayer()) {
                    playAnimation(1);
                }
            }

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void PLauE(ProjectileLaunchEvent e) {
                if(e.getEntity().getShooter() == s.getPlayer() && !GPM.getCManager().P_INTERACT) {
                    e.setCancelled(true);
                }
            }

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void PAniE(PlayerAnimationEvent e) {
                if(e.getPlayer() == s.getPlayer() && e.getAnimationType() == PlayerAnimationType.ARM_SWING) {
                    playAnimation(e.getPlayer().getMainHand().equals(MainHand.RIGHT) ? 0 : 3);
                }
            }

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void PGamMCE(PlayerGameModeChangeEvent e) {
                if(e.getPlayer() == s.getPlayer() && e.getNewGameMode() == GameMode.CREATIVE) {
                    setEquipmentVisibility(true);
                }
            }
        };
    }

    public void spawn() {
        a = getNearPlayers();
        f.setGlowingTag(cp.hasGlowingTag());
        if(cp.hasGlowingTag()) cp.setGlowingTag(false);
        cp.setInvisible(true);
        setEquipmentVisibility(false);
        f.getEntityData().set(EntityDataSerializers.COMPOUND_TAG.createAccessor(19), cp.getEntityData().get(EntityDataSerializers.COMPOUND_TAG.createAccessor(19)));
        f.getEntityData().set(EntityDataSerializers.COMPOUND_TAG.createAccessor(20), cp.getEntityData().get(EntityDataSerializers.COMPOUND_TAG.createAccessor(20)));
        cp.getEntityData().set(EntityDataSerializers.COMPOUND_TAG.createAccessor(19), new CompoundTag());
        cp.getEntityData().set(EntityDataSerializers.COMPOUND_TAG.createAccessor(20), new CompoundTag());
        if(p == Pose.SLEEPING) {
            if(GPM.getCManager().P_LAY_NIGHT_SKIP) s.getPlayer().setSleepingIgnored(true);
            if(GPM.getCManager().P_LAY_REST) s.getPlayer().setStatistic(Statistic.TIME_SINCE_REST, 0);
        }
        for(Player z : a) spawnToPlayer(z);
        Bukkit.getPluginManager().registerEvents(li, GPM);
        startUpdate();
    }

    private void spawnToPlayer(Player z) {
        ServerPlayer sp = ((CraftPlayer) z).getHandle();
        sp.connection.send(add_npc);
        sp.connection.send(create_npc);
        if(p == Pose.SLEEPING) sp.connection.send(set_bed);
        sp.connection.send(meta_npc);
        if(p == Pose.SLEEPING) sp.connection.send(set_npc);
        if(p == Pose.SPIN_ATTACK) sp.connection.send(rot_npc);
        new BukkitRunnable() {
            @Override
            public void run() {
                sp.connection.send(remove_npc);
            }
        }.runTaskLater(GPM, 15);
    }

    public void remove() {
        stopUpdate();
        HandlerList.unregisterAll(li);
        for(Player z : a) removeToPlayer(z);
        if(p == Pose.SLEEPING && GPM.getCManager().P_LAY_NIGHT_SKIP) s.getPlayer().setSleepingIgnored(false);
        cp.setInvisible(false);
        setEquipmentVisibility(true);
        s.getPlayer().setInvisible(false);
        cp.getEntityData().set(EntityDataSerializers.COMPOUND_TAG.createAccessor(19), f.getEntityData().get(EntityDataSerializers.COMPOUND_TAG.createAccessor(19)));
        cp.getEntityData().set(EntityDataSerializers.COMPOUND_TAG.createAccessor(20), f.getEntityData().get(EntityDataSerializers.COMPOUND_TAG.createAccessor(20)));
        cp.setGlowingTag(f.hasGlowingTag());
    }

    private void removeToPlayer(Player z) {
        ServerPlayer sp = ((CraftPlayer) z).getHandle();
        sp.connection.send(remove_npc);
        sp.connection.send(remove_entity);
        z.sendBlockChange(bl, bd);
    }

    private Set<Player> getNearPlayers() {
        HashSet<Player> pl = new HashSet<>();
        s.getLocation().getWorld().getPlayers().stream().filter(o -> s.getLocation().distance(o.getLocation()) <= 250 && o.canSee(s.getPlayer())).forEach(pl::add);
        return pl;
    }

    private void startUpdate() {

        r = new BukkitRunnable() {

            long sleep_tick = 0;

            @Override
            public void run() {

                Set<Player> np = getNearPlayers();

                for(Player z : np) {
                    if(a.contains(z)) continue;
                    a.add(z);
                    spawnToPlayer(z);
                }

                for(Player z : new HashSet<>(a)) {
                    if(np.contains(z)) continue;
                    a.remove(z);
                    removeToPlayer(z);
                }

                if(p != Pose.SPIN_ATTACK) updateDirection();
                cp.setInvisible(true);
                updateEquipment();
                setEquipmentVisibility(false);
                updateSkin();

                if(p == Pose.SLEEPING) {
                    for(Player z : a) {
                        ServerPlayer sp = ((CraftPlayer) z).getHandle();
                        sp.connection.send(set_bed);
                    }
                    if(GPM.getCManager().P_LAY_SNORING_SOUNDS) {
                        sleep_tick++;
                        if(sleep_tick >= 90) {
                            long ti = s.getPlayer().getPlayerTime();
                            if(!GPM.getCManager().P_LAY_SNORING_NIGHT_ONLY || (ti >= 12500 && ti <= 23500)) {
                                for(Player z : a) {
                                    z.playSound(s.getLocation(), Sound.ENTITY_FOX_SLEEP, SoundCategory.PLAYERS, 1.5f, 0);
                                }
                            }
                            sleep_tick = 0;
                        }
                    }
                }
            }
        };

        r.runTaskTimerAsynchronously(GPM, 5, 1);
    }

    private void stopUpdate() {
        if(r != null && !r.isCancelled()) r.cancel();
    }

    private void setMeta() {
        f.getEntityData().set(EntityDataSerializers.POSE.createAccessor(6), net.minecraft.world.entity.Pose.values()[p.ordinal()]);
        f.getEntityData().set(EntityDataSerializers.BYTE.createAccessor(17), cp.getEntityData().get(EntityDataSerializers.BYTE.createAccessor(17)));
        f.getEntityData().set(EntityDataSerializers.BYTE.createAccessor(18), cp.getEntityData().get(EntityDataSerializers.BYTE.createAccessor(18)));
        if(p == Pose.SPIN_ATTACK) f.getEntityData().set(EntityDataSerializers.BYTE.createAccessor(8), (byte) 4);
        if(p == Pose.SLEEPING) f.getEntityData().set(EntityDataSerializers.OPTIONAL_BLOCK_POS.createAccessor(14), Optional.of(bp));
    }

    private float fixYaw(float Y) {
        return (Y < 0.0f ? 360.0f + Y : Y) % 360.0f;
    }

    private void updateDirection() {
        if(p == Pose.SWIMMING) {
            byte y = getFixedRotation(s.getPlayer().getLocation().getYaw());
            ClientboundRotateHeadPacket pa = new ClientboundRotateHeadPacket(f, y);
            ClientboundMoveEntityPacket.PosRot pa2 = new ClientboundMoveEntityPacket.PosRot(f.getId(), (short) 0, (short) 0, (short) 0, y, (byte) 0, true);
            for(Player z : a) {
                ServerPlayer sp = ((CraftPlayer) z).getHandle();
                sp.connection.send(pa);
                sp.connection.send(pa2);
            }
            return;
        }
        float yc = s.getPlayer().getLocation().getYaw();
        if(d == Direction.WEST) yc -= 90;
        if(d == Direction.EAST) yc += 90;
        if(d == Direction.NORTH) yc -= 180;
        yc = fixYaw(yc);
        byte y = getFixedRotation(yc >= 315 ? yc - 360 : yc <= 45 ? yc : yc >= 180 ? -45 : 45);
        ClientboundRotateHeadPacket pa = new ClientboundRotateHeadPacket(f, y);
        for(Player z : a) {
            ServerPlayer sp = ((CraftPlayer) z).getHandle();
            sp.connection.send(pa);
        }
    }

    private void updateSkin() {
        SynchedEntityData sed = f.getEntityData();
        sed.set(EntityDataSerializers.BYTE.createAccessor(17), cp.getEntityData().get(EntityDataSerializers.BYTE.createAccessor(17)));
        sed.set(EntityDataSerializers.BYTE.createAccessor(18), cp.getEntityData().get(EntityDataSerializers.BYTE.createAccessor(18)));
        ClientboundSetEntityDataPacket pa = new ClientboundSetEntityDataPacket(f.getId(), sed, false);
        for(Player z : a) {
            ServerPlayer sp = ((CraftPlayer) z).getHandle();
            sp.connection.send(pa);
        }
    }

    private void updateEquipment() {
        List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> lp = new ArrayList<>();
        for(net.minecraft.world.entity.EquipmentSlot es : net.minecraft.world.entity.EquipmentSlot.values()) {
            net.minecraft.world.item.ItemStack is = cp.getItemBySlot(es);
            if(is != null) lp.add(Pair.of(es, is));
        }
        ClientboundSetEquipmentPacket pa = new ClientboundSetEquipmentPacket(f.getId(), lp);
        for(Player z : a) {
            ServerPlayer sp = ((CraftPlayer) z).getHandle();
            sp.connection.send(pa);
        }
    }

    private void setEquipmentVisibility(boolean v) {
        List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> lp = new ArrayList<>();
        net.minecraft.world.item.ItemStack nis = CraftItemStack.asNMSCopy(new ItemStack(Material.AIR));
        for(net.minecraft.world.entity.EquipmentSlot es : net.minecraft.world.entity.EquipmentSlot.values()) {
            net.minecraft.world.item.ItemStack is = v ? cp.getItemBySlot(es) : null;
            lp.add(Pair.of(es, is != null ? is : nis));
        }
        ClientboundSetEquipmentPacket pa = new ClientboundSetEquipmentPacket(cp.getId(), lp);
        for(Player z : a) {
            if(z == s.getPlayer()) continue;
            ServerPlayer sp = ((CraftPlayer) z).getHandle();
            sp.connection.send(pa);
        }
        if(s.getPlayer().getGameMode() != GameMode.CREATIVE) {
            s.getPlayer().updateInventory();
            if(!v) cp.connection.send(pa);
        }
    }

    private void playAnimation(int A) {
        ClientboundAnimatePacket pa = new ClientboundAnimatePacket(f, A);
        for(Player z : a) {
            ServerPlayer sp = ((CraftPlayer) z).getHandle();
            sp.connection.send(pa);
        }
    }

    private byte getFixedRotation(float Y) {
        return (byte)(Y * 256.0f / 360.0f);
    }

    private Direction getDirection() {
        float y = s.getLocation().getYaw();
        if(y >= 135f || y < -135f) return Direction.NORTH;
        if(y >= -135f && y < -45f) return Direction.EAST;
        if(y >= -45f && y < 45f) return Direction.SOUTH;
        if(y >= 45f) return Direction.WEST;
        return Direction.NORTH;
    }

    private ServerPlayer createNPC() {
        MinecraftServer mcs = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel sl = ((CraftWorld) s.getLocation().getWorld()).getHandle();
        GameProfile pf = new GameProfile(UUID.randomUUID(), GPM.getMManager().toColoredString(s.getPlayer().getName()));
        pf.getProperties().putAll(cp.getGameProfile().getProperties());
        return new ServerPlayer(mcs, sl, pf);
    }

    public GSeat getSeat() { return s; }

    public Pose getPose() { return p; }

    public String toString() { return s.toString(); }

}