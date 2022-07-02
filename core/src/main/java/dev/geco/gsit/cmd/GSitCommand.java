package dev.geco.gsit.cmd;

import org.jetbrains.annotations.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class GSitCommand implements CommandExecutor {

    private final GSitMain GPM;

    public GSitCommand(GSitMain GPluginMain) { GPM = GPluginMain; }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, String[] a) {
        if(s instanceof Player) {
            Player p = (Player) s;
            if(a.length == 0) {
                if(GPM.getPManager().hasNormalPermission(s, "Sit")) {
                    if(GPM.getSitManager().isSitting(p)) {
                        GPM.getSitManager().removeSeat(GPM.getSitManager().getSeat(p), GetUpReason.GET_UP);
                    } else {
                        if(p.isValid() && !p.isSneaking() && p.isOnGround() && !p.isInsideVehicle() && !p.isSleeping()) {
                            if(!GPM.getCManager().WORLDBLACKLIST.contains(p.getWorld().getName()) || GPM.getPManager().hasPermission(s, "ByPass.World", "ByPass.*")) {
                                Location pl = p.getLocation();
                                Block b = pl.getBlock().isPassable() ? pl.subtract(0, 0.0625, 0).getBlock() : pl.getBlock();
                                if(!GPM.getCManager().MATERIALBLACKLIST.contains(b.getType())) {
                                    if(GPM.getCManager().ALLOW_UNSAFE || (b.getRelative(BlockFace.UP).isPassable() && (!b.isPassable() || !GPM.getCManager().CENTER_BLOCK))) {
                                        if(GPM.getWorldGuardLink() == null || GPM.getWorldGuardLink().checkFlag(b.getLocation(), GPM.getWorldGuardLink().SIT_FLAG)) {
                                            if(GPM.getGriefPreventionLink() == null || GPM.getGriefPreventionLink().check(b.getLocation(), p)) {
                                                if(GPM.getCManager().SAME_BLOCK_REST || GPM.getSitManager().kickSeat(b, p)) {
                                                    if(Tag.STAIRS.isTagged(b.getType())) {
                                                        GSeat v = GPM.getSitUtil().createSeatForStair(b, p);
                                                        if(v == null) GPM.getMManager().sendMessage(s, "Messages.action-sit-region-error");
                                                    } else {
                                                        GSeat v = GPM.getSitManager().createSeat(b, p);
                                                        if(v == null) GPM.getMManager().sendMessage(s, "Messages.action-sit-region-error");
                                                    }
                                                } else GPM.getMManager().sendMessage(s, "Messages.action-sit-kick-error");
                                            } else GPM.getMManager().sendMessage(s, "Messages.action-sit-region-error");
                                        } else GPM.getMManager().sendMessage(s, "Messages.action-sit-region-error");
                                    } else GPM.getMManager().sendMessage(s, "Messages.action-sit-location-error");
                                } else GPM.getMManager().sendMessage(s, "Messages.action-sit-location-error");
                            } else GPM.getMManager().sendMessage(s, "Messages.action-sit-world-error");
                        } else GPM.getMManager().sendMessage(s, "Messages.action-sit-now-error");
                    }
                } else GPM.getMManager().sendMessage(s, "Messages.command-permission-error");
            } else {
                switch(a[0]) {
                    case "toggle":
                        if(GPM.getPManager().hasNormalPermission(s, "SitToggle")) {
                            if(GPM.getToggleManager().canSit(p.getUniqueId())) {
                                GPM.getToggleManager().setCanSit(p.getUniqueId(), false);
                                GPM.getMManager().sendMessage(s, "Messages.command-gsit-toggle-off");
                            } else {
                                GPM.getToggleManager().setCanSit(p.getUniqueId(), true);
                                GPM.getMManager().sendMessage(s, "Messages.command-gsit-toggle-on");
                            }
                            break;
                        }
                    case "playertoggle":
                        if(GPM.getPManager().hasNormalPermission(s, "PlayerSitToggle")) {
                            if(GPM.getToggleManager().canPlayerSit(p.getUniqueId())) {
                                GPM.getToggleManager().setCanPlayerSit(p.getUniqueId(), false);
                                GPM.getMManager().sendMessage(s, "Messages.command-gsit-playertoggle-off");
                            } else {
                                GPM.getToggleManager().setCanPlayerSit(p.getUniqueId(), true);
                                GPM.getMManager().sendMessage(s, "Messages.command-gsit-playertoggle-on");
                            }
                            break;
                        }
                    default:
                        Bukkit.dispatchCommand(s, l);
                        break;
                }
            }
        } else GPM.getMManager().sendMessage(s, "Messages.command-sender-error");
        return true;
    }

}