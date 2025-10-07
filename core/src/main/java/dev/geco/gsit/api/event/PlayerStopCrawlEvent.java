package dev.geco.gsit.api.event;

import dev.geco.gsit.model.StopReason;
import dev.geco.gsit.model.Crawl;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerStopCrawlEvent extends PlayerEvent {

    private final Crawl crawl;
    private final StopReason reason;
    private static final HandlerList handlers = new HandlerList();

    public PlayerStopCrawlEvent(@NotNull Crawl crawl, @NotNull StopReason reason) {
        super(crawl.getPlayer());
        this.crawl = crawl;
        this.reason = reason;
    }

    public @NotNull Crawl getCrawl() { return crawl; }

    public @NotNull StopReason getReason() { return reason; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}