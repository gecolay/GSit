package dev.geco.gsit.objects;

import org.bukkit.entity.*;

public interface IPlayerSitManager {

    int getFeatureUsedCount();

    void resetFeatureUsedCount();

    boolean sitOnPlayer(Player Player, Player Target);

    boolean stopPlayerSit(Entity Entity, GetUpReason Reason);

}