package dev.geco.gsit.object;

import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

public interface IGPose {

    void spawn();

    void remove();

    GSeat getSeat();

    Player getPlayer();

    Pose getPose();

}