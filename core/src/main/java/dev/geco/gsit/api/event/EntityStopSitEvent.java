package dev.geco.gsit.api.event;

import dev.geco.gsit.model.Seat;
import dev.geco.gsit.model.StopReason;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class EntityStopSitEvent extends EntityEvent {

    private final Seat seat;
    private final StopReason reason;
    private static final HandlerList handlers = new HandlerList();

    public EntityStopSitEvent(@NotNull Seat seat, @NotNull StopReason reason) {
        super(seat.getEntity());
        this.seat = seat;
        this.reason = reason;
    }

    @Override
    public @NotNull LivingEntity getEntity() { return (LivingEntity) super.getEntity(); }

    public @NotNull Seat getSeat() { return seat; }

    public @NotNull StopReason getReason() { return reason; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}