package dev.geco.gsit.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PrePlayerCrawlEvent extends PlayerEvent implements Cancellable {

    private boolean cancel = false;
    private static final HandlerList handlers = new HandlerList();

    public PrePlayerCrawlEvent(@NotNull Player player) {
        super(player);
    }

    @Override
    public boolean isCancelled() { return cancel; }

    @Override
    public void setCancelled(boolean cancelled) { cancel = cancelled; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}