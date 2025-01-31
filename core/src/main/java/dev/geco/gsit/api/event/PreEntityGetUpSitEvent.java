package dev.geco.gsit.api.event;

import dev.geco.gsit.object.GSeat;
import dev.geco.gsit.object.GetUpReason;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class PreEntityGetUpSitEvent extends EntityEvent implements Cancellable {

    private final GSeat seat;
    private final GetUpReason reason;
    private boolean cancel = false;
    private static final HandlerList handlers = new HandlerList();

    public PreEntityGetUpSitEvent(@NotNull GSeat seat, @NotNull GetUpReason reason) {
        super(seat.getEntity());
        this.seat = seat;
        this.reason = reason;
    }

    public boolean isCancelled() { return cancel; }

    public void setCancelled(boolean cancelled) { cancel = cancelled; }

    public @NotNull LivingEntity getEntity() { return (LivingEntity) super.getEntity(); }

    public @NotNull GSeat getSeat() { return seat; }

    public @NotNull GetUpReason getReason() { return reason; }

    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}