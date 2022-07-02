package dev.geco.gsit.cmd;

import org.jetbrains.annotations.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class GSpinCommand implements CommandExecutor {

    private final GSitMain GPM;

    public GSpinCommand(GSitMain GPluginMain) { GPM = GPluginMain; }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, String[] a) {
        if(s instanceof Player) {
            Player p = (Player) s;
            if(GPM.getPManager().hasNormalPermission(s, "Spin")) {
                if(GPM.getPoseManager() != null) {
                    if(GPM.getPoseManager().isPosing(p) && GPM.getPoseManager().getPose(p).getPose() == Pose.SPIN_ATTACK) {
                        GPM.getPoseManager().removePose(GPM.getPoseManager().getPose(p), GetUpReason.GET_UP);
                    } else {
                        if(p.isValid() && !p.isSneaking() && p.isOnGround() && !p.isInsideVehicle() && !p.isSleeping()) {
                            if(!GPM.getCManager().WORLDBLACKLIST.contains(p.getWorld().getName()) || GPM.getPManager().hasPermission(s, "ByPass.World", "ByPass.*")) {
                                Location pl = p.getLocation();
                                Block b = pl.getBlock().isPassable() ? pl.subtract(0, 0.0625, 0).getBlock() : pl.getBlock();
                                if(!GPM.getCManager().MATERIALBLACKLIST.contains(b.getType())) {
                                    if(GPM.getCManager().ALLOW_UNSAFE || (b.getRelative(BlockFace.UP).isPassable() && (!b.isPassable() || !GPM.getCManager().CENTER_BLOCK))) {
                                        if(GPM.getWorldGuardLink() == null || GPM.getWorldGuardLink().checkFlag(b.getLocation(), GPM.getWorldGuardLink().POSE_FLAG)) {
                                            if(GPM.getGriefPreventionLink() == null || GPM.getGriefPreventionLink().check(b.getLocation(), p)) {
                                                if(GPM.getPlotSquaredLink() == null || GPM.getPlotSquaredLink().canCreateSeat(b.getLocation(), p)) {
                                                    if(GPM.getCManager().SAME_BLOCK_REST || GPM.getPoseManager().kickPose(b, p)) {
                                                        IGPoseSeat v = GPM.getPoseManager().createPose(b, p, Pose.SPIN_ATTACK);
                                                        if(v == null) GPM.getMManager().sendMessage(s, "Messages.action-pose-region-error");
                                                    } else GPM.getMManager().sendMessage(s, "Messages.action-pose-kick-error");
                                                } else GPM.getMManager().sendMessage(s, "Messages.action-pose-region-error");
                                            } else GPM.getMManager().sendMessage(s, "Messages.action-pose-region-error");
                                        } else GPM.getMManager().sendMessage(s, "Messages.action-pose-region-error");
                                    } else GPM.getMManager().sendMessage(s, "Messages.action-pose-location-error");
                                } else GPM.getMManager().sendMessage(s, "Messages.action-pose-location-error");
                            } else GPM.getMManager().sendMessage(s, "Messages.action-pose-world-error");
                        } else GPM.getMManager().sendMessage(s, "Messages.action-pose-now-error");
                    }
                } else {
                    String v = Bukkit.getServer().getClass().getPackage().getName();
                    v = v.substring(v.lastIndexOf('.') + 1);
                    GPM.getMManager().sendMessage(s, "Messages.command-version-error", "%Version%", v);
                }
            } else GPM.getMManager().sendMessage(s, "Messages.command-permission-error");
        } else GPM.getMManager().sendMessage(s, "Messages.command-sender-error");
        return true;
    }

}