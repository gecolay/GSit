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

public class GSitCommand implements CommandExecutor {

    private final GSitMain gSitMain;

    public GSitCommand(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(!(sender instanceof Player player)) {
            gSitMain.getMessageService().sendMessage(sender, "Messages.command-sender-error");
            return true;
        }

        if(args.length == 0) {
            if(!gSitMain.getPermissionService().hasPermission(sender, "Sit", "Sit.*")) {
                gSitMain.getMessageService().sendMessage(sender, "Messages.command-permission-error");
                return true;
            }

            Seat seat = gSitMain.getSitService().getSeatByEntity(player);
            if(seat != null) {
                gSitMain.getSitService().removeSeat(seat, StopReason.GET_UP);
                return true;
            }

            if(!player.isValid() || player.isSneaking() || !player.isOnGround() || player.getVehicle() != null || player.isSleeping() || gSitMain.getSitService().isEntityBlocked(player)) {
                gSitMain.getMessageService().sendMessage(sender, "Messages.action-sit-now-error");
                return true;
            }

            if(!gSitMain.getEnvironmentUtil().isEntityInAllowedWorld(player)) {
                gSitMain.getMessageService().sendMessage(sender, "Messages.action-sit-world-error");
                return true;
            }

            Location playerLocation = player.getLocation();
            Block block = playerLocation.getBlock().isPassable() ? playerLocation.subtract(0, 0.0625, 0).getBlock() : playerLocation.getBlock();
            if(gSitMain.getConfigService().MATERIALBLACKLIST.contains(block.getType())) {
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
            case "toggle":
                if(gSitMain.getPermissionService().hasPermission(sender, "SitToggle", "Sit.*") && !gSitMain.getConfigService().S_SITMATERIALS.isEmpty()) {
                    boolean toggle = gSitMain.getToggleService().canEntityUseSit(player.getUniqueId());
                    if(args.length > 1 && args[1].equalsIgnoreCase("off")) toggle = true;
                    if(args.length > 1 && args[1].equalsIgnoreCase("on")) toggle = false;

                    if(toggle) {
                        gSitMain.getToggleService().setEntityCanUseSit(player.getUniqueId(), false);
                        gSitMain.getMessageService().sendMessage(sender, "Messages.command-gsit-toggle-off");
                    } else {
                        gSitMain.getToggleService().setEntityCanUseSit(player.getUniqueId(), true);
                        gSitMain.getMessageService().sendMessage(sender, "Messages.command-gsit-toggle-on");
                    }
                    break;
                }
            case "playertoggle":
                if(gSitMain.getPermissionService().hasPermission(sender, "PlayerSitToggle", "PlayerSit.*") && gSitMain.getConfigService().PS_ALLOW_SIT) {
                    boolean toggle = gSitMain.getToggleService().canPlayerUsePlayerSit(player.getUniqueId());
                    if(args.length > 1 && args[1].equalsIgnoreCase("off")) toggle = true;
                    if(args.length > 1 && args[1].equalsIgnoreCase("on")) toggle = false;

                    if(toggle) {
                        gSitMain.getToggleService().setPlayerCanUsePlayerSit(player.getUniqueId(), false);
                        gSitMain.getMessageService().sendMessage(sender, "Messages.command-gsit-playertoggle-off");
                    } else {
                        gSitMain.getToggleService().setPlayerCanUsePlayerSit(player.getUniqueId(), true);
                        gSitMain.getMessageService().sendMessage(sender, "Messages.command-gsit-playertoggle-on");
                    }
                    break;
                }
            default:
                Bukkit.dispatchCommand(sender, label);
                break;
        }

        return true;
    }

}