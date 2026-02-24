package dev.geco.gsit.model;

public enum StopReason {

    BLOCK_BREAK(true, false),
    DAMAGE(true, false),
    DEATH(false, false),
    ENVIRONMENT(true, false),
    GAMEMODE_CHANGE(false, false),
    GET_UP(true, false),
    KICKED(true, false),
    PLUGIN(false, true),
    REGION(true, false),
    TELEPORT(false, false),
    DISCONNECT(false, false);

    private final boolean cancellable;
    private final boolean useEntityTask;

    StopReason(boolean cancellable, boolean useEntityTask) {
        this.cancellable = cancellable;
        this.useEntityTask = useEntityTask;
    }

    public boolean isCancellable() {
        return cancellable;
    }

    public boolean isUsingEntityTask() {
        return useEntityTask;
    }

}