package dev.geco.gsit.api.event;

import dev.geco.gsit.object.GStopReason;
import dev.geco.gsit.object.IGCrawl;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PrePlayerStopCrawlEvent extends PlayerEvent implements Cancellable {

    private final IGCrawl crawl;
    private final GStopReason reason;
    private boolean cancel = false;
    private static final HandlerList handlers = new HandlerList();

    public PrePlayerStopCrawlEvent(@NotNull IGCrawl crawl, @NotNull GStopReason reason) {
        super(crawl.getPlayer());
        this.crawl = crawl;
        this.reason = reason;
    }

    public @NotNull IGCrawl getCrawl() { return crawl; }

    public @NotNull GStopReason getReason() { return reason; }

    @Override
    public boolean isCancelled() { return cancel; }

    @Override
    public void setCancelled(boolean cancelled) { cancel = cancelled; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}