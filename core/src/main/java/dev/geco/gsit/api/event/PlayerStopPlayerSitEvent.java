package dev.geco.gsit.api.event;

import dev.geco.gsit.object.GStopReason;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerStopPlayerSitEvent extends PlayerEvent {

    private final GStopReason reason;
    private final boolean removePassengers;
    private static final HandlerList handlers = new HandlerList();

    public PlayerStopPlayerSitEvent(@NotNull Player player, @NotNull GStopReason reason, boolean removePassengers) {
        super(player);
        this.reason = reason;
        this.removePassengers = removePassengers;
    }

    public @NotNull GStopReason getReason() { return reason; }

    public boolean shouldRemovePassengers() { return removePassengers; }

    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}