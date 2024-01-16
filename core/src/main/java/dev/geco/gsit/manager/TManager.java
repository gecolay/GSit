package dev.geco.gsit.manager;

import java.util.*;
import java.util.concurrent.*;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.*;

import io.papermc.paper.threadedregions.scheduler.*;

import dev.geco.gsit.GSitMain;

public class TManager {

    private final GSitMain GPM;

    public TManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    private final HashMap<UUID, Object> tasks = new HashMap<>();

    public List<UUID> getTasks() { return new ArrayList<>(tasks.keySet()); }

    public UUID run(Callback Call) { return run(Call, true, null, null); }

    public UUID run(Callback Call, boolean Sync) { return run(Call, Sync, null, null); }

    public UUID run(Callback Call, Entity Entity) { return run(Call, true, Entity, null); }

    public UUID run(Callback Call, boolean Sync, Entity Entity) { return run(Call, Sync, Entity, null); }

    public UUID run(Callback Call, Location Location) { return run(Call, true, null, Location); }

    public UUID run(Callback Call, boolean Sync, Location Location) { return run(Call, Sync, null, Location); }

    private UUID run(Callback Call, boolean Sync, Entity Entity, Location Location) {
        UUID uuid = UUID.randomUUID();
        if(GPM.supportsTaskFeature()) {
            if(Entity != null) {
                tasks.put(uuid, Entity.getScheduler().run(GPM, scheduledTask -> {
                    Call.call();
                    tasks.remove(uuid);
                }, null));
                return uuid;
            }
            ScheduledTask task;
            if(Location != null) task = Bukkit.getRegionScheduler().run(GPM, Location, scheduledTask -> {
                Call.call();
                tasks.remove(uuid);
            });
            else if(Sync) task = Bukkit.getGlobalRegionScheduler().run(GPM, scheduledTask -> {
                Call.call();
                tasks.remove(uuid);
            });
            else task = Bukkit.getAsyncScheduler().runNow(GPM, scheduledTask -> {
                    Call.call();
                    tasks.remove(uuid);
                });
            tasks.put(uuid, task);
        } else {
            BukkitRunnable task = new BukkitRunnable() {
                public void run() {
                    Call.call();
                    tasks.remove(uuid);
                }
            };
            tasks.put(uuid, task);
            if(Sync) task.runTask(GPM);
            else task.runTaskAsynchronously(GPM);
        }
        return uuid;
    }

    public UUID runDelayed(Callback Call, long Ticks) { return runDelayed(Call, true, null, null, Ticks); }

    public UUID runDelayed(Callback Call, boolean Sync, long Ticks) { return runDelayed(Call, Sync, null, null, Ticks); }

    public UUID runDelayed(Callback Call, Entity Entity, long Ticks) { return runDelayed(Call, true, Entity, null, Ticks); }

    public UUID runDelayed(Callback Call, boolean Sync, Entity Entity, long Ticks) { return runDelayed(Call, Sync, Entity, null, Ticks); }

    public UUID runDelayed(Callback Call, Location Location, long Ticks) { return runDelayed(Call, true, null, Location, Ticks); }

    public UUID runDelayed(Callback Call, boolean Sync, Location Location, long Ticks) { return runDelayed(Call, Sync, null, Location, Ticks); }

