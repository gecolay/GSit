package dev.geco.gsit.api.event;

import org.jetbrains.annotations.*;

import org.bukkit.event.*;
import org.bukkit.event.player.*;

import dev.geco.gsit.objects.*;

public class PlayerGetUpCrawlEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final IGCrawl crawl;

    private final GetUpReason reason;

    public PlayerGetUpCrawlEvent(IGCrawl Crawl, GetUpReason Reason) {

        super(Crawl.getPlayer());

        crawl = Crawl;
        reason = Reason;
    }

    public IGCrawl getCrawl() { return crawl; }

    public GetUpReason getReason() { return reason; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}