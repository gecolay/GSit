package dev.geco.gsit.api.event;

import dev.geco.gsit.object.GetUpReason;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerGetUpPlayerSitEvent extends PlayerEvent {

    private final GetUpReason reason;
    private static final HandlerList handlers = new HandlerList();

    public PlayerGetUpPlayerSitEvent(@NotNull Player player, @NotNull GetUpReason reason) {
        super(player);
        this.reason = reason;
    }

    public @NotNull GetUpReason getReason() { return reason; }

    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}