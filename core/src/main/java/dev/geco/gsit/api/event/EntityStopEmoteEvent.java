package dev.geco.gsit.api.event;

import org.jetbrains.annotations.*;

import org.bukkit.event.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;

import dev.geco.gsit.objects.*;

public class EntityStopEmoteEvent extends EntityEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GEmote emote;

    public EntityStopEmoteEvent(LivingEntity Entity, GEmote Emote) {

        super(Entity);

        emote = Emote;
    }

    public @NotNull LivingEntity getEntity() { return (LivingEntity) super.getEntity(); }

    public GEmote getEmote() { return emote; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}