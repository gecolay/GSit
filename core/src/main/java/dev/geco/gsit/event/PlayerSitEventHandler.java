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
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PlayerSitEventHandler implements Listener {

    private final GSitMain gSitMain;

    public PlayerSitEventHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerToggleSneakEvent(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if(!gSitMain.getConfigService().PS_SNEAK_EJECTS || !event.isSneaking() || player.isFlying() || player.getVehicle() != null || gSitMain.getPlayerSitService().getPreventDismountStackPlayers().contains(player) || player.getPassengers().isEmpty()) return;
        gSitMain.getPlayerSitService().stopPlayerSit(player, GStopReason.KICKED, true, false, true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerGameModeChangeEvent(PlayerGameModeChangeEvent event) { if(event.getNewGameMode() == GameMode.SPECTATOR) gSitMain.getPlayerSitService().stopPlayerSit(event.getPlayer(), GStopReason.GAMEMODE_CHANGE); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerDeathEvent(PlayerDeathEvent event) { gSitMain.getPlayerSitService().stopPlayerSit(event.getEntity(), GStopReason.DEATH); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerQuitEvent(PlayerQuitEvent event) { gSitMain.getPlayerSitService().stopPlayerSit(event.getPlayer(), GStopReason.DISCONNECT); }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerTeleportEvent(PlayerTeleportEvent event) { gSitMain.getPlayerSitService().stopPlayerSit(event.getPlayer(), GStopReason.TELEPORT); }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void entityDamageEvent(EntityDamageEvent event) { if(event.getCause() == EntityDamageEvent.DamageCause.FALL && event.getEntity() instanceof LivingEntity && event.getEntity().getVehicle() != null && event.getEntity().getVehicle().getScoreboardTags().contains(PlayerSitService.PLAYERSIT_ENTITY_TAG)) event.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {

        Entity rightClicked = event.getRightClicked();
        if(!(rightClicked instanceof Player target)) return;

        if(!gSitMain.getConfigService().PS_ALLOW_SIT && !gSitMain.getConfigService().PS_ALLOW_SIT_NPC) return;

        Player player = event.getPlayer();
        if(!gSitMain.getPermissionService().hasPermission(player, "PlayerSit", "PlayerSit.*")) return;

        if(!gSitMain.getEnvironmentUtil().isEntityInAllowedWorld(player)) return;

        if(gSitMain.getConfigService().PS_EMPTY_HAND_ONLY && player.getInventory().getItemInMainHand().getType() != Material.AIR) return;

        if(!player.isValid() || !target.isValid() || player.isSneaking() || player.getGameMode() == GameMode.SPECTATOR) return;

        if(gSitMain.getConfigService().FEATUREFLAGS.contains("DISABLE_PLAYERSIT_ELYTRA") && player.isGliding()) return;

        if(gSitMain.getCrawlService().isPlayerCrawling(player)) return;

        double distance = gSitMain.getConfigService().PS_MAX_DISTANCE;
        if(distance > 0d && target.getLocation().add(0, target.getHeight() / 2, 0).distance(player.getLocation().clone().add(0, player.getHeight() / 2, 0)) > distance) return;

        if(!gSitMain.getEnvironmentUtil().canUseInLocation(target.getLocation(), player, "playersit")) return;

        if(gSitMain.getPassengerUtil().isEntityInPassengerList(target, player) || gSitMain.getPassengerUtil().isEntityInPassengerList(player, target)) return;

        long amount = gSitMain.getPassengerUtil().getEntityPassengerCount(target) + 1 + gSitMain.getPassengerUtil().getEntityVehicleCount(target) + gSitMain.getPassengerUtil().getEntityPassengerCount(player);
        if(gSitMain.getConfigService().PS_MAX_STACK > 0 && gSitMain.getConfigService().PS_MAX_STACK <= amount) return;

        Entity highestEntity = gSitMain.getPassengerUtil().getTopEntityPassenger(target);
        if(!(highestEntity instanceof Player highestPlayer)) return;

        boolean isNPC = isPlayerNPC(highestPlayer);
        if((isNPC && !gSitMain.getConfigService().PS_ALLOW_SIT_NPC) || (!isNPC && !gSitMain.getConfigService().PS_ALLOW_SIT)) return;

        if(!gSitMain.getToggleService().canPlayerUsePlayerSit(player.getUniqueId()) || !gSitMain.getToggleService().canPlayerUsePlayerSit(highestPlayer.getUniqueId())) return;

        gSitMain.getPlayerSitService().sitOnPlayer(player, highestPlayer);
    }

    private boolean isPlayerNPC(Player player) { return !Bukkit.getOnlinePlayers().contains(player); }

}