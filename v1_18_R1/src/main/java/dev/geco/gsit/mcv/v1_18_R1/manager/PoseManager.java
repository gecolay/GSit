package dev.geco.gsit.mcv.v1_18_R1.manager;

import java.util.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.metadata.*;
import org.bukkit.scheduler.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.*;
import dev.geco.gsit.manager.*;
import dev.geco.gsit.objects.*;
import dev.geco.gsit.mcv.v1_18_R1.objects.*;

public class PoseManager implements IPoseManager {

    private final GSitMain GPM;

    public PoseManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    private int feature_used = 0;

    public int getFeatureUsedCount() { return feature_used; }

    public void resetFeatureUsedCount() { feature_used = 0; }

    private final List<IGPoseSeat> poses = new ArrayList<>();

    private final HashMap<IGPoseSeat, BukkitRunnable> rotate = new HashMap<>();

    public List<IGPoseSeat> getPoses() { return new ArrayList<>(poses); }

    public boolean isPosing(Player Player) { return getPose(Player) != null; }

    public IGPoseSeat getPose(Player Player) {

        for(IGPoseSeat pose : getPoses()) if(Player.equals(pose.getSeat().getPlayer())) return pose;
        return null;
    }

    public void clearPoses() { for(IGPoseSeat pose : getPoses()) removePose(pose.getSeat().getPlayer(), GetUpReason.PLUGIN); }

    public boolean kickPose(Block Block, Player Player) {

        if(GPM.getPoseUtil().isPoseBlock(Block)) {

            if(!GPM.getPManager().hasPermission(Player, "Kick.Pose", "Kick.*")) return false;

            for(IGPoseSeat p : GPM.getPoseUtil().getPoses(Block)) if(!removePose(p.getSeat().getPlayer(), GetUpReason.KICKED)) return false;
        }

        return true;
    }

    public IGPoseSeat createPose(Block Block, Player Player, Pose Pose) { return createPose(Block, Player, Pose, 0d, 0d, 0d, Player.getLocation().getYaw(), GPM.getCManager().CENTER_BLOCK, GPM.getCManager().GET_UP_SNEAK); }

    public IGPoseSeat createPose(Block Block, Player Player, Pose Pose, double XOffset, double YOffset, double ZOffset, float SeatRotation, boolean SitAtBlock, boolean GetUpSneak) {

        PrePlayerPoseEvent preEvent = new PrePlayerPoseEvent(Player, Block);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return null;

        double offset = SitAtBlock ? Block.getBoundingBox().getMinY() + Block.getBoundingBox().getHeight() : 0d;

        offset = (SitAtBlock ? offset == 0d ? 1d : offset - Block.getY() : offset) + GPM.getCManager().S_SITMATERIALS.getOrDefault(Block.getType(), 0d);

        Location playerLocation = Player.getLocation().clone();

        Location returnLocation = playerLocation.clone();

        if(SitAtBlock) {

            playerLocation = Block.getLocation().clone().add(0.5d + XOffset, YOffset + offset - 0.2d, 0.5d + ZOffset);
        } else {

            playerLocation = playerLocation.add(XOffset, YOffset - 0.2d + GPM.getCManager().S_SITMATERIALS.getOrDefault(Block.getType(), 0d), ZOffset);
        }

        if(!GPM.getSpawnUtil().checkLocation(playerLocation)) return null;

        playerLocation.setYaw(SeatRotation);

        Entity seatEntity = GPM.getSpawnUtil().createSeatEntity(playerLocation, Player);

        if(GPM.getCManager().P_POSE_MESSAGE) GPM.getMManager().sendActionBarMessage(Player, "Messages.action-pose-info");

        GSeat seat = new GSeat(Block, playerLocation, Player, seatEntity, returnLocation);

        GPoseSeat poseseat = new GPoseSeat(seat, Pose);

        poseseat.spawn();

        seatEntity.setMetadata(GPM.NAME + "P", new FixedMetadataValue(GPM, poseseat));

        poses.add(poseseat);

        GPM.getPoseUtil().setPoseBlock(Block, poseseat);

        startRotateSeat(poseseat);

        feature_used++;

        Bukkit.getPluginManager().callEvent(new PlayerPoseEvent(poseseat));

        return poseseat;
    }

    private void startRotateSeat(IGPoseSeat PoseSeat) {

        if(rotate.containsKey(PoseSeat)) stopRotateSeat(PoseSeat);

        BukkitRunnable task = new BukkitRunnable() {

            @Override
            public void run() {

                if(!poses.contains(PoseSeat) || PoseSeat.getSeat().getSeatEntity().getPassengers().isEmpty()) {

                    cancel();
                    return;
                }

                Location location = PoseSeat.getSeat().getSeatEntity().getPassengers().get(0).getLocation();
                PoseSeat.getSeat().getSeatEntity().setRotation(location.getYaw(), location.getPitch());
            }
        };

        task.runTaskTimer(GPM, 0, 2);

        rotate.put(PoseSeat, task);
    }

    protected void stopRotateSeat(IGPoseSeat PoseSeat) {

        if(!rotate.containsKey(PoseSeat)) return;

        BukkitRunnable task = rotate.get(PoseSeat);

        if(task != null && !task.isCancelled()) task.cancel();

        rotate.remove(PoseSeat);
    }

    public boolean removePose(Player Player, GetUpReason Reason) { return removePose(Player, Reason, true); }

    public boolean removePose(Player Player, GetUpReason Reason, boolean Safe) {

        if(!isPosing(Player)) return true;

        IGPoseSeat poseSeat = getPose(Player);

        PrePlayerGetUpPoseEvent preEvent = new PrePlayerGetUpPoseEvent(poseSeat, Reason);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return false;

        GPM.getPoseUtil().removePoseBlock(poseSeat.getSeat().getBlock(), poseSeat);

        poses.remove(poseSeat);

        stopRotateSeat(poseSeat);

        poseSeat.remove();

        Location returnLocation = (GPM.getCManager().GET_UP_RETURN ? poseSeat.getSeat().getReturn() : poseSeat.getSeat().getLocation().add(0d, 0.2d + (Tag.STAIRS.isTagged(poseSeat.getSeat().getBlock().getType()) ? ISitManager.STAIR_Y_OFFSET : 0d) - GPM.getCManager().S_SITMATERIALS.getOrDefault(poseSeat.getSeat().getBlock().getType(), 0d), 0d));

        if(!GPM.getCManager().GET_UP_RETURN) {
            returnLocation.setYaw(poseSeat.getSeat().getPlayer().getLocation().getYaw());
            returnLocation.setPitch(poseSeat.getSeat().getPlayer().getLocation().getPitch());
        }

        if(poseSeat.getSeat().getPlayer().isValid() && Safe) {

            GPM.getPlayerUtil().posEntity(poseSeat.getSeat().getPlayer(), returnLocation);
            GPM.getPlayerUtil().teleportEntity(poseSeat.getSeat().getPlayer(), returnLocation, true);
        }

        if(poseSeat.getSeat().getSeatEntity().isValid()) poseSeat.getSeat().getSeatEntity().remove();

        Bukkit.getPluginManager().callEvent(new PlayerGetUpPoseEvent(poseSeat, Reason));

        return true;
    }

}