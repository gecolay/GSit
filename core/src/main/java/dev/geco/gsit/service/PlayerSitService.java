package dev.geco.gsit.service;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.PlayerGetUpPlayerSitEvent;
import dev.geco.gsit.api.event.PlayerPlayerSitEvent;
import dev.geco.gsit.api.event.PrePlayerGetUpPlayerSitEvent;
import dev.geco.gsit.api.event.PrePlayerPlayerSitEvent;
import dev.geco.gsit.object.GetUpReason;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerSitService {

    private final GSitMain gSitMain;
    private final int seatEntityStackCount;
    private final HashMap<UUID, Long> spawnTimes = new HashMap<>();
    private final List<Player> waitEject = new ArrayList<>();
    private int playerSitUsageCount = 0;
    private long playerSitUsageNanoTime = 0;

    public PlayerSitService(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        seatEntityStackCount = gSitMain.getVersionManager().isNewerOrVersion(20, 2) ? 1 : 2;
    }

    public int getSeatEntityStackCount() { return seatEntityStackCount; }

    public void removePlayerSitEntities() {
        for(World world : Bukkit.getWorlds()) for(Entity entity : world.getEntities()) {
            try {
                if(entity.getScoreboardTags().contains(GSitMain.NAME + "_PlayerSeatEntity")) entity.remove();
            } catch (Throwable ignored) { }
        }
        spawnTimes.clear();
    }

    public List<Player> getWaitEjectPlayers() { return waitEject; }

    public boolean isPlayerInPlayerSitStack(Player player) {
        if(player.getVehicle() != null && player.getVehicle().getScoreboardTags().contains(GSitMain.NAME + "_PlayerSeatEntity")) return true;
        return player.getPassengers().stream().filter(passenger -> passenger.getScoreboardTags().contains(GSitMain.NAME + "_PlayerSeatEntity")).findFirst().orElse(null) != null;
    }

    public boolean sitOnPlayer(Player player, Player target) {
        if(!gSitMain.getEntityUtil().isPlayerSitLocationValid(target.getLocation())) return false;

        PrePlayerPlayerSitEvent prePlayerPlayerSitEvent = new PrePlayerPlayerSitEvent(player, target);
        Bukkit.getPluginManager().callEvent(prePlayerPlayerSitEvent);
        if(prePlayerPlayerSitEvent.isCancelled()) return false;

        UUID topEntityUuid = gSitMain.getEntityUtil().createPlayerSeatEntity(target, player);
        if(gSitMain.getConfigService().CUSTOM_MESSAGE) gSitMain.getMessageService().sendActionBarMessage(player, "Messages.action-playersit-info");
        playerSitUsageCount++;
        Bukkit.getPluginManager().callEvent(new PlayerPlayerSitEvent(player, target));
        if(topEntityUuid != null) spawnTimes.put(topEntityUuid, System.nanoTime());

        return true;
    }

    public boolean stopPlayerSit(Entity entity, GetUpReason getUpReason) {
        if(entity instanceof Player) {
            PrePlayerGetUpPlayerSitEvent prePlayerGetUpPlayerSitEvent = new PrePlayerGetUpPlayerSitEvent((Player) entity, getUpReason);
            Bukkit.getPluginManager().callEvent(prePlayerGetUpPlayerSitEvent);
            if(prePlayerGetUpPlayerSitEvent.isCancelled()) return false;
        }

        removePassengers(entity);
        removeVehicles(entity);

        if(entity.getScoreboardTags().contains(GSitMain.NAME + "_PlayerSeatEntity")) {
            long spawnTime = spawnTimes.getOrDefault(entity.getUniqueId(), -1L);
            if(spawnTime != -1) {
                playerSitUsageNanoTime += System.nanoTime() - spawnTime;
                spawnTimes.remove(entity.getUniqueId());
            }
            entity.remove();
        }

        if(entity instanceof Player) Bukkit.getPluginManager().callEvent(new PlayerGetUpPlayerSitEvent((Player) entity, getUpReason));

        return true;
    }

    private void removePassengers(Entity entity) {
        for(Entity passenger : entity.getPassengers()) {
            if(!passenger.getScoreboardTags().contains(GSitMain.NAME + "_PlayerSeatEntity")) continue;

            removePassengers(passenger);

            long spawnTime = spawnTimes.getOrDefault(entity.getUniqueId(), -1L);
            if(spawnTime != -1) {
                playerSitUsageNanoTime += System.nanoTime() - spawnTime;
                spawnTimes.remove(entity.getUniqueId());
            }

            passenger.remove();
        }
    }

    private void removeVehicles(Entity entity) {
        Entity vehicle = entity.getVehicle();
        if(vehicle == null) return;

        if(!vehicle.getScoreboardTags().contains(GSitMain.NAME + "_PlayerSeatEntity")) return;

        removeVehicles(vehicle);

        long spawnTime = spawnTimes.getOrDefault(entity.getUniqueId(), -1L);
        if(spawnTime != -1) {
            playerSitUsageNanoTime += System.nanoTime() - spawnTime;
            spawnTimes.remove(entity.getUniqueId());
        }

        vehicle.remove();
    }

    public int getPlayerSitUsageCount() { return playerSitUsageCount; }

    public long getPlayerSitUsageTimeInSeconds() { return playerSitUsageNanoTime / 1_000_000_000; }

    public void resetPlayerSitUsageStats() {
        playerSitUsageCount = 0;
        playerSitUsageNanoTime = 0;
    }

}