    private UUID runDelayed(Callback Call, boolean Sync, Entity Entity, Location Location, long Ticks) {
        UUID uuid = UUID.randomUUID();
        if(GPM.supportsTaskFeature()) {
            if(Ticks <= 0) return run(Call, Sync, Entity);
            if(Entity != null) {
                tasks.put(uuid, Entity.getScheduler().runDelayed(GPM, scheduledTask -> {
                    Call.call();
                    tasks.remove(uuid);
                }, null, Ticks));
                return uuid;
            }
            ScheduledTask task;
            if(Location != null) task = Bukkit.getRegionScheduler().runDelayed(GPM, Location, scheduledTask -> {
                Call.call();
                tasks.remove(uuid);
            }, Ticks);
            else if(Sync) task = Bukkit.getGlobalRegionScheduler().runDelayed(GPM, scheduledTask -> {
                Call.call();
                tasks.remove(uuid);
            }, Ticks);
            else task = Bukkit.getAsyncScheduler().runDelayed(GPM, scheduledTask -> {
                    Call.call();
                    tasks.remove(uuid);
                }, Ticks * 50, TimeUnit.MILLISECONDS);
            tasks.put(uuid, task);
        } else {
            BukkitRunnable task = new BukkitRunnable() {
                public void run() {
                    Call.call();
                    tasks.remove(uuid);
                }
            };
            tasks.put(uuid, task);
            if(Sync) task.runTaskLater(GPM, Ticks);
            else task.runTaskLaterAsynchronously(GPM, Ticks);
        }
        return uuid;
    }

    public UUID runAtFixedRate(Callback Call, long Delay, long Ticks) { return runAtFixedRate(Call, true, null, null, Delay, Ticks); }

    public UUID runAtFixedRate(Callback Call, boolean Sync, long Delay, long Ticks) { return runAtFixedRate(Call, Sync, null, null, Delay, Ticks); }

    public UUID runAtFixedRate(Callback Call, Entity Entity, long Delay, long Ticks) { return runAtFixedRate(Call, true, Entity, null, Delay, Ticks); }

    public UUID runAtFixedRate(Callback Call, boolean Sync, Entity Entity, long Delay, long Ticks) { return runAtFixedRate(Call, Sync, Entity, null, Delay, Ticks); }

    public UUID runAtFixedRate(Callback Call, Location Location, long Delay, long Ticks) { return runAtFixedRate(Call, true, null, Location, Delay, Ticks); }

    public UUID runAtFixedRate(Callback Call, boolean Sync, Location Location, long Delay, long Ticks) { return runAtFixedRate(Call, Sync, null, Location, Delay, Ticks); }

    private UUID runAtFixedRate(Callback Call, boolean Sync, Entity Entity, Location Location, long Delay, long Ticks) {
        UUID uuid = UUID.randomUUID();
        if(GPM.supportsTaskFeature()) {
            if(Entity != null) {
                tasks.put(uuid, Entity.getScheduler().runAtFixedRate(GPM, scheduledTask -> { Call.call(); }, null, Delay <= 0 ? 1 : Delay, Ticks <= 0 ? 1 : Ticks));
                return uuid;
            }
            ScheduledTask task;
            if(Location != null) task = Bukkit.getRegionScheduler().runAtFixedRate(GPM, Location, scheduledTask -> { Call.call(); }, Delay <= 0 ? 1 : Delay, Ticks <= 0 ? 1 : Ticks);
            else if(Sync) task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(GPM, scheduledTask -> { Call.call(); }, Delay <= 0 ? 1 : Delay, Ticks <= 0 ? 1 : Ticks);
            else task = Bukkit.getAsyncScheduler().runAtFixedRate(GPM, scheduledTask -> { Call.call(); }, Delay <= 0 ? 1 : Delay * 50, (Ticks <= 0 ? 1 : Ticks) * 50, TimeUnit.MILLISECONDS);
            tasks.put(uuid, task);
        } else {
            BukkitRunnable task = new BukkitRunnable() { public void run() { Call.call(); } };
            tasks.put(uuid, task);
            if(Sync) task.runTaskTimer(GPM, Delay, Ticks);
            else task.runTaskTimerAsynchronously(GPM, Delay, Ticks);
        }
        return uuid;
    }

    public void cancel(UUID Task) {
        if(!tasks.containsKey(Task)) return;
        Object task = tasks.get(Task);
        if(task instanceof BukkitRunnable) ((BukkitRunnable) task).cancel();
        else ((ScheduledTask) task).cancel();
        tasks.remove(Task);
    }

    public interface Callback { void call(); }

}