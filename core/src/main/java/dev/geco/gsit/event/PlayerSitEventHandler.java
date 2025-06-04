package dev.geco.gsit.event;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.object.GStopReason;
import dev.geco.gsit.service.PlayerSitService;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class PlayerSitEventHandler implements Listener {

    private final GSitMain gSitMain;
    private final boolean sneakEjectsEnabled;
    private final boolean allowSit;
    private final boolean allowSitNPC;
    private final boolean emptyHandOnly;
    private final double maxDistance;
    private final int maxStack;
    private final boolean disableElytra;

    public PlayerSitEventHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        var config = gSitMain.getConfigService();
        this.sneakEjectsEnabled = config.PS_SNEAK_EJECTS;
        this.allowSit = config.PS_ALLOW_SIT;
        this.allowSitNPC = config.PS_ALLOW_SIT_NPC;
        this.emptyHandOnly = config.PS_EMPTY_HAND_ONLY;
        this.maxDistance = config.PS_MAX_DISTANCE;
        this.maxStack = config.PS_MAX_STACK;
        this.disableElytra = config.FEATUREFLAGS.contains("DISABLE_PLAYERSIT_ELYTRA");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerToggleSneakEvent(PlayerToggleSneakEvent event) {
        if (!sneakEjectsEnabled || !event.isSneaking()) return;
        
        Player player = event.getPlayer();
        if (player.isFlying() || 
            player.getVehicle() != null || 
            gSitMain.getPlayerSitService().getPreventDismountStackPlayers().contains(player) || 
            player.getPassengers().isEmpty()) return;
        
        gSitMain.getPlayerSitService().stopPlayerSit(player, GStopReason.KICKED, true, false, true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerGameModeChangeEvent(PlayerGameModeChangeEvent event) {
        if (event.getNewGameMode() == GameMode.SPECTATOR) {
            gSitMain.getPlayerSitService().stopPlayerSit(event.getPlayer(), GStopReason.GAMEMODE_CHANGE);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerDeathEvent(PlayerDeathEvent event) {
        gSitMain.getPlayerSitService().stopPlayerSit(event.getEntity(), GStopReason.DEATH);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerQuitEvent(PlayerQuitEvent event) {
        gSitMain.getPlayerSitService().stopPlayerSit(event.getPlayer(), GStopReason.DISCONNECT);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerTeleportEvent(PlayerTeleportEvent event) {
        gSitMain.getPlayerSitService().stopPlayerSit(event.getPlayer(), GStopReason.TELEPORT);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void entityDamageEvent(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity) || entity.getVehicle() == null) return;
        
        Entity vehicle = entity.getVehicle();
        if (vehicle.getScoreboardTags().contains(PlayerSitService.PLAYERSIT_ENTITY_TAG)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
        Entity rightClicked = event.getRightClicked();
        if (!(rightClicked instanceof Player target)) return;
        
        // Early exit if both sit options are disabled
        if (!allowSit && !allowSitNPC) return;
        
        Player player = event.getPlayer();
        if (!isPlayerValidForSitting(player)) return;
        
        // Check player permissions
        if (!gSitMain.getPermissionService().hasPermission(player, "PlayerSit", "PlayerSit.*")) return;
        
        // World and environment checks
        if (!gSitMain.getEnvironmentUtil().isEntityInAllowedWorld(player)) return;
        if (!gSitMain.getEnvironmentUtil().canUseInLocation(target.getLocation(), player, "playersit")) return;
        
        // Hand item check
        if (emptyHandOnly && player.getInventory().getItemInMainHand().getType() != Material.AIR) return;
        
        // Distance check
        if (maxDistance > 0d && calculateDistance(player, target) > maxDistance) return;
        
        // Passenger hierarchy checks
        if (gSitMain.getPassengerUtil().isEntityInEntityPassengerList(target, player) || 
            gSitMain.getPassengerUtil().isEntityInEntityPassengerList(player, target)) return;
        
        // Passenger count check
        if (isPassengerCountExceeded(target, player)) return;
        
        Entity highestEntity = gSitMain.getPassengerUtil().getTopEntityPassenger(target);
        if (!(highestEntity instanceof Player highestPlayer)) return;
        
        // NPC vs player sit permissions
        if (!isSittingAllowed(highestPlayer)) return;
        
        // Toggle permissions
        if (!gSitMain.getToggleService().canPlayerUsePlayerSit(player.getUniqueId()) || 
            !gSitMain.getToggleService().canPlayerUsePlayerSit(highestPlayer.getUniqueId())) return;
        
        gSitMain.getPlayerSitService().sitOnPlayer(player, highestPlayer);
    }
    
    private boolean isPlayerValidForSitting(Player player) {
        return player.isValid() && 
               !player.isSneaking() && 
               player.getGameMode() != GameMode.SPECTATOR && 
               !(disableElytra && player.isGliding()) && 
               !gSitMain.getCrawlService().isPlayerCrawling(player);
    }
    
    private double calculateDistance(Player player, Player target) {
        return target.getLocation()
            .add(0, target.getHeight() / 2, 0)
            .distance(player.getLocation().clone().add(0, player.getHeight() / 2, 0));
    }
    
    private boolean isPassengerCountExceeded(Entity target, Player player) {
        if (maxStack <= 0) return false;
        
        long passengerCount = gSitMain.getPassengerUtil().getEntityPassengerCount(target) + 1 + 
                             gSitMain.getPassengerUtil().getEntityVehicleCount(target) + 
                             gSitMain.getPassengerUtil().getEntityPassengerCount(player);
        
        return maxStack <= passengerCount;
    }
    
    private boolean isSittingAllowed(Player highestPlayer) {
        boolean isNPC = isPlayerNPC(highestPlayer);
        return (isNPC && allowSitNPC) || (!isNPC && allowSit);
    }
    
    private boolean isPlayerNPC(Player player) {
        return !Bukkit.getOnlinePlayers().contains(player);
    }
}
