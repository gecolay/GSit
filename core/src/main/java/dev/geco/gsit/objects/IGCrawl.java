package dev.geco.gsit.objects;

import org.bukkit.entity.Player;

public interface IGCrawl {

    void start();

    void stop();

    Player getPlayer();

    long getNano();

}