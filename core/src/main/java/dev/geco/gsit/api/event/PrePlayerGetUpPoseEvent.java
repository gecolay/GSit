package dev.geco.gsit.api.event;

import org.jetbrains.annotations.*;

import org.bukkit.event.*;
import org.bukkit.event.player.*;

import dev.geco.gsit.objects.*;

public class PrePlayerGetUpPoseEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancel = false;

    private final IGPoseSeat p;

    private final GetUpReason r;

    public PrePlayerGetUpPoseEvent(IGPoseSeat PoseSeat, GetUpReason Reason) {
        super(PoseSeat.getSeat().getPlayer());
        p = PoseSeat;
        r = Reason;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public IGPoseSeat getPoseSeat() { return p; }

    public GetUpReason getReason() { return r; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}