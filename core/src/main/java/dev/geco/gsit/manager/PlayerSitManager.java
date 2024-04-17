package dev.geco.gsit.manager;

import java.util.*;

import org.bukkit.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.*;
import dev.geco.gsit.objects.*;

public class PlayerSitManager {

    private final GSitMain GPM;

    private final HashMap<UUID, Long> spawnTimes = new HashMap<>();

    public final List<Player> WAIT_EJECT = new ArrayList<>();

    public PlayerSitManager(GSitMain GPluginMain) {
        GPM = GPluginMain;
        seat_entity_count = GPM.getSVManager().isNewerOrVersion(20, 2) ? 1 : 2;
    }

    private int playersit_used = 0;
    private long playersit_used_nano = 0;

    public int getPlayerSitUsedCount() { return playersit_used; }
    public long getPlayerSitUsedSeconds() { return playersit_used_nano / 1_000_000_000; }

    public void resetFeatureUsedCount() {
        playersit_used = 0;
        playersit_used_nano = 0;
    }

    private final int seat_entity_count;

    public int getSeatEntityCount() { return seat_entity_count; }

    public void clearSeats() {
        for(World world : Bukkit.getWorlds()) for(Entity entity : world.getEntities()) {
            try {
                if(entity.getScoreboardTags().contains(GPM.NAME + "_PlayerSeatEntity")) entity.remove();
            } catch (Throwable ignored) { }
        }
        spawnTimes.clear();
    }

    public boolean sitOnPlayer(Player Player, Player Target) {

        PrePlayerPlayerSitEvent preEvent = new PrePlayerPlayerSitEvent(Player, Target);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return false;

        if(!GPM.getEntityUtil().isPlayerSitLocationValid(Target)) return false;

        UUID lastUUID = GPM.getEntityUtil().createPlayerSeatEntity(Target, Player);

        if(GPM.getCManager().CUSTOM_MESSAGE) GPM.getMManager().sendActionBarMessage(Player, "Messages.action-playersit-info");

        playersit_used++;

        Bukkit.getPluginManager().callEvent(new PlayerPlayerSitEvent(Player, Target));

        if(lastUUID != null) spawnTimes.put(lastUUID, System.nanoTime());

        return true;
    }

    public boolean stopPlayerSit(Entity Entity, GetUpReason Reason) {

        if(Entity instanceof Player) {

            PrePlayerGetUpPlayerSitEvent preEvent = new PrePlayerGetUpPlayerSitEvent((Player) Entity, Reason);

            Bukkit.getPluginManager().callEvent(preEvent);

            if(preEvent.isCancelled()) return false;
        }

        removePassengers(Entity);

        removeVehicles(Entity);

        if(Entity.getScoreboardTags().contains(GPM.NAME + "_PlayerSeatEntity")) {
            long spawnTime = spawnTimes.getOrDefault(Entity.getUniqueId(), -1L);
            if(spawnTime != -1) {
                playersit_used_nano += System.nanoTime() - spawnTime;
                spawnTimes.remove(Entity.getUniqueId());
            }
            Entity.remove();
        }

        if(Entity instanceof Player) Bukkit.getPluginManager().callEvent(new PlayerGetUpPlayerSitEvent((Player) Entity, Reason));

        return true;
    }

    private void removePassengers(Entity Entity) {

        for(Entity passenger : Entity.getPassengers()) {

            if(!passenger.getScoreboardTags().contains(GPM.NAME + "_PlayerSeatEntity")) continue;

            removePassengers(passenger);

            long spawnTime = spawnTimes.getOrDefault(Entity.getUniqueId(), -1L);
            if(spawnTime != -1) {
                playersit_used_nano += System.nanoTime() - spawnTime;
                spawnTimes.remove(Entity.getUniqueId());
            }

            passenger.remove();
        }
    }

    private void removeVehicles(Entity Entity) {

        Entity vehicle = Entity.getVehicle();

        if(vehicle == null) return;

        if(!vehicle.getScoreboardTags().contains(GPM.NAME + "_PlayerSeatEntity")) return;

        removeVehicles(vehicle);

        long spawnTime = spawnTimes.getOrDefault(Entity.getUniqueId(), -1L);
        if(spawnTime != -1) {
            playersit_used_nano += System.nanoTime() - spawnTime;
            spawnTimes.remove(Entity.getUniqueId());
        }

        vehicle.remove();
    }

}