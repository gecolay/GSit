package dev.geco.gsit.cmd;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.model.Seat;
import dev.geco.gsit.model.StopReason;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GSitCommand implements CommandExecutor {

    private final GSitMain gSitMain;

    public GSitCommand(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(args.length == 0) {
            if(!(sender instanceof Player player)) {
                gSitMain.getMessageService().sendMessage(sender, "Messages.command-sender-error");
                return true;
            }

            if(!gSitMain.getPermissionService().hasPermission(sender, "Sit", "Sit.*")) {
                gSitMain.getMessageService().sendMessage(sender, "Messages.command-permission-error");
                return true;
            }

            Seat seat = gSitMain.getSitService().getSeatByEntity(player);
            if(seat != null) {
                gSitMain.getSitService().removeSeat(seat, StopReason.GET_UP);
                return true;
            }

            if(!player.isValid() || player.isSneaking() || !player.isOnGround() || player.getVehicle() != null || player.isSleeping()) {
                gSitMain.getMessageService().sendMessage(sender, "Messages.action-sit-now-error");
                return true;
            }

            if(!gSitMain.getEnvironmentUtil().isEntityInAllowedWorld(player)) {
                gSitMain.getMessageService().sendMessage(sender, "Messages.action-sit-world-error");
                return true;
            }

            Location playerLocation = player.getLocation();
            Block block = playerLocation.getBlock().isPassable() ? playerLocation.subtract(0, 0.0625, 0).getBlock() : playerLocation.getBlock();
            if(gSitMain.getSitService().isBlacklistedSitBlockData(block.getBlockData())) {
                gSitMain.getMessageService().sendMessage(sender, "Messages.action-sit-location-error");
                return true;
            }

            boolean overSize = false;
            try {
                for(BoundingBox boundingBox : block.getCollisionShape().getBoundingBoxes()) if(boundingBox.getMaxY() > 1.25) overSize = true;
            } catch(Throwable ignored) { }
            if(!gSitMain.getConfigService().ALLOW_UNSAFE && !(block.getRelative(BlockFace.UP).isPassable() && !overSize && (!block.isPassable() || !gSitMain.getConfigService().CENTER_BLOCK))) {
                gSitMain.getMessageService().sendMessage(sender, "Messages.action-sit-location-error");
                return true;
            }

            if(!gSitMain.getEnvironmentUtil().canUseInLocation(block.getLocation(), player, "sit")) {
                gSitMain.getMessageService().sendMessage(sender, "Messages.action-sit-region-error");
                return true;
            }

            if(!gSitMain.getConfigService().SAME_BLOCK_REST && !gSitMain.getSitService().kickSeatEntitiesFromBlock(block, player)) {
                gSitMain.getMessageService().sendMessage(sender, "Messages.action-sit-kick-error");
                return true;
            }

            if(Tag.STAIRS.isTagged(block.getType()) ? gSitMain.getSitService().createStairSeatForEntity(block, player) == null : gSitMain.getSitService().createSeat(block, player) == null) gSitMain.getMessageService().sendMessage(sender, "Messages.action-sit-error");
            return true;
        }

        switch(args[0]) {
            case "toggle" -> {
                if(!gSitMain.getPermissionService().hasPermission(sender, "SitToggle", "Sit.*")) {
                    Bukkit.dispatchCommand(sender, label);
                    return true;
                }

                if(args.length == 1 && !(sender instanceof Player)) {
                    Bukkit.dispatchCommand(sender, label);
                    return true;
                }

                UUID uuid = sender instanceof Player player ? player.getUniqueId() : getTargetUuid(args[1]);

                boolean toggle = gSitMain.getToggleService().canEntityUseSit(uuid);
                int toggleIndex = sender instanceof Player ? 1 : 2;
                if(args.length > toggleIndex && args[toggleIndex].equalsIgnoreCase("off")) toggle = true;
                if(args.length > toggleIndex && args[toggleIndex].equalsIgnoreCase("on")) toggle = false;

                if(toggle) {
                    gSitMain.getToggleService().setEntityCanUseSit(uuid, false);
                    gSitMain.getMessageService().sendMessage(sender, "Messages.command-gsit-toggle-off");
                } else {
                    gSitMain.getToggleService().setEntityCanUseSit(uuid, true);
                    gSitMain.getMessageService().sendMessage(sender, "Messages.command-gsit-toggle-on");
                }

                return true;
            }
            case "playertoggle" -> {
                if(!gSitMain.getPermissionService().hasPermission(sender, "PlayerSitToggle", "PlayerSit.*") || !gSitMain.getConfigService().PS_ALLOW_SIT) {
                    Bukkit.dispatchCommand(sender, label);
                    return true;
                }

                if(args.length == 1 && !(sender instanceof Player)) {
                    Bukkit.dispatchCommand(sender, label);
                    return true;
                }

                UUID uuid = sender instanceof Player player ? player.getUniqueId() : getTargetUuid(args[1]);

                boolean toggle = gSitMain.getToggleService().canPlayerUsePlayerSit(uuid);
                int toggleIndex = sender instanceof Player ? 1 : 2;
                if(args.length > toggleIndex && args[toggleIndex].equalsIgnoreCase("off")) toggle = true;
                if(args.length > toggleIndex && args[toggleIndex].equalsIgnoreCase("on")) toggle = false;

                if(toggle) {
                    gSitMain.getToggleService().setPlayerCanUsePlayerSit(uuid, false);
                    gSitMain.getMessageService().sendMessage(sender, "Messages.command-gsit-playertoggle-off");
                } else {
                    gSitMain.getToggleService().setPlayerCanUsePlayerSit(uuid, true);
                    gSitMain.getMessageService().sendMessage(sender, "Messages.command-gsit-playertoggle-on");
                }

                return true;
            }
        }

        Bukkit.dispatchCommand(sender, label);
        return true;
    }

    private UUID getTargetUuid(String name) {
        try {
            return UUID.fromString(name);
        } catch(IllegalArgumentException e) {
            return Bukkit.getOfflinePlayer(name).getUniqueId();
        }
    }

}