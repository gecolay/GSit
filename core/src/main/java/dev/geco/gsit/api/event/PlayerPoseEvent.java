package dev.geco.gsit.api.event;

import dev.geco.gsit.model.Pose;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerPoseEvent extends PlayerEvent {

    private final Pose pose;
    private static final HandlerList handlers = new HandlerList();

    public PlayerPoseEvent(@NotNull Pose pose) {
        super(pose.getPlayer());
        this.pose = pose;
    }

    public @NotNull Pose getPose() { return pose; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}