package dev.geco.gsit.api.event;

import dev.geco.gsit.objects.GetUpReason;
import dev.geco.gsit.objects.IGCrawl;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PrePlayerGetUpCrawlEvent extends PlayerEvent implements Cancellable {

    private final IGCrawl crawl;
    private final GetUpReason reason;
    private boolean cancel = false;
    private static final HandlerList handlers = new HandlerList();

    public PrePlayerGetUpCrawlEvent(@NotNull IGCrawl crawl, @NotNull GetUpReason reason) {
        super(crawl.getPlayer());
        this.crawl = crawl;
        this.reason = reason;
    }

    public @NotNull IGCrawl getCrawl() { return crawl; }

    public @NotNull GetUpReason getReason() { return reason; }

    public boolean isCancelled() { return cancel; }

    public void setCancelled(boolean cancelled) { cancel = cancelled; }

    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}