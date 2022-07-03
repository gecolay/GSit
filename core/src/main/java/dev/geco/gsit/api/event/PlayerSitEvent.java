package dev.geco.gsit.api.event;

import org.jetbrains.annotations.*;

import org.bukkit.event.*;
import org.bukkit.event.player.*;

import dev.geco.gsit.objects.*;

public class PlayerSitEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GSeat seat;

    public PlayerSitEvent(GSeat Seat) {

        super(Seat.getPlayer());

        seat = Seat;
    }

    public GSeat getSeat() { return seat; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}