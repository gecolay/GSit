package dev.geco.gsit.api.event;

import dev.geco.gsit.object.GSeat;
import dev.geco.gsit.object.GetUpReason;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class EntityGetUpSitEvent extends EntityEvent {

    private final GSeat seat;
    private final GetUpReason reason;
    private static final HandlerList HANDLERS = new HandlerList();

    public EntityGetUpSitEvent(@NotNull GSeat seat, @NotNull GetUpReason reason) {
        super(seat.getEntity());
        this.seat = seat;
        this.reason = reason;
    }

    public @NotNull LivingEntity getEntity() { return (LivingEntity) super.getEntity(); }

    public @NotNull GSeat getSeat() { return seat; }

    public @NotNull GetUpReason getReason() { return reason; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static @NotNull HandlerList getHandlerList() { return HANDLERS; }

}