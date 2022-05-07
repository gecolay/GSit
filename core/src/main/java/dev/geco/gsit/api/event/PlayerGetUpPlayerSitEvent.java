package dev.geco.gsit.api.event;

import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerEvent;

import dev.geco.gsit.objects.*;

public class PlayerGetUpPlayerSitEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GetUpReason r;

    public PlayerGetUpPlayerSitEvent(Player Player, GetUpReason Reason) {
        super(Player);
        r = Reason;
    }

    public GetUpReason getReason() { return r; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}