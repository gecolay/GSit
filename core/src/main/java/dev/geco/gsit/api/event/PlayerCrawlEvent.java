package dev.geco.gsit.api.event;

import org.jetbrains.annotations.*;

import org.bukkit.event.*;
import org.bukkit.event.player.*;

import dev.geco.gsit.objects.*;

public class PlayerCrawlEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final IGCrawl crawl;

    public PlayerCrawlEvent(IGCrawl Crawl) {

        super(Crawl.getPlayer());

        crawl = Crawl;
    }

    public IGCrawl getCrawl() { return crawl; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}