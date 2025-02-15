package dev.geco.gsit.api.event;

import dev.geco.gsit.object.GSeat;
import dev.geco.gsit.object.GStopReason;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class EntityStopSitEvent extends EntityEvent {

    private final GSeat seat;
    private final GStopReason reason;
    private static final HandlerList handlers = new HandlerList();

    public EntityStopSitEvent(@NotNull GSeat seat, @NotNull GStopReason reason) {
        super(seat.getEntity());
        this.seat = seat;
        this.reason = reason;
    }

    @Override
    public @NotNull LivingEntity getEntity() { return (LivingEntity) super.getEntity(); }

    public @NotNull GSeat getSeat() { return seat; }

    public @NotNull GStopReason getReason() { return reason; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}