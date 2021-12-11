package dev.geco.gsit.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerEvent;

public class PrePlayerPlayerSitEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancel = false;

    private final Player t;

    public PrePlayerPlayerSitEvent(Player Player, Player Target) {
        super(Player);
        t = Target;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public Player getTarget() { return t; }

    public HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}