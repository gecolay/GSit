package dev.geco.gsit.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerPlayerSitEvent extends PlayerEvent {

    private final Player target;
    private static final HandlerList handlers = new HandlerList();

    public PlayerPlayerSitEvent(@NotNull Player player, @NotNull Player target) {
        super(player);
        this.target = target;
    }

    public @NotNull Player getTarget() { return target; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}