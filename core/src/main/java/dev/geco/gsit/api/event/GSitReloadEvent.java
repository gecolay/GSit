package dev.geco.gsit.api.event;

import dev.geco.gsit.GSitMain;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.server.PluginEvent;
import org.jetbrains.annotations.NotNull;

public class GSitReloadEvent extends PluginEvent implements Cancellable {

    private final GSitMain gSitMain;
    private boolean cancel = false;
    private static final HandlerList handlers = new HandlerList();

    public GSitReloadEvent(@NotNull GSitMain gSitMain) {
        super(gSitMain);
        this.gSitMain = gSitMain;
    }

    public @NotNull GSitMain getPlugin() { return gSitMain; }

    public boolean isCancelled() { return cancel; }

    public void setCancelled(boolean cancelled) { cancel = cancelled; }

    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}