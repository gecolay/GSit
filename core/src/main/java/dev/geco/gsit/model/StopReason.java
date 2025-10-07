package dev.geco.gsit.model;

public enum StopReason {

    BLOCK_BREAK(true),
    DAMAGE(true),
    DEATH(false),
    ENVIRONMENT(true),
    GAMEMODE_CHANGE(false),
    GET_UP(true),
    KICKED(true),
    PLUGIN(false),
    REGION(true),
    TELEPORT(false),
    DISCONNECT(false);

    private final boolean cancellable;

    StopReason(boolean cancellable) {
        this.cancellable = cancellable;
    }

    public boolean isCancellable() {
        return cancellable;
    }

}