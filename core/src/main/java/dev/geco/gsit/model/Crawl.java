package dev.geco.gsit.model;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Crawl {

    void start();

    void stop();

    @NotNull Player getPlayer();

    long getLifetimeInNanoSeconds();

}