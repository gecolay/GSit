package dev.geco.gsit.api.event;

import org.jetbrains.annotations.*;

import org.bukkit.event.*;
import org.bukkit.event.player.*;

import dev.geco.gsit.objects.*;

public class PlayerGetUpPoseEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final IGPoseSeat poseSeat;

    private final GetUpReason reason;

    public PlayerGetUpPoseEvent(IGPoseSeat PoseSeat, GetUpReason Reason) {

        super(PoseSeat.getPlayer());

        poseSeat = PoseSeat;
        reason = Reason;
    }

    public IGPoseSeat getPoseSeat() { return poseSeat; }

    public GetUpReason getReason() { return reason; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}