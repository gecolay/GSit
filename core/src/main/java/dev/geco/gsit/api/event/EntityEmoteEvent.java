package dev.geco.gsit.api.event;

import org.jetbrains.annotations.NotNull;

import org.bukkit.event.*;
import org.bukkit.entity.*;

import dev.geco.gsit.objects.*;

public class EntityEmoteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final LivingEntity e;

    private final GEmote g;

    public EntityEmoteEvent(LivingEntity Entity, GEmote Emote) {
        e = Entity;
        g = Emote;
    }

    public LivingEntity getEntity() { return e; }

    public GEmote getEmote() { return g; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}