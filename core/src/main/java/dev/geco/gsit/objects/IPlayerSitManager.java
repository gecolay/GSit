package dev.geco.gsit.objects;

import org.bukkit.entity.Player;

public interface IPlayerSitManager {

    int getFeatureUsedCount();

    void resetFeatureUsedCount();

    boolean sitOnPlayer(Player Player, Player Target);

}