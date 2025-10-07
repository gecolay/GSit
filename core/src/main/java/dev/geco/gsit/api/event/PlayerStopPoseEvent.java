package dev.geco.gsit.api.event;

import dev.geco.gsit.model.StopReason;
import dev.geco.gsit.model.Pose;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerStopPoseEvent extends PlayerEvent {

    private final Pose pose;
    private final StopReason reason;
    private static final HandlerList handlers = new HandlerList();

    public PlayerStopPoseEvent(@NotNull Pose pose, @NotNull StopReason reason) {
        super(pose.getPlayer());
        this.pose = pose;
        this.reason = reason;
    }

    public @NotNull Pose getPose() { return pose; }

    public @NotNull StopReason getReason() { return reason; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}