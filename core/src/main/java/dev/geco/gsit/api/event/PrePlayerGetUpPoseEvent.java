package dev.geco.gsit.api.event;

import org.jetbrains.annotations.*;

import org.bukkit.event.*;
import org.bukkit.event.player.*;

import dev.geco.gsit.objects.*;

public class PrePlayerGetUpPoseEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancel = false;

    private final IGPoseSeat poseSeat;

    private final GetUpReason reason;

    public PrePlayerGetUpPoseEvent(IGPoseSeat PoseSeat, GetUpReason Reason) {

        super(PoseSeat.getPlayer());

        poseSeat = PoseSeat;
        reason = Reason;
    }

    public boolean isCancelled() { return cancel; }

    public void setCancelled(boolean Cancel) { cancel = Cancel; }

    public IGPoseSeat getPoseSeat() { return poseSeat; }

    public GetUpReason getReason() { return reason; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}