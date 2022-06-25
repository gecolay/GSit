package dev.geco.gsit.api.event;

import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.*;
import org.bukkit.event.*;

import dev.geco.gsit.objects.*;

public class PreEntityStopEmoteEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancel = false;

    private final LivingEntity e;

    private final GEmote g;

    public PreEntityStopEmoteEvent(LivingEntity Entity, GEmote Emote) {
        e = Entity;
        g = Emote;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public LivingEntity getEntity() { return e; }

    public GEmote getEmote() { return g; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}