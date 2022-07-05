package dev.geco.gsit.api.event;

import org.jetbrains.annotations.*;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;

import dev.geco.gsit.objects.*;

public class PreEntityGetUpSitEvent extends EntityEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancel = false;

    private final GSeat seat;

    private final GetUpReason reason;

    public PreEntityGetUpSitEvent(GSeat Seat, GetUpReason Reason) {

        super(Seat.getEntity());

        seat = Seat;
        reason = Reason;
    }

    public boolean isCancelled() { return cancel; }

    public void setCancelled(boolean Cancel) { cancel = Cancel; }

    public @NotNull LivingEntity getEntity() { return (LivingEntity) super.getEntity(); }

    public GSeat getSeat() { return seat; }

    public GetUpReason getReason() { return reason; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}