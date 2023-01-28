package dev.geco.gsit.manager;

import java.util.*;
import java.util.stream.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.*;
import dev.geco.gsit.objects.*;
import dev.geco.gsit.util.*;

public class PoseManager {

    private final GSitMain GPM;

    private final boolean available;

    public PoseManager(GSitMain GPluginMain) {
        GPM = GPluginMain;
        available = NMSManager.hasPackageClass("objects.GPoseSeat");
    }

    public boolean isAvailable() { return available; }

    private int feature_used = 0;

    public int getFeatureUsedCount() { return feature_used; }

    public void resetFeatureUsedCount() { feature_used = 0; }

    private final List<IGPoseSeat> poses = new ArrayList<>();

    public List<IGPoseSeat> getPoses() { return new ArrayList<>(poses); }

    public boolean isPosing(Player Player) { return getPose(Player) != null; }

    public IGPoseSeat getPose(Player Player) { return getPoses().stream().filter(pose -> Player.equals(pose.getPlayer())).findFirst().orElse(null); }

    public void clearPoses() { for(IGPoseSeat pose : getPoses()) removePose(pose.getPlayer(), GetUpReason.PLUGIN); }

    public boolean isPoseBlock(Block Block) { return getPoses().stream().anyMatch(pose -> Block.equals(pose.getSeat().getBlock())); }

    public List<IGPoseSeat> getPoses(Block Block) { return getPoses().stream().filter(pose -> Block.equals(pose.getSeat().getBlock())).collect(Collectors.toList()); }

    public List<IGPoseSeat> getPoses(List<Block> Blocks) { return getPoses().stream().filter(pose -> Blocks.contains(pose.getSeat().getBlock())).collect(Collectors.toList()); }

    public boolean kickPose(Block Block, Player Player) {

        if(isPoseBlock(Block)) {

            if(!GPM.getPManager().hasPermission(Player, "Kick.Pose", "Kick.*")) return false;

            for(IGPoseSeat p : getPoses(Block)) if(!removePose(p.getPlayer(), GetUpReason.KICKED)) return false;
        }

        return true;
    }

    public IGPoseSeat createPose(Block Block, Player Player, Pose Pose) { return createPose(Block, Player, Pose, 0d, 0d, 0d, Player.getLocation().getYaw(), GPM.getCManager().CENTER_BLOCK); }

    public IGPoseSeat createPose(Block Block, Player Player, Pose Pose, double XOffset, double YOffset, double ZOffset, float SeatRotation, boolean SitAtBlock) {

        Location playerLocation = Player.getLocation().clone();

        Location returnLocation = playerLocation.clone();

        double offset = SitAtBlock ? Block.getBoundingBox().getMinY() + Block.getBoundingBox().getHeight() : 0d;

        offset = (SitAtBlock ? offset == 0d ? 1d : offset - Block.getY() : offset) + GPM.getCManager().S_SITMATERIALS.getOrDefault(Block.getType(), 0d);

        if(SitAtBlock) {

            playerLocation = Block.getLocation().clone().add(0.5d + XOffset, YOffset + offset - 0.2d, 0.5d + ZOffset);
        } else {

            playerLocation = playerLocation.add(XOffset, YOffset - 0.2d + GPM.getCManager().S_SITMATERIALS.getOrDefault(Block.getType(), 0d), ZOffset);
        }

        if(!GPM.getSpawnUtil().checkLocation(playerLocation)) return null;

        PrePlayerPoseEvent preEvent = new PrePlayerPoseEvent(Player, Block);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return null;

        playerLocation.setYaw(SeatRotation);

        Entity seatEntity = GPM.getSpawnUtil().createSeatEntity(playerLocation, Player, true);

        if(seatEntity == null) return null;

        if(GPM.getCManager().P_POSE_MESSAGE) {

            GPM.getMManager().sendActionBarMessage(Player, "Messages.action-pose-info");

            if(GPM.getCManager().ENHANCED_COMPATIBILITY) new BukkitRunnable() {

                @Override
                public void run() {

                    GPM.getMManager().sendActionBarMessage(Player, "Messages.action-pose-info");
                }
            }.runTaskLater(GPM, 2);
        }

        IGPoseSeat poseSeat = getPoseSeatInstance(new GSeat(Block, playerLocation, Player, seatEntity, returnLocation), Pose);

        poseSeat.spawn();

        poses.add(poseSeat);

        feature_used++;

        Bukkit.getPluginManager().callEvent(new PlayerPoseEvent(poseSeat));

        return poseSeat;
    }

    private IGPoseSeat getPoseSeatInstance(GSeat Seat, Pose Pose) {
        try {
            Class<?> petClass = Class.forName("dev.geco.gsit.mcv." + NMSManager.getPackageVersion() + ".objects.GPoseSeat");
            return (IGPoseSeat) petClass.getConstructor(GSeat.class, Pose.class).newInstance(Seat, Pose);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean removePose(Player Player, GetUpReason Reason) { return removePose(Player, Reason, true); }

    public boolean removePose(Player Player, GetUpReason Reason, boolean Safe) {

        if(!isPosing(Player)) return true;

        IGPoseSeat poseSeat = getPose(Player);

        PrePlayerGetUpPoseEvent preEvent = new PrePlayerGetUpPoseEvent(poseSeat, Reason);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return false;

        poses.remove(poseSeat);

        poseSeat.remove();

        Location returnLocation = (GPM.getCManager().GET_UP_RETURN ? poseSeat.getSeat().getReturn() : poseSeat.getSeat().getLocation().add(0d, 0.2d + (Tag.STAIRS.isTagged(poseSeat.getSeat().getBlock().getType()) ? EnvironmentUtil.STAIR_Y_OFFSET : 0d) - GPM.getCManager().S_SITMATERIALS.getOrDefault(poseSeat.getSeat().getBlock().getType(), 0d), 0d));

        if(!GPM.getCManager().GET_UP_RETURN) {
            returnLocation.setYaw(poseSeat.getPlayer().getLocation().getYaw());
            returnLocation.setPitch(poseSeat.getPlayer().getLocation().getPitch());
        }

        if(poseSeat.getPlayer().isValid() && Safe) {

            GPM.getTeleportUtil().posEntity(poseSeat.getPlayer(), returnLocation);
            GPM.getTeleportUtil().teleportEntity(poseSeat.getPlayer(), returnLocation, true);
        }

        if(poseSeat.getSeat().getSeatEntity().isValid()) poseSeat.getSeat().getSeatEntity().remove();

        Bukkit.getPluginManager().callEvent(new PlayerGetUpPoseEvent(poseSeat, Reason));

        return true;
    }

}