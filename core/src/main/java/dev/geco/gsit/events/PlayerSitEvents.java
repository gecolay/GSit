package dev.geco.gsit.events;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;

import org.spigotmc.event.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class PlayerSitEvents implements Listener {

    private final GSitMain GPM;

    public PlayerSitEvents(GSitMain GPluginMain) { GPM = GPluginMain; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PTogSE(PlayerToggleSneakEvent Event) {

        Player player = Event.getPlayer();

        if(!GPM.getCManager().PS_SNEAK_EJECTS || !Event.isSneaking() || player.isFlying() || player.isInsideVehicle()) return;

        if(!GPM.getPlayerSitManager().stopPlayerSit(player, GetUpReason.KICKED)) Event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PDeaE(PlayerDeathEvent Event) { if(Event.getEntity().isInsideVehicle()) GPM.getPlayerSitManager().stopPlayerSit(Event.getEntity(), GetUpReason.DEATH); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PQuiE(PlayerQuitEvent Event) { if(Event.getPlayer().isInsideVehicle()) GPM.getPlayerSitManager().stopPlayerSit(Event.getPlayer(), GetUpReason.QUIT); }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void EDisE(EntityDismountEvent Event) {

        if(!GPM.getPlayerSitManager().stopPlayerSit(Event.getEntity(), GetUpReason.GET_UP)) Event.setCancelled(true);

        GPM.getPlayerSitManager().stopPlayerSit(Event.getDismounted(), GetUpReason.GET_UP);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PIntAEE(PlayerInteractAtEntityEvent Event) {

        Entity rightClicked = Event.getRightClicked();

        if(!(rightClicked instanceof Player)) return;

        Player player = Event.getPlayer();

        Player target = (Player) rightClicked;

        if(!GPM.getCManager().PS_ALLOW_SIT && !GPM.getCManager().PS_ALLOW_SIT_NPC) return;

        if(!GPM.getPManager().hasPermission(player, "PlayerSit")) return;

        if(GPM.getCManager().WORLDBLACKLIST.contains(player.getWorld().getName()) && !GPM.getPManager().hasPermission(player, "ByPass.World", "ByPass.*")) return;

        if(GPM.getCManager().PS_EMPTY_HAND_ONLY && player.getInventory().getItemInMainHand().getType() != Material.AIR) return;

        if(!player.isValid() || !target.isValid() || player.isSneaking() || player.isInsideVehicle() || player.getGameMode() == GameMode.SPECTATOR) return;

        if(GPM.getCrawlManager().isCrawling(player)) return;

        double distance = GPM.getCManager().PS_MAX_DISTANCE;

        if(distance > 0d && target.getLocation().add(0, target.getHeight() / 2, 0).distance(player.getLocation().add(0, player.getHeight() / 2, 0)) > distance) return;

        if(GPM.getPlotSquaredLink() != null && !GPM.getPlotSquaredLink().canCreateSeat(target.getLocation(), player)) return;

        if(GPM.getWorldGuardLink() != null && !GPM.getWorldGuardLink().checkFlag(target.getLocation(), GPM.getWorldGuardLink().getFlag("playersit"))) return;

        if(GPM.getGriefPreventionLink() != null && !GPM.getGriefPreventionLink().check(target.getLocation(), player)) return;

        if(GPM.getPassengerUtil().isInPassengerList(target, player) || GPM.getPassengerUtil().isInPassengerList(player, target)) return;

        long amount = GPM.getPassengerUtil().getPassengerAmount(target) + 1 + GPM.getPassengerUtil().getVehicleAmount(target) + GPM.getPassengerUtil().getPassengerAmount(player);

        if(GPM.getCManager().PS_MAX_STACK > 0 && GPM.getCManager().PS_MAX_STACK <= amount) return;

        Entity highestEntity = GPM.getPassengerUtil().getHighestEntity(target);

        if(!(highestEntity instanceof Player)) return;

        Player highestPlayer = (Player) highestEntity;

        if(!GPM.getToggleManager().canPlayerSit(player.getUniqueId()) || !GPM.getToggleManager().canPlayerSit(highestPlayer.getUniqueId())) return;

        boolean isNPC = GPM.getPassengerUtil().isNPC(highestPlayer);

        if(isNPC && !GPM.getCManager().PS_ALLOW_SIT_NPC) return;

        if(!isNPC && !GPM.getCManager().PS_ALLOW_SIT) return;

        boolean cancel = GPM.getPlayerSitManager().sitOnPlayer(player, highestPlayer);

        if(cancel) Event.setCancelled(true);
    }

}