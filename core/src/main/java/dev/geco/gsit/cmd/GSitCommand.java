package dev.geco.gsit.cmd;

import org.jetbrains.annotations.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.util.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class GSitCommand implements CommandExecutor {

    private final GSitMain GPM;

    public GSitCommand(GSitMain GPluginMain) { GPM = GPluginMain; }

    @Override
    public boolean onCommand(@NotNull CommandSender Sender, @NotNull Command Command, @NotNull String Label, String[] Args) {

        if(!(Sender instanceof Player player)) {

            GPM.getMManager().sendMessage(Sender, "Messages.command-sender-error");
            return true;
        }

        if(Args.length == 0) {

            if(!GPM.getPManager().hasPermission(Sender, "Sit", "Sit.*")) {

                GPM.getMManager().sendMessage(Sender, "Messages.command-permission-error");
                return true;
            }

            if(GPM.getSitManager().isSitting(player)) {

                GPM.getSitManager().removeSeat(player, GetUpReason.GET_UP);
                return true;
            }

            if(!player.isValid() || player.isSneaking() || !player.isOnGround() || player.getVehicle() != null || player.isSleeping()) {

                GPM.getMManager().sendMessage(Sender, "Messages.action-sit-now-error");
                return true;
            }

            if(!GPM.getEnvironmentUtil().isInAllowedWorld(player)) {

                GPM.getMManager().sendMessage(Sender, "Messages.action-sit-world-error");
                return true;
            }

            Location playerLocation = player.getLocation();

            Block block = playerLocation.getBlock().isPassable() ? playerLocation.subtract(0, 0.0625, 0).getBlock() : playerLocation.getBlock();

            if(GPM.getCManager().MATERIALBLACKLIST.contains(block.getType())) {

                GPM.getMManager().sendMessage(Sender, "Messages.action-sit-location-error");
                return true;
            }

            boolean overSize = false;

            try {

                for(BoundingBox boundingBox : block.getCollisionShape().getBoundingBoxes()) if(boundingBox.getMaxY() > 1.25) overSize = true;
            } catch (Throwable ignored) { }

            if(!GPM.getCManager().ALLOW_UNSAFE && !(block.getRelative(BlockFace.UP).isPassable() && !overSize && (!block.isPassable() || !GPM.getCManager().CENTER_BLOCK))) {

                GPM.getMManager().sendMessage(Sender, "Messages.action-sit-location-error");
                return true;
            }

            if(!GPM.getPManager().hasPermission(Sender, "ByPass.Region", "ByPass.*")) {

                if(GPM.getWorldGuardLink() != null && !GPM.getWorldGuardLink().checkFlag(block.getLocation(), GPM.getWorldGuardLink().getFlag("sit"))) {

                    GPM.getMManager().sendMessage(Sender, "Messages.action-sit-region-error");
                    return true;
                }

                if(GPM.getGriefPreventionLink() != null && !GPM.getGriefPreventionLink().check(block.getLocation(), player)) {

                    GPM.getMManager().sendMessage(Sender, "Messages.action-sit-region-error");
                    return true;
                }

                if(GPM.getPlotSquaredLink() != null && !GPM.getPlotSquaredLink().canCreateSeat(block.getLocation(), player)) {

                    GPM.getMManager().sendMessage(Sender, "Messages.action-sit-region-error");
                    return true;
                }
            }

            if(!GPM.getCManager().SAME_BLOCK_REST && !GPM.getSitManager().kickSeat(block, player)) {

                GPM.getMManager().sendMessage(Sender, "Messages.action-sit-kick-error");
                return true;
            }

            if(Tag.STAIRS.isTagged(block.getType()) ? GPM.getEnvironmentUtil().createSeatForStair(block, player) == null : GPM.getSitManager().createSeat(block, player) == null) GPM.getMManager().sendMessage(Sender, "Messages.action-sit-error");
            return true;
        }

        switch(Args[0]) {

            case "toggle":

                if(GPM.getPManager().hasPermission(Sender, "SitToggle", "Sit.*") && !GPM.getCManager().S_SITMATERIALS.isEmpty()) {

                    boolean toggle = GPM.getToggleManager().canSit(player.getUniqueId());

                    if(Args.length > 1 && Args[1].equalsIgnoreCase("off")) toggle = true;

                    if(Args.length > 1 && Args[1].equalsIgnoreCase("on")) toggle = false;

                    if(toggle) {

                        GPM.getToggleManager().setCanSit(player.getUniqueId(), false);

                        GPM.getMManager().sendMessage(Sender, "Messages.command-gsit-toggle-off");
                    } else {

                        GPM.getToggleManager().setCanSit(player.getUniqueId(), true);

                        GPM.getMManager().sendMessage(Sender, "Messages.command-gsit-toggle-on");
                    }

                    break;
                }
            case "playertoggle":

                if(GPM.getPManager().hasPermission(Sender, "PlayerSitToggle", "PlayerSit.*") && GPM.getCManager().PS_ALLOW_SIT) {

                    boolean toggle = GPM.getToggleManager().canPlayerSit(player.getUniqueId());

                    if(Args.length > 1 && Args[1].equalsIgnoreCase("off")) toggle = true;

                    if(Args.length > 1 && Args[1].equalsIgnoreCase("on")) toggle = false;

                    if(toggle) {

                        GPM.getToggleManager().setCanPlayerSit(player.getUniqueId(), false);

                        GPM.getMManager().sendMessage(Sender, "Messages.command-gsit-playertoggle-off");
                    } else {

                        GPM.getToggleManager().setCanPlayerSit(player.getUniqueId(), true);

                        GPM.getMManager().sendMessage(Sender, "Messages.command-gsit-playertoggle-on");
                    }

                    break;
                }
            default:

                Bukkit.dispatchCommand(Sender, Label);
                break;
        }

        return true;
    }

}