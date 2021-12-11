package dev.geco.gsit.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerEvent;

public class PlayerPlayerSitEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player t;

    public PlayerPlayerSitEvent(Player Player, Player Target) {
        super(Player);
        t = Target;
    }

    public Player getTarget() { return t; }

    public HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}