package dev.geco.gsit.api.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerEvent;

public class PrePlayerSitEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancel = false;

    private final Block b;

    public PrePlayerSitEvent(Player Player, Block Block) {
        super(Player);
        b = Block;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public Block getBlock() { return b; }

    public HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}