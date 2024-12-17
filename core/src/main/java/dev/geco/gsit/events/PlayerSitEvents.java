package dev.geco.gsit.events;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class PlayerSitEvents implements Listener {

    private final GSitMain GPM;

    public PlayerSitEvents(GSitMain GPluginMain) { GPM = GPluginMain; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PTogSE(PlayerToggleSneakEvent Event) {

        Player player = Event.getPlayer();

        if(!GPM.getCManager().PS_SNEAK_EJECTS || !Event.isSneaking() || player.isFlying() || player.getVehicle() != null || GPM.getPlayerSitManager().WAIT_EJECT.contains(player) || player.getPassengers().isEmpty()) return;

        GPM.getPlayerSitManager().stopPlayerSit(player, GetUpReason.KICKED);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void PGamMCE(PlayerGameModeChangeEvent Event) { if(Event.getNewGameMode() == GameMode.SPECTATOR) GPM.getPlayerSitManager().stopPlayerSit(Event.getPlayer(), GetUpReason.ACTION); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PDeaE(PlayerDeathEvent Event) { if(Event.getEntity().getVehicle() != null) GPM.getPlayerSitManager().stopPlayerSit(Event.getEntity(), GetUpReason.DEATH); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PQuiE(PlayerQuitEvent Event) { if(Event.getPlayer().getVehicle() != null) GPM.getPlayerSitManager().stopPlayerSit(Event.getPlayer(), GetUpReason.QUIT); }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void PTelE(PlayerTeleportEvent Event) { GPM.getPlayerSitManager().stopPlayerSit(Event.getPlayer(), GetUpReason.TELEPORT); }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void EDamE(EntityDamageEvent Event) { if(Event.getCause() == EntityDamageEvent.DamageCause.FALL && Event.getEntity() instanceof LivingEntity && Event.getEntity().getVehicle() != null && Event.getEntity().getVehicle().getScoreboardTags().contains(GSitMain.NAME + "_PlayerSeatEntity")) Event.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PIntAEE(PlayerInteractAtEntityEvent Event) {

        Entity rightClicked = Event.getRightClicked();
        if(!(rightClicked instanceof Player target)) return;

        if(!GPM.getCManager().PS_ALLOW_SIT && !GPM.getCManager().PS_ALLOW_SIT_NPC) return;

        Player player = Event.getPlayer();
        if(!GPM.getPManager().hasPermission(player, "PlayerSit", "PlayerSit.*")) return;

        if(!GPM.getEnvironmentUtil().isInAllowedWorld(player)) return;

        if(GPM.getCManager().PS_EMPTY_HAND_ONLY && player.getInventory().getItemInMainHand().getType() != Material.AIR) return;

        if(!player.isValid() || !target.isValid() || player.isSneaking() || player.getGameMode() == GameMode.SPECTATOR) return;

        if(GPM.getCManager().FEATUREFLAGS.contains("DISABLE_PLAYERSIT_ELYTRA") && player.isGliding()) return;

        if(GPM.getCrawlManager().isCrawling(player)) return;

        double distance = GPM.getCManager().PS_MAX_DISTANCE;
        if(distance > 0d && target.getLocation().add(0, target.getHeight() / 2, 0).distance(player.getLocation().clone().add(0, player.getHeight() / 2, 0)) > distance) return;

        if(GPM.getPlotSquaredLink() != null && !GPM.getPlotSquaredLink().canCreateSeat(target.getLocation(), player)) return;

        if(GPM.getWorldGuardLink() != null && !GPM.getWorldGuardLink().checkFlag(target.getLocation(), GPM.getWorldGuardLink().getFlag("playersit"))) return;

        if(GPM.getGriefPreventionLink() != null && !GPM.getGriefPreventionLink().check(target.getLocation(), player)) return;

        if(GPM.getPassengerUtil().isInPassengerList(target, player) || GPM.getPassengerUtil().isInPassengerList(player, target)) return;

        long amount = GPM.getPassengerUtil().getPassengerAmount(target) + 1 + GPM.getPassengerUtil().getVehicleAmount(target) + GPM.getPassengerUtil().getPassengerAmount(player);
        if(GPM.getCManager().PS_MAX_STACK > 0 && GPM.getCManager().PS_MAX_STACK <= amount) return;

        Entity highestEntity = GPM.getPassengerUtil().getHighestEntity(target);
        if(!(highestEntity instanceof Player highestPlayer)) return;

        boolean isNPC = isNPC(highestPlayer);
        if((isNPC && !GPM.getCManager().PS_ALLOW_SIT_NPC) || (!isNPC && !GPM.getCManager().PS_ALLOW_SIT)) return;

        if(!GPM.getToggleManager().canPlayerSit(player.getUniqueId()) || !GPM.getToggleManager().canPlayerSit(highestPlayer.getUniqueId())) return;

        GPM.getPlayerSitManager().sitOnPlayer(player, highestPlayer);
    }

    private boolean isNPC(Player P) { return !Bukkit.getOnlinePlayers().contains(P); }

}