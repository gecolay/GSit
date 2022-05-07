package dev.geco.gsit.api.event;

import org.jetbrains.annotations.NotNull;

import org.bukkit.event.*;
import org.bukkit.event.player.PlayerEvent;

import dev.geco.gsit.objects.*;

public class PlayerCrawlEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final IGCrawl c;

    public PlayerCrawlEvent(IGCrawl Crawl) {
        super(Crawl.getPlayer());
        c = Crawl;
    }

    public IGCrawl getCrawl() { return c; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}