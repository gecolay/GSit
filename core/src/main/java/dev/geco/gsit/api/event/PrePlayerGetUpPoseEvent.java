package dev.geco.gsit.api.event;

import dev.geco.gsit.object.GetUpReason;
import dev.geco.gsit.object.IGPose;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PrePlayerGetUpPoseEvent extends PlayerEvent implements Cancellable {

    private final IGPose pose;
    private final GetUpReason reason;
    private boolean cancel = false;
    private static final HandlerList handlers = new HandlerList();

    public PrePlayerGetUpPoseEvent(@NotNull IGPose pose, @NotNull GetUpReason reason) {
        super(pose.getPlayer());
        this.pose = pose;
        this.reason = reason;
    }

    public @NotNull IGPose getPose() { return pose; }

    public @NotNull GetUpReason getReason() { return reason; }

    public boolean isCancelled() { return cancel; }

    public void setCancelled(boolean cancelled) { cancel = cancelled; }

    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}