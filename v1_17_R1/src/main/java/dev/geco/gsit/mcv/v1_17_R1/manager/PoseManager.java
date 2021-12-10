package dev.geco.gsit.mcv.v1_17_R1.manager;

import java.util.*;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatMessageType;

import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;
import dev.geco.gsit.mcv.v1_17_R1.objects.*;

public class PoseManager implements IPoseManager, Listener {

    private final GSitMain GPM;

    public PoseManager(GSitMain GPluginMain) {
        GPM = GPluginMain;
        GPM.getServer().getPluginManager().registerEvents(this, GPM);
    }

    private int feature_used = 0;

    public int getFeatureUsedCount() { return feature_used; }

    public void resetFeatureUsedCount() { feature_used = 0; }

    private final HashMap<IGPoseSeat, SeatArmorStand> poses = new HashMap<IGPoseSeat, SeatArmorStand>();

    private final HashMap<IGPoseSeat, BukkitRunnable> detect = new HashMap<IGPoseSeat, BukkitRunnable>();

    private final HashMap<IGPoseSeat, BukkitRunnable> rotate = new HashMap<IGPoseSeat, BukkitRunnable>();

    public boolean isPosing(Player Player) { return getPose(Player) != null; }

    public IGPoseSeat getPose(Player Player) {
        for(IGPoseSeat p : poses.keySet()) if(Player.equals(p.getSeat().getPlayer())) return p;
        return null;
    }

    public void clearPoses() { for(IGPoseSeat p : poses.keySet()) removePose(p, GetUpReason.PLUGIN); }

    public boolean kickPose(Block Block, Player Player) {

        if(GPM.getPoseUtil().isPoseBlock(Block)) {

            if(!GPM.getPManager().hasPermission(Player, "Kick")) return false;

            for(IGPoseSeat p : GPM.getPoseUtil().getPoses(Block)) {

                if(!removePose(p, GetUpReason.KICKED)) return false;

            }

        }

        return true;

    }

    public IGPoseSeat createPose(Block Block, Player Player, Pose Pose) { return createPose(Block, Player, Pose, 0d, GPM.getCManager().L_BLOCK_CENTER ? Block.getBoundingBox().getHeight() : 0d, 0d, Player.getLocation().getYaw(), GPM.getCManager().L_BLOCK_CENTER); }

    public IGPoseSeat createPose(Block Block, Player Player, Pose Pose, double XOffset, double YOffset, double ZOffset, float SeatRotation, boolean SitAtBlock) {

        double o = GPM.getCManager().S_SITMATERIALS.getOrDefault(Block.getType(), 0d);

        ServerPlayer t = ((CraftPlayer) Player).getHandle();

        Location l = Player.getLocation().clone();

        Location r = l.clone();

        if(SitAtBlock) {

            l = Block.getLocation().clone().add(0.5d + XOffset, YOffset + o - 0.2d, 0.5d + ZOffset);

        } else {

            l = l.add(XOffset, YOffset - 0.2d, ZOffset);

        }

        l.setYaw(SeatRotation);

        Level w = ((CraftWorld) l.getWorld()).getHandle();

        SeatArmorStand sa = new SeatArmorStand(w, 0, 0, 0);

        sa.setInvisible(true);
        sa.setSmall(true);
        sa.setNoGravity(true);
        sa.setMarker(true);
        sa.setNoBasePlate(true);
        sa.setInvulnerable(true);
        sa.persist = false;

        sa.setPos(l.getX(), l.getY(), l.getZ());

        sa.setYRot(l.getYaw());
        sa.setXRot(l.getPitch());

        ClientboundAddEntityPacket pa = new ClientboundAddEntityPacket(sa);

        for(Player z : l.getWorld().getPlayers()) {
            ServerPlayer sp = ((CraftPlayer) z).getHandle();
            sp.connection.send(pa);
        }

        ClientboundSetEntityDataPacket pa2 = new ClientboundSetEntityDataPacket(sa.getId(), sa.getEntityData(), true);

        for(Player z : l.getWorld().getPlayers()) {
            ServerPlayer sp = ((CraftPlayer) z).getHandle();
            sp.connection.send(pa2);
        }

        t.absMoveTo(l.getX(), l.getY(), l.getZ());

        t.startRiding(sa, true);

        sa.getBukkitEntity().addPassenger(Player);

        ClientboundSetPassengersPacket pa3 = new ClientboundSetPassengersPacket(sa);

        for(Player z : l.getWorld().getPlayers()) {
            ServerPlayer sp = ((CraftPlayer) z).getHandle();
            sp.connection.send(pa3);
        }

        if(GPM.getCManager().L_SHOW_LAY_MESSAGE) Player.spigot().sendMessage(ChatMessageType.ACTION_BAR, GPM.getMManager().getComplexMessage(GPM.getMManager().getRawMessage("Messages.action-pose-info")));

        GSeat seat = new GSeat(Block, l, Player, sa.getBukkitEntity(), r);

        GPoseSeat pose = new GPoseSeat(seat, Pose);

        pose.spawn();

        sa.getBukkitEntity().setMetadata(GPM.NAME + "P", new FixedMetadataValue(GPM, pose));

        poses.put(pose, sa);

        GPM.getPoseUtil().setPoseBlock(Block, pose);

        startDetectSeat(pose);

        return pose;

    }

