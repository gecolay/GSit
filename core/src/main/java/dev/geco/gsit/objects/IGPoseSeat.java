package dev.geco.gsit.objects;

import org.bukkit.entity.Pose;

public interface IGPoseSeat {

    void spawn();

    void remove();

    GSeat getSeat();

    Pose getPose();

}