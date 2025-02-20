package dev.geco.gsit.cmd;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.object.GStopReason;
import dev.geco.gsit.object.IGPose;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public class GSpinCommand implements CommandExecutor {

    private final GSitMain gSitMain;

    public GSpinCommand(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(!(sender instanceof Player player)) {
            gSitMain.getMessageService().sendMessage(sender, "Messages.command-sender-error");
            return true;
        }

        if(!gSitMain.getPermissionService().hasPermission(sender, "Spin", "Pose.*")) {
            gSitMain.getMessageService().sendMessage(sender, "Messages.command-permission-error");
            return true;
        }

        if(!gSitMain.getPoseService().isAvailable()) {
            gSitMain.getMessageService().sendMessage(sender, "Messages.command-version-error", "%Version%", gSitMain.getVersionManager().getServerVersion());
            return true;
        }

        IGPose poseObject = gSitMain.getPoseService().getPoseByPlayer(player);
        if(poseObject != null && poseObject.getPose() == Pose.SPIN_ATTACK) {
            gSitMain.getPoseService().removePose(poseObject, GStopReason.GET_UP);
            return true;
        }

        if(!player.isValid() || player.isSneaking() || !player.isOnGround() || player.getVehicle() != null || player.isSleeping()) {
            gSitMain.getMessageService().sendMessage(sender, "Messages.action-pose-now-error");
            return true;
        }

        if(!gSitMain.getEnvironmentUtil().isEntityInAllowedWorld(player)) {
            gSitMain.getMessageService().sendMessage(sender, "Messages.action-pose-world-error");
            return true;
        }

        Location playerLocation = player.getLocation();
        Block block = playerLocation.getBlock().isPassable() ? playerLocation.subtract(0, 0.0625, 0).getBlock() : playerLocation.getBlock();
        if(gSitMain.getConfigService().MATERIALBLACKLIST.contains(block.getType())) {
            gSitMain.getMessageService().sendMessage(sender, "Messages.action-pose-location-error");
            return true;
        }

        boolean overSize = false;
        try {
            for(BoundingBox boundingBox : block.getCollisionShape().getBoundingBoxes()) if(boundingBox.getMaxY() > 1.25) overSize = true;
        } catch(Throwable ignored) { }
        if(!gSitMain.getConfigService().ALLOW_UNSAFE && !(block.getRelative(BlockFace.UP).isPassable() && !overSize && (!block.isPassable() || !gSitMain.getConfigService().CENTER_BLOCK))) {
            gSitMain.getMessageService().sendMessage(sender, "Messages.action-pose-location-error");
            return true;
        }

        if(!gSitMain.getEnvironmentUtil().canUseInLocation(block.getLocation(), player, "pose")) {
            gSitMain.getMessageService().sendMessage(sender, "Messages.action-pose-region-error");
            return true;
        }

        if(!gSitMain.getConfigService().SAME_BLOCK_REST && !gSitMain.getPoseService().kickPoseEntitiesFromBlock(block, player)) {
            gSitMain.getMessageService().sendMessage(sender, "Messages.action-pose-kick-error");
            return true;
        }

        if(gSitMain.getPoseService().createPose(block, player, Pose.SPIN_ATTACK) == null) gSitMain.getMessageService().sendMessage(sender, "Messages.action-pose-error");
        return true;
    }

}