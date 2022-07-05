package dev.geco.gsit.api.event;

import org.jetbrains.annotations.*;

import org.bukkit.event.*;
import org.bukkit.event.player.*;

import dev.geco.gsit.objects.*;

public class PlayerPoseEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final IGPoseSeat poseSeat;

    public PlayerPoseEvent(IGPoseSeat PoseSeat) {

        super(PoseSeat.getPlayer());

        poseSeat = PoseSeat;
    }

    public IGPoseSeat getPoseSeat() { return poseSeat; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}