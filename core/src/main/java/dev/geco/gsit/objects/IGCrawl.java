package dev.geco.gsit.objects;

import org.bukkit.entity.*;

public interface IGCrawl {

    void start();

    void stop();

    Player getPlayer();

    long getNano();

}