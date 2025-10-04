package dev.geco.gsit.service;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.PlayerPlayerSitEvent;
import dev.geco.gsit.api.event.PlayerStopPlayerSitEvent;
import dev.geco.gsit.api.event.PrePlayerPlayerSitEvent;
import dev.geco.gsit.api.event.PrePlayerStopPlayerSitEvent;
import dev.geco.gsit.object.GStopReason;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerSitService {

    public static final String PLAYERSIT_ENTITY_TAG = GSitMain.NAME + "_PlayerSeatEntity";

    private final GSitMain gSitMain;
    private final int seatEntityStackCount;
    private final HashMap<UUID, AbstractMap.SimpleImmutableEntry<UUID, Set<UUID>>> bottomToTopStacks = new HashMap<>();
    private final HashMap<UUID, AbstractMap.SimpleImmutableEntry<UUID, Set<UUID>>> topToBottomStacks = new HashMap<>();
    private final Set<Player> preventDismountStackPlayers = new HashSet<>();
    private final HashMap<String, Long> spawnTimes = new HashMap<>();
    private int playerSitUsageCount = 0;
    private long playerSitUsageNanoTime = 0;

    public PlayerSitService(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        seatEntityStackCount = gSitMain.getVersionManager().isNewerOrVersion(20, 2) ? 1 : 2;
    }

    public int getSeatEntityStackCount() { return seatEntityStackCount; }

    public Set<Player> getPreventDismountStackPlayers() { return preventDismountStackPlayers; }

    public boolean isPlayerInPlayerSitStack(Player player) { return bottomToTopStacks.containsKey(player.getUniqueId()) || topToBottomStacks.containsKey(player.getUniqueId()); }

    public void removeAllPlayerSitStacks() {
        for(UUID topPlayerUuid : new ArrayList<>(topToBottomStacks.keySet())) {
            Player topPlayer = Bukkit.getPlayer(topPlayerUuid);
            if(topPlayer != null) stopPlayerSit(topPlayer, GStopReason.PLUGIN, false, true, true);
        }
        bottomToTopStacks.clear();
        topToBottomStacks.clear();
        preventDismountStackPlayers.clear();
    }

    public boolean sitOnPlayer(Player player, Player target) {
        if(!gSitMain.getEntityUtil().isPlayerSitLocationValid(target.getLocation())) return false;

        PrePlayerPlayerSitEvent prePlayerPlayerSitEvent = new PrePlayerPlayerSitEvent(player, target);
        Bukkit.getPluginManager().callEvent(prePlayerPlayerSitEvent);
        if(prePlayerPlayerSitEvent.isCancelled()) return false;

        Set<UUID> playerSeatEntityIds = gSitMain.getEntityUtil().createPlayerSeatEntities(player, target);
        if(gSitMain.getConfigService().CUSTOM_MESSAGE) gSitMain.getMessageService().sendActionBarMessage(player, "Messages.action-playersit-info");
        playerSitUsageCount++;
        bottomToTopStacks.put(target.getUniqueId(), new AbstractMap.SimpleImmutableEntry<>(player.getUniqueId(), playerSeatEntityIds));
        topToBottomStacks.put(player.getUniqueId(), new AbstractMap.SimpleImmutableEntry<>(target.getUniqueId(), playerSeatEntityIds));
        Bukkit.getPluginManager().callEvent(new PlayerPlayerSitEvent(player, target));
        spawnTimes.put(target.getUniqueId().toString() + player.getUniqueId(), System.nanoTime());

        return true;
    }

    public boolean stopPlayerSit(Player source, GStopReason stopReason) { return stopPlayerSit(source, stopReason, true, true, true); }

    public boolean stopPlayerSit(Player source, GStopReason stopReason, boolean removePassengers, boolean removeVehicle, boolean callPreEvent) {
        AbstractMap.SimpleImmutableEntry<UUID, Set<UUID>> passengers = removePassengers ? bottomToTopStacks.get(source.getUniqueId()) : null;
        AbstractMap.SimpleImmutableEntry<UUID, Set<UUID>> vehicles = removeVehicle ? topToBottomStacks.get(source.getUniqueId()) : null;
        if(passengers == null && vehicles == null) return true;

        if(callPreEvent) {
            PrePlayerStopPlayerSitEvent prePlayerStopPlayerSitEvent = new PrePlayerStopPlayerSitEvent(source, stopReason);
            Bukkit.getPluginManager().callEvent(prePlayerStopPlayerSitEvent);
            if(prePlayerStopPlayerSitEvent.isCancelled() && stopReason.isCancellable()) return false;
        }

        if(passengers != null) {
            source.eject();
            bottomToTopStacks.remove(source.getUniqueId());
            topToBottomStacks.remove(passengers.getKey());
            for(UUID passenger : passengers.getValue()) {
                Entity passengerEntity = Bukkit.getEntity(passenger);
                if(passengerEntity == null) continue;
                passengerEntity.remove();
            }
            String key = source.getUniqueId().toString() + passengers.getKey();
            Long spawnTime = spawnTimes.get(key);
            if(spawnTime != null) {
                playerSitUsageNanoTime += System.nanoTime() - spawnTime;
                spawnTimes.remove(key);
            }
        }

        if(vehicles != null) {
            source.leaveVehicle();
            topToBottomStacks.remove(source.getUniqueId());
            bottomToTopStacks.remove(vehicles.getKey());
            for(UUID vehicle : vehicles.getValue()) {
                Entity vehicleEntity = Bukkit.getEntity(vehicle);
                if(vehicleEntity == null) continue;
                vehicleEntity.remove();
            }
            String key = vehicles.getKey().toString() + source.getUniqueId();
            Long spawnTime = spawnTimes.get(key);
            if(spawnTime != null) {
                playerSitUsageNanoTime += System.nanoTime() - spawnTime;
                spawnTimes.remove(key);
            }
        }

        Bukkit.getPluginManager().callEvent(new PlayerStopPlayerSitEvent(source, stopReason));

        return true;
    }

    public int getPlayerSitUsageCount() { return playerSitUsageCount; }

    public long getPlayerSitUsageTimeInSeconds() { return playerSitUsageNanoTime / 1_000_000_000; }

    public void resetPlayerSitUsageStats() {
        spawnTimes.clear();
        playerSitUsageCount = 0;
        playerSitUsageNanoTime = 0;
    }

}