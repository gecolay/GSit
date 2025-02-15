package dev.geco.gsit.api.event;

import dev.geco.gsit.object.IGPose;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerPoseEvent extends PlayerEvent {

    private final IGPose pose;
    private static final HandlerList handlers = new HandlerList();

    public PlayerPoseEvent(@NotNull IGPose pose) {
        super(pose.getPlayer());
        this.pose = pose;
    }

    public @NotNull IGPose getPose() { return pose; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}