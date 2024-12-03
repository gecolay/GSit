package dev.geco.gsit.api.event;

import org.jetbrains.annotations.*;

import org.bukkit.event.*;
import org.bukkit.event.server.*;

import dev.geco.gsit.GSitMain;

public class GSitLoadedEvent extends PluginEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GSitMain GPM;

    public GSitLoadedEvent(GSitMain GPluginMain) {
        super(GPluginMain);
        GPM = GPluginMain;
    }

    public @NotNull GSitMain getPlugin() { return GPM; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}