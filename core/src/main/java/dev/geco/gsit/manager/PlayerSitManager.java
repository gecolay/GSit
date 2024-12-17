package dev.geco.gsit.manager;

import java.util.*;

import org.bukkit.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.*;
import dev.geco.gsit.objects.*;

public class PlayerSitManager {

    public final List<Player> WAIT_EJECT = new ArrayList<>();

    private final GSitMain GPM;
    private int playersit_used = 0;
    private long playersit_used_nano = 0;
    private final int seat_entity_count;
    private final HashMap<UUID, Long> spawnTimes = new HashMap<>();

    public PlayerSitManager(GSitMain GPluginMain) {
        GPM = GPluginMain;
        seat_entity_count = GPM.getSVManager().isNewerOrVersion(20, 2) ? 1 : 2;
    }

    public int getPlayerSitUsedCount() { return playersit_used; }

    public long getPlayerSitUsedSeconds() { return playersit_used_nano / 1_000_000_000; }

    public void resetFeatureUsedCount() {
        playersit_used = 0;
        playersit_used_nano = 0;
    }

    public int getSeatEntityCount() { return seat_entity_count; }

    public void clearSeats() {
        for(World world : Bukkit.getWorlds()) for(Entity entity : world.getEntities()) {
            try {
                if(entity.getScoreboardTags().contains(GSitMain.NAME + "_PlayerSeatEntity")) entity.remove();
            } catch (Throwable ignored) { }
        }
        spawnTimes.clear();
    }

    public boolean isUsingPlayerSit(Player Player) {
        if(Player.getVehicle() != null && Player.getVehicle().getScoreboardTags().contains(GSitMain.NAME + "_PlayerSeatEntity")) return true;
        return Player.getPassengers().stream().filter(passenger -> passenger.getScoreboardTags().contains(GSitMain.NAME + "_PlayerSeatEntity")).findFirst().orElse(null) != null;
    }

    public boolean sitOnPlayer(Player Player, Player Target) {

        if(!GPM.getEntityUtil().isPlayerSitLocationValid(Target)) return false;

        PrePlayerPlayerSitEvent preEvent = new PrePlayerPlayerSitEvent(Player, Target);
        Bukkit.getPluginManager().callEvent(preEvent);
        if(preEvent.isCancelled()) return false;

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

        if(Entity.getScoreboardTags().contains(GSitMain.NAME + "_PlayerSeatEntity")) {
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

            if(!passenger.getScoreboardTags().contains(GSitMain.NAME + "_PlayerSeatEntity")) continue;

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

        if(!vehicle.getScoreboardTags().contains(GSitMain.NAME + "_PlayerSeatEntity")) return;

        removeVehicles(vehicle);

        long spawnTime = spawnTimes.getOrDefault(Entity.getUniqueId(), -1L);
        if(spawnTime != -1) {
            playersit_used_nano += System.nanoTime() - spawnTime;
            spawnTimes.remove(Entity.getUniqueId());
        }

        vehicle.remove();
    }

}