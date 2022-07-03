package dev.geco.gsit.api.event;

import org.jetbrains.annotations.*;

import org.bukkit.event.*;
import org.bukkit.entity.*;

import dev.geco.gsit.objects.*;

public class EntityStopEmoteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final LivingEntity entity;

    private final GEmote emote;

    public EntityStopEmoteEvent(LivingEntity Entity, GEmote Emote) {

        entity = Entity;
        emote = Emote;
    }

    public LivingEntity getEntity() { return entity; }

    public GEmote getEmote() { return emote; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}