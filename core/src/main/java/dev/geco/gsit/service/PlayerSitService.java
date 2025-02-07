package dev.geco.gsit.service;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.PlayerPlayerSitEvent;
import dev.geco.gsit.api.event.PlayerStopPlayerSitEvent;
import dev.geco.gsit.api.event.PrePlayerPlayerSitEvent;
import dev.geco.gsit.api.event.PrePlayerStopPlayerSitEvent;
import dev.geco.gsit.object.GStopReason;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerSitService {

    public static final String PLAYERSIT_ENTITY_TAG = GSitMain.NAME + "_PlayerSeatEntity";

    private final GSitMain gSitMain;
    private final int seatEntityStackCount;
    private final HashMap<String, Long> spawnTimes = new HashMap<>();
    private final List<Player> preventDismountStackPlayers = new ArrayList<>();
    private int playerSitUsageCount = 0;
    private long playerSitUsageNanoTime = 0;

    public PlayerSitService(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        seatEntityStackCount = gSitMain.getVersionManager().isNewerOrVersion(20, 2) ? 1 : 2;
    }

    public int getSeatEntityStackCount() { return seatEntityStackCount; }

    public void removePlayerSitEntities() {
        try {
            for(World world : Bukkit.getWorlds()) for(Entity entity : world.getEntities()) if(entity.getScoreboardTags().contains(PLAYERSIT_ENTITY_TAG)) entity.remove();
        } catch(Throwable ignored) { }
        spawnTimes.clear();
    }

    public List<Player> getPreventDismountStackPlayers() { return preventDismountStackPlayers; }

    public boolean isPlayerInPlayerSitStack(Player player) {
        if(player.getVehicle() != null && player.getVehicle().getScoreboardTags().contains(PLAYERSIT_ENTITY_TAG)) return true;
        return player.getPassengers().stream().filter(passenger -> passenger.getScoreboardTags().contains(PLAYERSIT_ENTITY_TAG)).findFirst().orElse(null) != null;
    }

    public boolean sitOnPlayer(Player player, Player target) {
        if(!gSitMain.getEntityUtil().isPlayerSitLocationValid(target.getLocation())) return false;

        PrePlayerPlayerSitEvent prePlayerPlayerSitEvent = new PrePlayerPlayerSitEvent(player, target);
        Bukkit.getPluginManager().callEvent(prePlayerPlayerSitEvent);
        if(prePlayerPlayerSitEvent.isCancelled()) return false;

        if(!gSitMain.getEntityUtil().createPlayerSeatEntities(player, target)) return false;
        if(gSitMain.getConfigService().CUSTOM_MESSAGE) gSitMain.getMessageService().sendActionBarMessage(player, "Messages.action-playersit-info");
        playerSitUsageCount++;
        Bukkit.getPluginManager().callEvent(new PlayerPlayerSitEvent(player, target));
        spawnTimes.put(player.getUniqueId().toString() + target.getUniqueId(), System.nanoTime());

        return true;
    }

    public boolean stopPlayerSit(Player player, GStopReason stopReason) { return stopPlayerSit(player, stopReason, true, true, true); }

    public boolean stopPlayerSit(Player player, GStopReason stopReason, boolean removePassengers, boolean removeVehicle, boolean callPreEvent) {
        if(player.getPassengers().isEmpty() && player.getVehicle() == null) return true;

        if(callPreEvent) {
            PrePlayerStopPlayerSitEvent prePlayerStopPlayerSitEvent = new PrePlayerStopPlayerSitEvent(player, stopReason, removePassengers);
            Bukkit.getPluginManager().callEvent(prePlayerStopPlayerSitEvent);
            if(prePlayerStopPlayerSitEvent.isCancelled()) return false;
        }

        if(removePassengers) removePassengers(player, player);
        if(removeVehicle) removeVehicles(player, player);

        Bukkit.getPluginManager().callEvent(new PlayerStopPlayerSitEvent(player, stopReason, removePassengers));

        return true;
    }

    private void removePassengers(Entity entity, Player source) {
        for(Entity passenger : entity.getPassengers()) {
            if(passenger instanceof Player player) {
                finishStats(player, source);
                continue;
            }
            if(!passenger.getScoreboardTags().contains(PLAYERSIT_ENTITY_TAG)) continue;
            removePassengers(passenger, source);
            passenger.remove();
        }
    }

    private void removeVehicles(Entity entity, Player source) {
        Entity vehicle = entity.getVehicle();
        if(vehicle == null) return;
        if(vehicle instanceof Player player) {
            finishStats(player, source);
            return;
        }
        if(!vehicle.getScoreboardTags().contains(PLAYERSIT_ENTITY_TAG)) return;
        removeVehicles(vehicle, source);
        vehicle.remove();
    }

    private void finishStats(Player target, Player player) {
        String key = target.getUniqueId().toString() + player.getUniqueId();
        Long value = spawnTimes.get(key);
        if(value != null) {
            playerSitUsageNanoTime += System.nanoTime() - value;
            spawnTimes.remove(key);
            return;
        }
        key = player.getUniqueId().toString() + target.getUniqueId();
        value = spawnTimes.get(key);
        if(value == null) return;
        playerSitUsageNanoTime += System.nanoTime() - value;
        spawnTimes.remove(key);
    }

    public int getPlayerSitUsageCount() { return playerSitUsageCount; }

    public long getPlayerSitUsageTimeInSeconds() { return playerSitUsageNanoTime / 1_000_000_000; }

    public void resetPlayerSitUsageStats() {
        playerSitUsageCount = 0;
        playerSitUsageNanoTime = 0;
    }

}