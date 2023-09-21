package dev.geco.gsit.manager;

import java.util.*;
import java.util.stream.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;

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

        Location returnLocation = Player.getLocation().clone();

        Location seatLocation = GPM.getSitManager().getSeatLocation(Block, returnLocation, XOffset, YOffset, ZOffset, SitAtBlock);

        if(!GPM.getEntityUtil().isLocationValid(seatLocation)) return null;

        PrePlayerPoseEvent preEvent = new PrePlayerPoseEvent(Player, Block);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return null;

        seatLocation.setYaw(SeatRotation);

        Entity seatEntity = GPM.getEntityUtil().createSeatEntity(seatLocation, Player, true);

        if(seatEntity == null) return null;

        if(GPM.getCManager().P_POSE_MESSAGE) {

            GPM.getMManager().sendActionBarMessage(Player, "Messages.action-pose-info");

            if(GPM.getCManager().ENHANCED_COMPATIBILITY) {

                GPM.getTManager().runDelayed(() -> {
                    GPM.getMManager().sendActionBarMessage(Player, "Messages.action-pose-info");
                }, Player, 2);
            }
        }

        IGPoseSeat poseSeat = GPM.getEntityUtil().createPoseSeatObject(new GSeat(Block, seatLocation, Player, seatEntity, returnLocation), Pose);

        poseSeat.spawn();

        poses.add(poseSeat);

        feature_used++;

        Bukkit.getPluginManager().callEvent(new PlayerPoseEvent(poseSeat));

        return poseSeat;
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

        Location returnLocation = (GPM.getCManager().GET_UP_RETURN ? poseSeat.getSeat().getReturn() : poseSeat.getSeat().getLocation().add(0d, GPM.getSitManager().BASE_OFFSET + (Tag.STAIRS.isTagged(poseSeat.getSeat().getBlock().getType()) ? EnvironmentUtil.STAIR_Y_OFFSET : 0d) - GPM.getCManager().S_SITMATERIALS.getOrDefault(poseSeat.getSeat().getBlock().getType(), 0d), 0d));

        if(!GPM.getCManager().GET_UP_RETURN) {
            returnLocation.setYaw(poseSeat.getPlayer().getLocation().getYaw());
            returnLocation.setPitch(poseSeat.getPlayer().getLocation().getPitch());
        }

        if(poseSeat.getPlayer().isValid() && Safe) GPM.getEntityUtil().posEntity(poseSeat.getPlayer(), returnLocation);

        poseSeat.getSeat().getSeatEntity().remove();

        Bukkit.getPluginManager().callEvent(new PlayerGetUpPoseEvent(poseSeat, Reason));

        return true;
    }

}