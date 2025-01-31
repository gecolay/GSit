package dev.geco.gsit.api.event;

import dev.geco.gsit.object.GetUpReason;
import dev.geco.gsit.object.IGCrawl;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerGetUpCrawlEvent extends PlayerEvent {

    private final IGCrawl crawl;
    private final GetUpReason reason;
    private static final HandlerList handlers = new HandlerList();

    public PlayerGetUpCrawlEvent(@NotNull IGCrawl crawl, @NotNull GetUpReason reason) {
        super(crawl.getPlayer());
        this.crawl = crawl;
        this.reason = reason;
    }

    public @NotNull IGCrawl getCrawl() { return crawl; }

    public @NotNull GetUpReason getReason() { return reason; }

    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}