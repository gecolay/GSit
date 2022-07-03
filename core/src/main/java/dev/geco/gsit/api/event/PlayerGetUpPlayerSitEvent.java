package dev.geco.gsit.api.event;

import org.jetbrains.annotations.*;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

import dev.geco.gsit.objects.*;

public class PlayerGetUpPlayerSitEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GetUpReason reason;

    public PlayerGetUpPlayerSitEvent(Player Player, GetUpReason Reason) {

        super(Player);

        reason = Reason;
    }

    public GetUpReason getReason() { return reason; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}