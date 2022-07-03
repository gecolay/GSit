package dev.geco.gsit.api.event;

import org.jetbrains.annotations.*;

import org.bukkit.entity.*;
import org.bukkit.event.*;

import dev.geco.gsit.objects.*;

public class PreEntityEmoteEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancel = false;

    private final LivingEntity entity;

    private final GEmote emote;

    public PreEntityEmoteEvent(LivingEntity Entity, GEmote Emote) {

        entity = Entity;
        emote = Emote;
    }

    public boolean isCancelled() { return cancel; }

    public void setCancelled(boolean Cancel) { cancel = Cancel; }

    public LivingEntity getEntity() { return entity; }

    public GEmote getEmote() { return emote; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}