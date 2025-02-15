package dev.geco.gsit.api.event;

import dev.geco.gsit.GSitMain;
import org.bukkit.event.HandlerList;
import org.bukkit.event.server.PluginEvent;
import org.jetbrains.annotations.NotNull;

public class GSitLoadedEvent extends PluginEvent {

    private final GSitMain gSitMain;
    private static final HandlerList handlers = new HandlerList();

    public GSitLoadedEvent(@NotNull GSitMain gSitMain) {
        super(gSitMain);
        this.gSitMain = gSitMain;
    }

    @Override
    public @NotNull GSitMain getPlugin() { return gSitMain; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}