package dev.geco.gsit.manager;

import org.bukkit.entity.*;

import dev.geco.gsit.objects.*;

public interface IPlayerSitManager {

    int getFeatureUsedCount();

    void resetFeatureUsedCount();

    boolean sitOnPlayer(Player Player, Player Target);

    boolean stopPlayerSit(Entity Entity, GetUpReason Reason);

}