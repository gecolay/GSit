package dev.geco.gsit.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PrePlayerPlayerSitEvent extends PlayerEvent implements Cancellable {

    private final Player target;
    private boolean cancel = false;
    private static final HandlerList handlers = new HandlerList();

    public PrePlayerPlayerSitEvent(@NotNull Player player, @NotNull Player target) {
        super(player);
        this.target = target;
    }

    public @NotNull Player getTarget() { return target; }

    @Override
    public boolean isCancelled() { return cancel; }

    @Override
    public void setCancelled(boolean cancelled) { cancel = cancelled; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}