package dev.geco.gsit.api.event;

import dev.geco.gsit.object.GStopReason;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerStopPlayerSitEvent extends PlayerEvent {

    private final GStopReason reason;
    private static final HandlerList handlers = new HandlerList();

    public PlayerStopPlayerSitEvent(@NotNull Player player, @NotNull GStopReason reason) {
        super(player);
        this.reason = reason;
    }

    public @NotNull GStopReason getReason() { return reason; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}