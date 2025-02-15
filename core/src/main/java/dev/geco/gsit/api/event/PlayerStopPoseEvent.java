package dev.geco.gsit.api.event;

import dev.geco.gsit.object.GStopReason;
import dev.geco.gsit.object.IGPose;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerStopPoseEvent extends PlayerEvent {

    private final IGPose pose;
    private final GStopReason reason;
    private static final HandlerList handlers = new HandlerList();

    public PlayerStopPoseEvent(@NotNull IGPose pose, @NotNull GStopReason reason) {
        super(pose.getPlayer());
        this.pose = pose;
        this.reason = reason;
    }

    public @NotNull IGPose getPose() { return pose; }

    public @NotNull GStopReason getReason() { return reason; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}