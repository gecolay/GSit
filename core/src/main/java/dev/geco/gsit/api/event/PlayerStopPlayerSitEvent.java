package dev.geco.gsit.api.event;

import dev.geco.gsit.object.GetUpReason;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerStopPlayerSitEvent extends PlayerEvent {

    private final GetUpReason reason;
    private final boolean removePassengers;
    private static final HandlerList handlers = new HandlerList();

    public PlayerStopPlayerSitEvent(@NotNull Player player, @NotNull GetUpReason reason, boolean removePassengers) {
        super(player);
        this.reason = reason;
        this.removePassengers = removePassengers;
    }

    public @NotNull GetUpReason getReason() { return reason; }

    public boolean shouldRemovePassengers() { return removePassengers; }

    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}