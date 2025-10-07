package dev.geco.gsit.model;

import org.bukkit.entity.Pose;

public enum PoseType {

    LAY(Pose.SLEEPING),
    LAY_BACK(Pose.SLEEPING),
    BELLYFLOP(Pose.SWIMMING),
    SPIN(Pose.SPIN_ATTACK);

    private final Pose playerPose;

    PoseType(Pose playerPose) {
        this.playerPose = playerPose;
    }

    public Pose getPlayerPose() {
        return playerPose;
    }

}