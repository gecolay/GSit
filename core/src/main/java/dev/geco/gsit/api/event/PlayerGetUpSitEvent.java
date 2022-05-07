package dev.geco.gsit.api.event;

import org.jetbrains.annotations.NotNull;

import org.bukkit.event.*;
import org.bukkit.event.player.PlayerEvent;

import dev.geco.gsit.objects.*;

public class PlayerGetUpSitEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GSeat s;

    private final GetUpReason r;

    public PlayerGetUpSitEvent(GSeat Seat, GetUpReason Reason) {
        super(Seat.getPlayer());
        s = Seat;
        r = Reason;
    }

    public GSeat getSeat() { return s; }

    public GetUpReason getReason() { return r; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}