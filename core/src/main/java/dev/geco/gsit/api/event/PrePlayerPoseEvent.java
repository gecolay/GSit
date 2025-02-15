package dev.geco.gsit.api.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PrePlayerPoseEvent extends PlayerEvent implements Cancellable {

    private final Block block;
    private boolean cancel = false;
    private static final HandlerList handlers = new HandlerList();

    public PrePlayerPoseEvent(@NotNull Player player, @NotNull Block block) {
        super(player);
        this.block = block;
    }

    public @NotNull Block getBlock() { return block; }

    @Override
    public boolean isCancelled() { return cancel; }

    @Override
    public void setCancelled(boolean cancelled) { cancel = cancelled; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}