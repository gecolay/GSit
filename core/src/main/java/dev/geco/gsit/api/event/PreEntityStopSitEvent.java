package dev.geco.gsit.api.event;

import dev.geco.gsit.object.GSeat;
import dev.geco.gsit.object.GStopReason;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class PreEntityStopSitEvent extends EntityEvent implements Cancellable {

    private final GSeat seat;
    private final GStopReason reason;
    private boolean cancel = false;
    private static final HandlerList handlers = new HandlerList();

    public PreEntityStopSitEvent(@NotNull GSeat seat, @NotNull GStopReason reason) {
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

    public @NotNull GSeat getSeat() { return seat; }

    public @NotNull GStopReason getReason() { return reason; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}