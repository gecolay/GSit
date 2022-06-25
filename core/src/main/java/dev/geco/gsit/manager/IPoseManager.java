package dev.geco.gsit.manager;

import java.util.*;

import org.bukkit.block.*;
import org.bukkit.entity.*;

import dev.geco.gsit.objects.*;

public interface IPoseManager {

    int getFeatureUsedCount();

    void resetFeatureUsedCount();

    List<IGPoseSeat> getPoses();

    boolean isPosing(Player Player);

    IGPoseSeat getPose(Player Player);

    void clearPoses();

    boolean kickPose(Block Block, Player Player);

    IGPoseSeat createPose(Block Block, Player Player, Pose Pose);

    IGPoseSeat createPose(Block Block, Player Player, Pose Pose, double XOffset, double YOffset, double ZOffset, float SeatRotation, boolean SitAtBlock, boolean GetUpSneak);

    boolean removePose(IGPoseSeat PoseSeat, GetUpReason Reason);

    boolean removePose(IGPoseSeat PoseSeat, GetUpReason Reason, boolean Safe);

}