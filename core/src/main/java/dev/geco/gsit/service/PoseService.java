package dev.geco.gsit.service;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.PlayerStopPoseEvent;
import dev.geco.gsit.api.event.PlayerPoseEvent;
import dev.geco.gsit.api.event.PrePlayerStopPoseEvent;
import dev.geco.gsit.api.event.PrePlayerPoseEvent;
import dev.geco.gsit.object.GSeat;
import dev.geco.gsit.object.GStopReason;
import dev.geco.gsit.object.IGPose;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

import java.util.ArrayList;
import java.util.List;

public class PoseService {

    public static final String POSE_TAG = GSitMain.NAME + "_POSE";

    private final GSitMain gSitMain;
    private final boolean available;
    private final List<IGPose> poses = new ArrayList<>();
    private int poseUsageCount = 0;
    private long poseUsageNanoTime = 0;

    public PoseService(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        available = gSitMain.getVersionManager().isNewerOrVersion(18, 0);
    }

    public boolean isAvailable() { return available; }

    public List<IGPose> getAllPoses() { return new ArrayList<>(poses); }

    public boolean isPlayerPosing(Player player) { return getPoseByPlayer(player) != null; }

    public IGPose getPoseByPlayer(Player player) { return poses.stream().filter(pose -> player.equals(pose.getPlayer())).findFirst().orElse(null); }

    public void removeAllPoses() { for(IGPose pose : getAllPoses()) removePose(pose.getPlayer(), GStopReason.PLUGIN); }

    public boolean isBlockWithPose(Block block) { return poses.stream().anyMatch(pose -> block.equals(pose.getSeat().getBlock())); }

    public List<IGPose> getPosesByBlock(Block block) { return poses.stream().filter(pose -> block.equals(pose.getSeat().getBlock())).toList(); }

    public List<IGPose> getPosesByBlocks(List<Block> blocks) { return poses.stream().filter(pose -> blocks.contains(pose.getSeat().getBlock())).toList(); }

    public boolean kickPoseEntitiesFromBlock(Block block, Player player) {
        if(!isBlockWithPose(block)) return true;
        if(!gSitMain.getPermissionService().hasPermission(player, "Kick.Pose", "Kick.*")) return false;
        for(IGPose p : getPosesByBlock(block)) if(!removePose(p.getPlayer(), GStopReason.KICKED)) return false;
        return true;
    }

    public IGPose createPose(Block block, Player player, Pose pose) { return createPose(block, player, pose, 0d, 0d, 0d, player.getLocation().getYaw(), gSitMain.getConfigService().CENTER_BLOCK); }

    public IGPose createPose(Block block, Player player, Pose pose, double xOffset, double yOffset, double zOffset, float seatRotation, boolean sitInBlockCenter) {
        Location returnLocation = player.getLocation();
        Location seatLocation = gSitMain.getSitService().getSeatLocation(block, returnLocation, xOffset, yOffset, zOffset, sitInBlockCenter);
        if(!gSitMain.getEntityUtil().isSitLocationValid(seatLocation)) return null;

        PrePlayerPoseEvent prePlayerPoseEvent = new PrePlayerPoseEvent(player, block);
        Bukkit.getPluginManager().callEvent(prePlayerPoseEvent);
        if(prePlayerPoseEvent.isCancelled()) return null;

        seatLocation.setYaw(seatRotation);
        Entity seatEntity = gSitMain.getEntityUtil().createSeatEntity(seatLocation, player, true);
        if(seatEntity == null) return null;

        if(gSitMain.getConfigService().CUSTOM_MESSAGE) {
            gSitMain.getMessageService().sendActionBarMessage(player, "Messages.action-pose-info");
            if(gSitMain.getConfigService().ENHANCED_COMPATIBILITY) {
                gSitMain.getTaskService().runDelayed(() -> {
                    gSitMain.getMessageService().sendActionBarMessage(player, "Messages.action-pose-info");
                }, player, 2);
            }
        }

        IGPose poseObject = gSitMain.getEntityUtil().createPose(new GSeat(block, seatLocation, player, seatEntity, returnLocation), pose);
        poseObject.spawn();
        poses.add(poseObject);
        poseUsageCount++;
        Bukkit.getPluginManager().callEvent(new PlayerPoseEvent(poseObject));

        return poseObject;
    }

    public boolean removePose(Player player, GStopReason stopReason) { return removePose(player, stopReason, true); }

    public boolean removePose(Player player, GStopReason stopReason, boolean useReturnLocation) {
        IGPose poseObject = getPoseByPlayer(player);
        if(poseObject == null) return true;

        PrePlayerStopPoseEvent prePlayerStopPoseEvent = new PrePlayerStopPoseEvent(poseObject, stopReason);
        Bukkit.getPluginManager().callEvent(prePlayerStopPoseEvent);
        if(prePlayerStopPoseEvent.isCancelled()) return false;

        Location returnLocation = gSitMain.getConfigService().GET_UP_RETURN ? poseObject.getSeat().getReturnLocation() : poseObject.getSeat().getLocation().add(0d, gSitMain.getSitService().getBaseOffset() + (Tag.STAIRS.isTagged(poseObject.getSeat().getBlock().getType()) ? SitService.STAIR_Y_OFFSET : 0d) - gSitMain.getConfigService().S_SITMATERIALS.getOrDefault(poseObject.getSeat().getBlock().getType(), 0d), 0d);
        Location entityLocation = player.getLocation();
        returnLocation.setYaw(entityLocation.getYaw());
        returnLocation.setPitch(entityLocation.getPitch());
        if(player.isValid() && useReturnLocation) gSitMain.getEntityUtil().setEntityLocation(player, returnLocation);

        poses.remove(poseObject);
        poseObject.remove();
        poseObject.getSeat().getSeatEntity().remove();
        Bukkit.getPluginManager().callEvent(new PlayerStopPoseEvent(poseObject, stopReason));
        poseUsageNanoTime += poseObject.getSeat().getLifetimeInNanoSeconds();

        return true;
    }

    public int getPoseUsageCount() { return poseUsageCount; }

    public long getPoseUsageTimeInSeconds() { return poseUsageNanoTime / 1_000_000_000; }

    public void resetPoseUsageStats() {
        poseUsageCount = 0;
        poseUsageNanoTime = 0;
    }

}