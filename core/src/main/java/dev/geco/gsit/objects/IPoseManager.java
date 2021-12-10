package dev.geco.gsit.objects;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

public interface IPoseManager {

    boolean isPosing(Player Player);

    IGPoseSeat getPose(Player Player);

    void clearPoses();

    boolean kickPose(Block Block, Player Player);

    IGPoseSeat createPose(Block Block, Player Player, Pose Pose);

    IGPoseSeat createPose(Block Block, Player Player, Pose Pose, double XOffset, double YOffset, double ZOffset, float SeatRotation, boolean SitAtBlock);

    boolean removePose(IGPoseSeat PoseSeat, GetUpReason Reason);

    boolean removePose(IGPoseSeat PoseSeat, GetUpReason Reason, boolean Safe);

}