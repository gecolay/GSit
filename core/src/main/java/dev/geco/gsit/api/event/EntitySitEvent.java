package dev.geco.gsit.api.event;

import org.jetbrains.annotations.*;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;

import dev.geco.gsit.objects.*;

public class EntitySitEvent extends EntityEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GSeat seat;

    public EntitySitEvent(GSeat Seat) {

        super(Seat.getEntity());

        seat = Seat;
    }

    public @NotNull LivingEntity getEntity() { return (LivingEntity) super.getEntity(); }

    public GSeat getSeat() { return seat; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}