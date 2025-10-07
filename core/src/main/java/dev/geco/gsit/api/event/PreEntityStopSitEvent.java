package dev.geco.gsit.api.event;

import dev.geco.gsit.model.Seat;
import dev.geco.gsit.model.StopReason;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class PreEntityStopSitEvent extends EntityEvent implements Cancellable {

    private final Seat seat;
    private final StopReason reason;
    private boolean cancel = false;
    private static final HandlerList handlers = new HandlerList();

    public PreEntityStopSitEvent(@NotNull Seat seat, @NotNull StopReason reason) {
        super(seat.getEntity());
        this.seat = seat;
        this.reason = reason;
    }

    @Override
    public boolean isCancelled() { return cancel; }

    @Override
    public void setCancelled(boolean cancelled) { cancel = cancelled; }

    @Override
    public @NotNull LivingEntity getEntity() { return (LivingEntity) super.getEntity(); }

    public @NotNull Seat getSeat() { return seat; }

    public @NotNull StopReason getReason() { return reason; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}