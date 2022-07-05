package dev.geco.gsit.api.event;

import org.jetbrains.annotations.*;

import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;

public class PreEntitySitEvent extends EntityEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancel = false;

    private final Block block;

    public PreEntitySitEvent(LivingEntity Entity, Block Block) {

        super(Entity);

        block = Block;
    }

    public boolean isCancelled() { return cancel; }

    public void setCancelled(boolean Cancel) { cancel = Cancel; }

    public @NotNull LivingEntity getEntity() { return (LivingEntity) super.getEntity(); }

    public Block getBlock() { return block; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}