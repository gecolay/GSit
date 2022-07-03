package dev.geco.gsit.api.event;

import org.jetbrains.annotations.*;

import org.bukkit.event.*;
import org.bukkit.event.player.*;

import dev.geco.gsit.objects.*;

public class PlayerGetUpSitEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GSeat seat;

    private final GetUpReason reason;

    public PlayerGetUpSitEvent(GSeat Seat, GetUpReason Reason) {

        super(Seat.getPlayer());

        seat = Seat;
        reason = Reason;
    }

    public GSeat getSeat() { return seat; }

    public GetUpReason getReason() { return reason; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}