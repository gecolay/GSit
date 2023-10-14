package dev.geco.gsit.api.event;

import org.jetbrains.annotations.*;

import org.bukkit.event.*;
import org.bukkit.entity.*;
import org.bukkit.event.player.*;

import dev.geco.gsit.objects.*;

public class EntityStopEmoteEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GEmote emote;

    public EntityStopEmoteEvent(Player Player, GEmote Emote) {

        super(Player);

        emote = Emote;
    }

    public GEmote getEmote() { return emote; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}