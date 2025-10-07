package dev.geco.gsit.service;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.PlayerStopPoseEvent;
import dev.geco.gsit.api.event.PlayerPoseEvent;
import dev.geco.gsit.api.event.PrePlayerStopPoseEvent;
import dev.geco.gsit.api.event.PrePlayerPoseEvent;
import dev.geco.gsit.model.PoseType;
import dev.geco.gsit.model.Seat;
import dev.geco.gsit.model.StopReason;
import dev.geco.gsit.model.Pose;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

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
    private final HashMap<UUID, Pose> poses = new HashMap<>();
    private final HashMap<Block, Set<Pose>> blockPoses = new HashMap<>();
    private int poseUsageCount = 0;
    private long poseUsageNanoTime = 0;

    public PoseService(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        available = gSitMain.getVersionManager().isNewerOrVersion(18, 0);
    }

    public boolean isAvailable() { return available; }

    public HashMap<UUID, Pose> getAllPoses() { return poses; }

    public boolean isPlayerPosing(Player player) { return poses.containsKey(player.getUniqueId()); }

    public Pose getPoseByPlayer(Player player) { return poses.get(player.getUniqueId()); }

    public void removeAllPoses() { for(Pose pose : new ArrayList<>(poses.values())) removePose(pose, StopReason.PLUGIN); }

    public boolean isBlockWithPose(Block block) { return blockPoses.containsKey(block); }

    public Set<Pose> getPosesByBlock(Block block) { return blockPoses.getOrDefault(block, Collections.emptySet()); }

    public boolean kickPoseEntitiesFromBlock(Block block, Player player) {
        if(!isBlockWithPose(block)) return true;
        if(!gSitMain.getPermissionService().hasPermission(player, "Kick.Pose", "Kick.*")) return false;
        for(Pose pose : getPosesByBlock(block)) if(!removePose(pose, StopReason.KICKED)) return false;
        return true;
    }

    public Pose createPose(Block block, Player player, PoseType poseType) { return createPose(block, player, poseType, 0d, 0d, 0d, player.getLocation().getYaw(), gSitMain.getConfigService().CENTER_BLOCK); }

    public Pose createPose(Block block, Player player, PoseType poseType, double xOffset, double yOffset, double zOffset, float seatRotation, boolean sitInBlockCenter) {
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

        Pose pose = gSitMain.getEntityUtil().createPose(new Seat(block, seatLocation, player, seatEntity, returnLocation), poseType);
        pose.spawn();
        poses.put(player.getUniqueId(), pose);
        blockPoses.computeIfAbsent(block, b -> new HashSet<>()).add(pose);
        poseUsageCount++;
        Bukkit.getPluginManager().callEvent(new PlayerPoseEvent(pose));

        return pose;
    }

    public boolean removePose(Pose pose, StopReason stopReason) { return removePose(pose, stopReason, true); }

    public boolean removePose(Pose pose, StopReason stopReason, boolean useSafeDismount) {
        PrePlayerStopPoseEvent prePlayerStopPoseEvent = new PrePlayerStopPoseEvent(pose, stopReason);
        Bukkit.getPluginManager().callEvent(prePlayerStopPoseEvent);
        if(prePlayerStopPoseEvent.isCancelled() && stopReason.isCancellable()) return false;

        Seat seat = pose.getSeat();
        Player player = pose.getPlayer();
        if(useSafeDismount) gSitMain.getSitService().handleSafeSeatDismount(seat);

        blockPoses.remove(seat.getBlock());
        poses.remove(player.getUniqueId());
        pose.remove();
        seat.getSeatEntity().remove();
        Bukkit.getPluginManager().callEvent(new PlayerStopPoseEvent(pose, stopReason));
        poseUsageNanoTime += seat.getLifetimeInNanoSeconds();

        return true;
    }

    public int getPoseUsageCount() { return poseUsageCount; }

    public long getPoseUsageTimeInSeconds() { return poseUsageNanoTime / 1_000_000_000; }

    public void resetPoseUsageStats() {
        poseUsageCount = 0;
        poseUsageNanoTime = 0;
    }

}