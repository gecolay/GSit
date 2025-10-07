package dev.geco.gsit.model;

import org.bukkit.entity.Player;

public interface Crawl {

    void start();

    void stop();

    Player getPlayer();

    long getLifetimeInNanoSeconds();

}