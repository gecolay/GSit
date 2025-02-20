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
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PoseService {

    public static final String POSE_TAG = GSitMain.NAME + "_POSE";

    private final GSitMain gSitMain;
    private final boolean available;
    private final HashMap<UUID, IGPose> poses = new HashMap<>();
    private final HashMap<Block, Set<IGPose>> blockPoses = new HashMap<>();
    private final HashMap<Block, Material> blockTypes = new HashMap<>();
    private int poseUsageCount = 0;
    private long poseUsageNanoTime = 0;

    public PoseService(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        available = gSitMain.getVersionManager().isNewerOrVersion(18, 0);
    }

    public boolean isAvailable() { return available; }

    public HashMap<UUID, IGPose> getAllPoses() { return poses; }

    public boolean isPlayerPosing(Player player) { return poses.containsKey(player.getUniqueId()); }

    public IGPose getPoseByPlayer(Player player) { return poses.get(player.getUniqueId()); }

    public void removeAllPoses() { for(IGPose pose : new ArrayList<>(poses.values())) removePose(pose, GStopReason.PLUGIN); }

    public boolean isBlockWithPose(Block block) { return blockPoses.containsKey(block); }

    public Set<IGPose> getPosesByBlock(Block block) { return blockPoses.getOrDefault(block, Collections.emptySet()); }

    public Material getPoseBlockMaterial(Block block) { return blockTypes.get(block); }

    public boolean kickPoseEntitiesFromBlock(Block block, Player player) {
        if(!isBlockWithPose(block)) return true;
        if(!gSitMain.getPermissionService().hasPermission(player, "Kick.Pose", "Kick.*")) return false;
        for(IGPose pose : getPosesByBlock(block)) if(!removePose(pose, GStopReason.KICKED)) return false;
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
        poses.put(player.getUniqueId(), poseObject);
        blockPoses.computeIfAbsent(block, k -> new HashSet<>()).add(poseObject);
        blockTypes.put(block, block.getType());
        poseUsageCount++;
        Bukkit.getPluginManager().callEvent(new PlayerPoseEvent(poseObject));

        return poseObject;
    }

    public boolean removePose(IGPose pose, GStopReason stopReason) { return removePose(pose, stopReason, true); }

    public boolean removePose(IGPose pose, GStopReason stopReason, boolean useReturnLocation) {
        PrePlayerStopPoseEvent prePlayerStopPoseEvent = new PrePlayerStopPoseEvent(pose, stopReason);
        Bukkit.getPluginManager().callEvent(prePlayerStopPoseEvent);
        if(prePlayerStopPoseEvent.isCancelled() && stopReason.isCancellable()) return false;

        Player player = pose.getPlayer();

        Location returnLocation = gSitMain.getConfigService().GET_UP_RETURN ? pose.getSeat().getReturnLocation() : pose.getSeat().getLocation().add(0d, gSitMain.getSitService().getBaseOffset() + (Tag.STAIRS.isTagged(pose.getSeat().getBlock().getType()) ? SitService.STAIR_Y_OFFSET : 0d) - gSitMain.getConfigService().S_SITMATERIALS.getOrDefault(pose.getSeat().getBlock().getType(), 0d), 0d);
        Location entityLocation = player.getLocation();
        returnLocation.setYaw(entityLocation.getYaw());
        returnLocation.setPitch(entityLocation.getPitch());
        if(player.isValid() && useReturnLocation) gSitMain.getEntityUtil().setEntityLocation(player, returnLocation);

        blockPoses.remove(pose.getSeat().getBlock());
        blockTypes.remove(pose.getSeat().getBlock());
        poses.remove(player.getUniqueId());
        pose.remove();
        pose.getSeat().getSeatEntity().remove();
        Bukkit.getPluginManager().callEvent(new PlayerStopPoseEvent(pose, stopReason));
        poseUsageNanoTime += pose.getSeat().getLifetimeInNanoSeconds();

        return true;
    }

    public int getPoseUsageCount() { return poseUsageCount; }

    public long getPoseUsageTimeInSeconds() { return poseUsageNanoTime / 1_000_000_000; }

    public void resetPoseUsageStats() {
        poseUsageCount = 0;
        poseUsageNanoTime = 0;
    }

}