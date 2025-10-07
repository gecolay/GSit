package dev.geco.gsit.cmd;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.model.Pose;
import dev.geco.gsit.model.PoseType;
import dev.geco.gsit.model.StopReason;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public class GLayBackCommand implements CommandExecutor {

    private final GSitMain gSitMain;

    public GLayBackCommand(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(!(sender instanceof Player player)) {
            gSitMain.getMessageService().sendMessage(sender, "Messages.command-sender-error");
            return true;
        }

        if(!gSitMain.getPermissionService().hasPermission(sender, "LayBack", "Pose.*")) {
            gSitMain.getMessageService().sendMessage(sender, "Messages.command-permission-error");
            return true;
        }

        if(!gSitMain.getPoseService().isAvailable()) {
            gSitMain.getMessageService().sendMessage(sender, "Messages.command-version-error", "%Version%", gSitMain.getVersionManager().getServerVersion());
            return true;
        }

        Pose pose = gSitMain.getPoseService().getPoseByPlayer(player);
        if(pose != null && pose.getPoseType() == PoseType.LAY_BACK) {
            gSitMain.getPoseService().removePose(pose, StopReason.GET_UP);
            return true;
        }

        if(!player.isValid() || player.isSneaking() || !player.isOnGround() || player.getVehicle() != null || player.isSleeping() || gSitMain.getSitService().isEntityBlocked(player)) {
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

        pose = gSitMain.getPoseService().createPose(block, player, PoseType.LAY_BACK);
        if(pose == null) gSitMain.getMessageService().sendMessage(sender, "Messages.action-pose-error");

        return true;
    }

}