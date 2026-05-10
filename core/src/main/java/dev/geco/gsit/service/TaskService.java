package dev.geco.gsit.service;

import dev.geco.gsit.GSitMain;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TaskService {

    private final GSitMain gSitMain;
    private final HashMap<UUID, Object> tasks = new HashMap<>();

    public TaskService(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    public List<UUID> getTasks() { return new ArrayList<>(tasks.keySet()); }

    public void run(@NotNull Runnable runnable) { run(runnable, true, null, null); }

    public void run(@NotNull Runnable runnable, boolean sync) { run(runnable, sync, null, null); }

    public void run(@NotNull Runnable runnable, @Nullable Entity entity) { run(runnable, true, entity, null); }

    public void run(@NotNull Runnable runnable, boolean sync, @Nullable Entity entity) { run(runnable, sync, entity, null); }

    public void run(@NotNull Runnable runnable, @Nullable Location location) { run(runnable, true, null, location); }

    public void run(@NotNull Runnable runnable, boolean sync, @Nullable Location location) { run(runnable, sync, null, location); }

    private void run(@NotNull Runnable runnable, boolean sync, @Nullable Entity entity, @Nullable Location location) {
        if(!gSitMain.isEnabled()) return;
        if(gSitMain.supportsTaskFeature()) {
            if(entity != null) entity.getScheduler().run(gSitMain, scheduledTask -> runnable.run(), null);
            else if(location != null) Bukkit.getRegionScheduler().run(gSitMain, location, scheduledTask -> runnable.run());
            else if(sync) Bukkit.getGlobalRegionScheduler().run(gSitMain, scheduledTask -> runnable.run());
            else Bukkit.getAsyncScheduler().runNow(gSitMain, scheduledTask -> runnable.run());
        } else {
            if(sync) new BukkitRunnable() { public void run() { runnable.run(); } }.runTask(gSitMain);
            else new BukkitRunnable() { public void run() { runnable.run(); } }.runTaskAsynchronously(gSitMain);
        }
    }

    public void runDelayed(@NotNull Runnable runnable, long ticks) { runDelayed(runnable, true, null, null, ticks); }

    public void runDelayed(@NotNull Runnable runnable, boolean sync, long ticks) { runDelayed(runnable, sync, null, null, ticks); }

    public void runDelayed(@NotNull Runnable runnable, @Nullable Entity entity, long ticks) { runDelayed(runnable, true, entity, null, ticks); }

    public void runDelayed(@NotNull Runnable runnable, boolean sync, @Nullable Entity entity, long ticks) { runDelayed(runnable, sync, entity, null, ticks); }

    public void runDelayed(@NotNull Runnable runnable, @Nullable Location location, long ticks) { runDelayed(runnable, true, null, location, ticks); }

    public void runDelayed(@NotNull Runnable runnable, boolean sync, @Nullable Location location, long ticks) { runDelayed(runnable, sync, null, location, ticks); }

    private void runDelayed(@NotNull Runnable runnable, boolean sync, @Nullable Entity entity, @Nullable Location location, long ticks) {
        if(!gSitMain.isEnabled()) return;
        if(gSitMain.supportsTaskFeature()) {
            if(entity != null) entity.getScheduler().runDelayed(gSitMain, scheduledTask -> runnable.run(), null, ticks);
            else if(location != null) Bukkit.getRegionScheduler().runDelayed(gSitMain, location, scheduledTask -> runnable.run(), ticks);
            else if(sync) Bukkit.getGlobalRegionScheduler().runDelayed(gSitMain, scheduledTask -> runnable.run(), ticks);
            else Bukkit.getAsyncScheduler().runDelayed(gSitMain, scheduledTask -> runnable.run(), ticks * 50, TimeUnit.MILLISECONDS);
        } else {
            if(sync) new BukkitRunnable() { public void run() { runnable.run(); } }.runTaskLater(gSitMain, ticks);
            else new BukkitRunnable() { public void run() { runnable.run(); } }.runTaskLaterAsynchronously(gSitMain, ticks);
        }
    }

    public @Nullable UUID runAtFixedRate(@NotNull Runnable runnable, long delayTicks, long ticks) { return runAtFixedRate(runnable, true, null, null, delayTicks, ticks); }

    public @Nullable UUID runAtFixedRate(@NotNull Runnable runnable, boolean sync, long delayTicks, long ticks) { return runAtFixedRate(runnable, sync, null, null, delayTicks, ticks); }

    public @Nullable UUID runAtFixedRate(@NotNull Runnable runnable, @Nullable Entity entity, long delayTicks, long ticks) { return runAtFixedRate(runnable, true, entity, null, delayTicks, ticks); }

    public @Nullable UUID runAtFixedRate(@NotNull Runnable runnable, boolean sync, @Nullable Entity entity, long delayTicks, long ticks) { return runAtFixedRate(runnable, sync, entity, null, delayTicks, ticks); }

    public @Nullable UUID runAtFixedRate(@NotNull Runnable runnable, @Nullable Location location, long delayTicks, long ticks) { return runAtFixedRate(runnable, true, null, location, delayTicks, ticks); }

    public @Nullable UUID runAtFixedRate(@NotNull Runnable runnable, boolean sync, @Nullable Location location, long delayTicks, long ticks) { return runAtFixedRate(runnable, sync, null, location, delayTicks, ticks); }

    private @Nullable UUID runAtFixedRate(@NotNull Runnable runnable, boolean sync, @Nullable Entity entity, @Nullable Location location, long delayTicks, long ticks) {
        if(!gSitMain.isEnabled()) return null;
        UUID taskId = UUID.randomUUID();
        if(gSitMain.supportsTaskFeature()) {
            ScheduledTask task;
            if(entity != null) {
                task = entity.getScheduler().runAtFixedRate(gSitMain, scheduledTask -> runnable.run(), null, delayTicks <= 0 ? 1 : delayTicks, ticks <= 0 ? 1 : ticks);
                if(task == null) return null;
            }
            else if(location != null) task = Bukkit.getRegionScheduler().runAtFixedRate(gSitMain, location, scheduledTask -> runnable.run(), delayTicks <= 0 ? 1 : delayTicks, ticks <= 0 ? 1 : ticks);
            else if(sync) task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(gSitMain, scheduledTask -> runnable.run(), delayTicks <= 0 ? 1 : delayTicks, ticks <= 0 ? 1 : ticks);
            else task = Bukkit.getAsyncScheduler().runAtFixedRate(gSitMain, scheduledTask -> runnable.run(), delayTicks <= 0 ? 1 : delayTicks * 50, (ticks <= 0 ? 1 : ticks) * 50, TimeUnit.MILLISECONDS);
            tasks.put(taskId, task);
        } else {
            BukkitRunnable task = new BukkitRunnable() { public void run() { runnable.run(); } };
            tasks.put(taskId, task);
            if(sync) task.runTaskTimer(gSitMain, delayTicks, ticks);
            else task.runTaskTimerAsynchronously(gSitMain, delayTicks, ticks);
        }
        return taskId;
    }

    public void cancel(@Nullable UUID taskId) {
        if(!tasks.containsKey(taskId)) return;
        Object task = tasks.get(taskId);
        if(task instanceof BukkitRunnable) ((BukkitRunnable) task).cancel();
        else ((ScheduledTask) task).cancel();
        tasks.remove(taskId);
    }

}