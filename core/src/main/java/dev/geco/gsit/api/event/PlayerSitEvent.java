package dev.geco.gsit.api.event;

import org.jetbrains.annotations.NotNull;

import org.bukkit.event.*;
import org.bukkit.event.player.PlayerEvent;

import dev.geco.gsit.objects.*;

public class PlayerSitEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GSeat s;

    public PlayerSitEvent(GSeat Seat) {
        super(Seat.getPlayer());
        s = Seat;
    }

    public GSeat getSeat() { return s; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}