package dev.geco.gsit.api.event;

import dev.geco.gsit.object.IGCrawl;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerCrawlEvent extends PlayerEvent {

    private final IGCrawl crawl;
    private static final HandlerList handlers = new HandlerList();

    public PlayerCrawlEvent(@NotNull IGCrawl crawl) {
        super(crawl.getPlayer());
        this.crawl = crawl;
    }

    public @NotNull IGCrawl getCrawl() { return crawl; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}