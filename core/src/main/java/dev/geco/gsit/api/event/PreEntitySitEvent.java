package dev.geco.gsit.api.event;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class PreEntitySitEvent extends EntityEvent implements Cancellable {

    private final Block block;
    private boolean cancel = false;
    private static final HandlerList handlers = new HandlerList();

    public PreEntitySitEvent(@NotNull LivingEntity entity, @NotNull Block block) {
        super(entity);
        this.block = block;
    }

    @Override
    public @NotNull LivingEntity getEntity() { return (LivingEntity) super.getEntity(); }

    public @NotNull Block getBlock() { return block; }

    @Override
    public boolean isCancelled() { return cancel; }

    @Override
    public void setCancelled(boolean cancelled) { cancel = cancelled; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}