package dev.geco.gsit.api.event;

import dev.geco.gsit.objects.GetUpReason;
import dev.geco.gsit.objects.IGPose;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerGetUpPoseEvent extends PlayerEvent {

    private final IGPose pose;
    private final GetUpReason reason;
    private static final HandlerList handlers = new HandlerList();

    public PlayerGetUpPoseEvent(@NotNull IGPose pose, @NotNull GetUpReason reason) {
        super(pose.getPlayer());
        this.pose = pose;
        this.reason = reason;
    }

    public @NotNull IGPose getPose() { return pose; }

    public @NotNull GetUpReason getReason() { return reason; }

    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}