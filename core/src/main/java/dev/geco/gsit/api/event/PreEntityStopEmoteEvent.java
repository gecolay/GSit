package dev.geco.gsit.api.event;

import org.jetbrains.annotations.*;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

import dev.geco.gsit.objects.*;

public class PreEntityStopEmoteEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancel = false;

    private final GEmote emote;

    public PreEntityStopEmoteEvent(Player Player, GEmote Emote) {

        super(Player);

        emote = Emote;
    }

    public boolean isCancelled() { return cancel; }

    public void setCancelled(boolean Cancel) { cancel = Cancel; }

    public GEmote getEmote() { return emote; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}