    protected void showPose(IGPoseSeat PoseSeat, Player Player) {

        SeatArmorStand sa = poses.get(PoseSeat);

        ServerPlayer sp = ((CraftPlayer) Player).getHandle();

        ClientboundAddEntityPacket pa = new ClientboundAddEntityPacket(sa);

        sp.connection.send(pa);

        ClientboundSetEntityDataPacket pa2 = new ClientboundSetEntityDataPacket(sa.getId(), sa.getEntityData(), true);

        sp.connection.send(pa2);

        ClientboundSetPassengersPacket pa3 = new ClientboundSetPassengersPacket(sa);

        sp.connection.send(pa3);

    }

    protected void startDetectSeat(IGPoseSeat PoseSeat) {

        if(detect.containsKey(PoseSeat)) stopDetectSeat(PoseSeat);

        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {

                if(PoseSeat.getSeat().getPlayer().isSneaking()) {
                    cancel();
                    PoseSeat.getSeat().getPlayer().setSneaking(false);
                    removePose(PoseSeat, GetUpReason.GET_UP);
                }

            }
        };

        r.runTaskTimerAsynchronously(GPM, 0, 1);

        detect.put(PoseSeat, r);

    }

    protected void stopDetectSeat(IGPoseSeat PoseSeat) {

        if(!detect.containsKey(PoseSeat)) return;

        BukkitRunnable r = detect.get(PoseSeat);

        if(r != null && !r.isCancelled()) r.cancel();

        detect.remove(PoseSeat);

    }

    public boolean removePose(IGPoseSeat PoseSeat, GetUpReason Reason) { return removePose(PoseSeat, Reason, true); }

    public boolean removePose(IGPoseSeat PoseSeat, GetUpReason Reason, boolean Safe) {

        GPM.getPoseUtil().removePoseBlock(PoseSeat.getSeat().getBlock(), PoseSeat);

        poses.remove(PoseSeat);

        stopDetectSeat(PoseSeat);

        PoseSeat.remove();

        if(PoseSeat.getSeat().getPlayer().isValid()) {

            ServerPlayer t = ((CraftPlayer) PoseSeat.getSeat().getPlayer()).getHandle();

            t.stopRiding();

            if(Safe) {

                Location l = (GPM.getCManager().S_GET_UP_RETURN ? PoseSeat.getSeat().getReturn() : PoseSeat.getSeat().getLocation()).add(0d, 0.2d, 0d);

                if(!GPM.getCManager().S_GET_UP_RETURN) {
                    l.setYaw(PoseSeat.getSeat().getPlayer().getLocation().getYaw());
                    l.setPitch(PoseSeat.getSeat().getPlayer().getLocation().getPitch());
                }

                GPM.getTeleportUtil().teleport(PoseSeat.getSeat().getPlayer(), l.add(0d, 0.2d, 0d), true);

            }

        }

        ClientboundRemoveEntityPacket pa = new ClientboundRemoveEntityPacket(PoseSeat.getSeat().getEntity().getEntityId());

        for(Player z : PoseSeat.getSeat().getLocation().getWorld().getPlayers()) {
            ServerPlayer sp = ((CraftPlayer) z).getHandle();
            sp.connection.send(pa);
        }

        return true;

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void PJoiE(PlayerJoinEvent e) {

        Player p = e.getPlayer();

        for(Player t : p.getWorld().getPlayers()) {

            if(isPosing(t)) {

                showPose(getPose(t), p);

            }

        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void PChaWE(PlayerChangedWorldEvent e) {

        Player p = e.getPlayer();

        for(Player t : p.getWorld().getPlayers()) {

            if(isPosing(t)) {

                showPose(getPose(t), p);

            }

        }

    }

}