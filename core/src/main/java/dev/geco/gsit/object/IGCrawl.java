package dev.geco.gsit.object;

import org.bukkit.entity.Player;

public interface IGCrawl {

    void start();

    void stop();

    Player getPlayer();

    long getLifetimeInNanoSeconds();

}