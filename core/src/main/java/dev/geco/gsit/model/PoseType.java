package dev.geco.gsit.model;

import org.bukkit.entity.Pose;

public enum PoseType {

    LAY(Pose.SLEEPING, "lay"),
    LAY_BACK(Pose.SLEEPING, "lay_back"),
    BELLYFLOP(Pose.SWIMMING, "bellyflop"),
    SPIN(Pose.SPIN_ATTACK, "spin");

    private final Pose playerPose;
    private final String name;

    PoseType(Pose playerPose, String name) {
        this.playerPose = playerPose;
        this.name = name;
    }

    public Pose getPlayerPose() { return playerPose; }

    public String getName() { return name; }

}