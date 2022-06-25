package dev.geco.gsit.api.event;

import org.jetbrains.annotations.NotNull;

import org.bukkit.event.*;
import org.bukkit.event.player.*;

import dev.geco.gsit.objects.*;

public class PlayerGetUpPoseEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final IGPoseSeat p;

    private final GetUpReason r;

    public PlayerGetUpPoseEvent(IGPoseSeat PoseSeat, GetUpReason Reason) {
        super(PoseSeat.getSeat().getPlayer());
        p = PoseSeat;
        r = Reason;
    }

    public IGPoseSeat getPoseSeat() { return p; }

    public GetUpReason getReason() { return r; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}