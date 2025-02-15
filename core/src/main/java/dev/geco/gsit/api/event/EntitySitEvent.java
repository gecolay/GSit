package dev.geco.gsit.api.event;

import dev.geco.gsit.object.GSeat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class EntitySitEvent extends EntityEvent {

    private final GSeat seat;
    private static final HandlerList handlers = new HandlerList();

    public EntitySitEvent(@NotNull GSeat seat) {
        super(seat.getEntity());
        this.seat = seat;
    }

    @Override
    public @NotNull LivingEntity getEntity() { return (LivingEntity) super.getEntity(); }

    public @NotNull GSeat getSeat() { return seat; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}