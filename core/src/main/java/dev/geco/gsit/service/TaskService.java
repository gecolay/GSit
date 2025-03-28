package dev.geco.gsit.service;

import dev.geco.gsit.GSitMain;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

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

    public UUID run(Runnable runnable) { return run(runnable, true, null, null); }

    public UUID run(Runnable runnable, boolean sync) { return run(runnable, sync, null, null); }

    public UUID run(Runnable runnable, Entity entity) { return run(runnable, true, entity, null); }

    public UUID run(Runnable runnable, boolean sync, Entity entity) { return run(runnable, sync, entity, null); }

    public UUID run(Runnable runnable, Location location) { return run(runnable, true, null, location); }

    public UUID run(Runnable runnable, boolean sync, Location location) { return run(runnable, sync, null, location); }

    private UUID run(Runnable runnable, boolean sync, Entity entity, Location location) {
        UUID taskId = UUID.randomUUID();
        if(!gSitMain.isEnabled()) return taskId;
        if(gSitMain.supportsTaskFeature()) {
            if(entity != null) {
                tasks.put(taskId, entity.getScheduler().run(gSitMain, scheduledTask -> {
                    runnable.run();
                    tasks.remove(taskId);
                }, null));
                return taskId;
            }
            ScheduledTask task;
            if(location != null) task = Bukkit.getRegionScheduler().run(gSitMain, location, scheduledTask -> {
                runnable.run();
                tasks.remove(taskId);
            });
            else if(sync) task = Bukkit.getGlobalRegionScheduler().run(gSitMain, scheduledTask -> {
                runnable.run();
                tasks.remove(taskId);
            });
            else task = Bukkit.getAsyncScheduler().runNow(gSitMain, scheduledTask -> {
                    runnable.run();
                    tasks.remove(taskId);
                });
            tasks.put(taskId, task);
        } else {
            BukkitRunnable task = new BukkitRunnable() {
                public void run() {
                    runnable.run();
                    tasks.remove(taskId);
                }
            };
            tasks.put(taskId, task);
            if(sync) task.runTask(gSitMain);
            else task.runTaskAsynchronously(gSitMain);
        }
        return taskId;
    }

    public UUID runDelayed(Runnable runnable, long ticks) { return runDelayed(runnable, true, null, null, ticks); }

    public UUID runDelayed(Runnable runnable, boolean sync, long ticks) { return runDelayed(runnable, sync, null, null, ticks); }

    public UUID runDelayed(Runnable runnable, Entity entity, long ticks) { return runDelayed(runnable, true, entity, null, ticks); }

    public UUID runDelayed(Runnable runnable, boolean sync, Entity entity, long ticks) { return runDelayed(runnable, sync, entity, null, ticks); }

    public UUID runDelayed(Runnable runnable, Location location, long ticks) { return runDelayed(runnable, true, null, location, ticks); }

    public UUID runDelayed(Runnable runnable, boolean sync, Location location, long ticks) { return runDelayed(runnable, sync, null, location, ticks); }

    private UUID runDelayed(Runnable runnable, boolean sync, Entity entity, Location location, long ticks) {
        UUID taskId = UUID.randomUUID();
        if(!gSitMain.isEnabled()) return taskId;
        if(gSitMain.supportsTaskFeature()) {
            if(ticks <= 0) return run(runnable, sync, entity);
            if(entity != null) {
                tasks.put(taskId, entity.getScheduler().runDelayed(gSitMain, scheduledTask -> {
                    runnable.run();
                    tasks.remove(taskId);
                }, null, ticks));
                return taskId;
            }
            ScheduledTask task;
            if(location != null) task = Bukkit.getRegionScheduler().runDelayed(gSitMain, location, scheduledTask -> {
                runnable.run();
                tasks.remove(taskId);
            }, ticks);
            else if(sync) task = Bukkit.getGlobalRegionScheduler().runDelayed(gSitMain, scheduledTask -> {
                runnable.run();
                tasks.remove(taskId);
            }, ticks);
            else task = Bukkit.getAsyncScheduler().runDelayed(gSitMain, scheduledTask -> {
                    runnable.run();
                    tasks.remove(taskId);
                }, ticks * 50, TimeUnit.MILLISECONDS);
            tasks.put(taskId, task);
        } else {
            BukkitRunnable task = new BukkitRunnable() {
                public void run() {
                    runnable.run();
                    tasks.remove(taskId);
                }
            };
            tasks.put(taskId, task);
            if(sync) task.runTaskLater(gSitMain, ticks);
            else task.runTaskLaterAsynchronously(gSitMain, ticks);
        }
        return taskId;
    }

    public UUID runAtFixedRate(Runnable runnable, long delayTicks, long ticks) { return runAtFixedRate(runnable, true, null, null, delayTicks, ticks); }

    public UUID runAtFixedRate(Runnable runnable, boolean sync, long delayTicks, long ticks) { return runAtFixedRate(runnable, sync, null, null, delayTicks, ticks); }

    public UUID runAtFixedRate(Runnable runnable, Entity entity, long delayTicks, long ticks) { return runAtFixedRate(runnable, true, entity, null, delayTicks, ticks); }

    public UUID runAtFixedRate(Runnable runnable, boolean sync, Entity entity, long delayTicks, long ticks) { return runAtFixedRate(runnable, sync, entity, null, delayTicks, ticks); }

    public UUID runAtFixedRate(Runnable runnable, Location location, long delayTicks, long ticks) { return runAtFixedRate(runnable, true, null, location, delayTicks, ticks); }

    public UUID runAtFixedRate(Runnable runnable, boolean sync, Location location, long delayTicks, long ticks) { return runAtFixedRate(runnable, sync, null, location, delayTicks, ticks); }

    private UUID runAtFixedRate(Runnable runnable, boolean sync, Entity entity, Location location, long delayTicks, long ticks) {
        UUID taskId = UUID.randomUUID();
        if(!gSitMain.isEnabled()) return taskId;
        if(gSitMain.supportsTaskFeature()) {
            if(entity != null) {
                tasks.put(taskId, entity.getScheduler().runAtFixedRate(gSitMain, scheduledTask -> { runnable.run(); }, null, delayTicks <= 0 ? 1 : delayTicks, ticks <= 0 ? 1 : ticks));
                return taskId;
            }
            ScheduledTask task;
            if(location != null) task = Bukkit.getRegionScheduler().runAtFixedRate(gSitMain, location, scheduledTask -> { runnable.run(); }, delayTicks <= 0 ? 1 : delayTicks, ticks <= 0 ? 1 : ticks);
            else if(sync) task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(gSitMain, scheduledTask -> { runnable.run(); }, delayTicks <= 0 ? 1 : delayTicks, ticks <= 0 ? 1 : ticks);
            else task = Bukkit.getAsyncScheduler().runAtFixedRate(gSitMain, scheduledTask -> { runnable.run(); }, delayTicks <= 0 ? 1 : delayTicks * 50, (ticks <= 0 ? 1 : ticks) * 50, TimeUnit.MILLISECONDS);
            tasks.put(taskId, task);
        } else {
            BukkitRunnable task = new BukkitRunnable() { public void run() { runnable.run(); } };
            tasks.put(taskId, task);
            if(sync) task.runTaskTimer(gSitMain, delayTicks, ticks);
            else task.runTaskTimerAsynchronously(gSitMain, delayTicks, ticks);
        }
        return taskId;
    }

    public void cancel(UUID taskId) {
        if(!tasks.containsKey(taskId)) return;
        Object task = tasks.get(taskId);
        if(task instanceof BukkitRunnable) ((BukkitRunnable) task).cancel();
        else ((ScheduledTask) task).cancel();
        tasks.remove(taskId);
    }

}