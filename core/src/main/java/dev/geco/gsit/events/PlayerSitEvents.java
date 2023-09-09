package dev.geco.gsit.events;

import java.util.*;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;

import org.spigotmc.event.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.*;
import dev.geco.gsit.manager.*;
import dev.geco.gsit.objects.*;

public class PlayerSitEvents implements Listener {

    private final GSitMain GPM;

    public PlayerSitEvents(GSitMain GPluginMain) { GPM = GPluginMain; }

    private final List<Player> wait_eject = new ArrayList<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PTogSE(PlayerToggleSneakEvent Event) {

        Player player = Event.getPlayer();

        if(!GPM.getCManager().PS_SNEAK_EJECTS || !Event.isSneaking() || player.isFlying() || player.isInsideVehicle() || wait_eject.contains(player)) return;

        GPM.getPlayerSitManager().stopPlayerSit(player, GetUpReason.KICKED);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void PGamMCE(PlayerGameModeChangeEvent Event) { if(Event.getNewGameMode() == GameMode.SPECTATOR) GPM.getPlayerSitManager().stopPlayerSit(Event.getPlayer(), GetUpReason.ACTION); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PDeaE(PlayerDeathEvent Event) { if(Event.getEntity().isInsideVehicle()) GPM.getPlayerSitManager().stopPlayerSit(Event.getEntity(), GetUpReason.DEATH); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PQuiE(PlayerQuitEvent Event) { if(Event.getPlayer().isInsideVehicle()) GPM.getPlayerSitManager().stopPlayerSit(Event.getPlayer(), GetUpReason.QUIT); }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void EDamE(EntityDamageEvent Event) { if(Event.getCause() == EntityDamageEvent.DamageCause.FALL && Event.getEntity() instanceof LivingEntity && Event.getEntity().isInsideVehicle() && Event.getEntity().getVehicle().hasMetadata(GPM.NAME + "A")) Event.setCancelled(true); }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void EDisE(EntityDismountEvent Event) {

        if(!Event.getDismounted().hasMetadata(GPM.NAME + "A") && !(Event.getDismounted() instanceof Player)) return;

        if(Event.getEntity() instanceof Player) {

            Player player = (Player) Event.getEntity();

            PrePlayerGetUpPlayerSitEvent preEvent = new PrePlayerGetUpPlayerSitEvent(player, GetUpReason.GET_UP);

            Bukkit.getPluginManager().callEvent(preEvent);

            if(preEvent.isCancelled()) {

                Event.setCancelled(true);
                return;
            }

            wait_eject.add(player);

            GPM.getTManager().runDelayed(() -> {
                wait_eject.remove(player);
            }, 2);
        }

        Entity bottom = GPM.getPassengerUtil().getBottomEntity(Event.getDismounted());

        if(GPM.getCManager().PS_BOTTOM_RETURN && Event.getEntity().isValid() && Event.getEntity() instanceof Player && NMSManager.isNewerOrVersion(17, 0)) GPM.getEntityUtil().posEntity(Event.getEntity(), bottom.getLocation());

        if(Event.getDismounted().hasMetadata(GPM.NAME + "A") && !NMSManager.isNewerOrVersion(17, 0)) GPM.getEntityUtil().posEntity(Event.getDismounted(), bottom.getLocation());

        GPM.getPlayerSitManager().stopPlayerSit(Event.getDismounted(), GetUpReason.GET_UP);

        if(Event.getEntity() instanceof Player) Bukkit.getPluginManager().callEvent(new PlayerGetUpPlayerSitEvent((Player) Event.getEntity(), GetUpReason.GET_UP));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PIntAEE(PlayerInteractAtEntityEvent Event) {

        Entity rightClicked = Event.getRightClicked();

        if(!(rightClicked instanceof Player)) return;

        Player player = Event.getPlayer();

        Player target = (Player) rightClicked;

        if(!GPM.getCManager().PS_ALLOW_SIT && !GPM.getCManager().PS_ALLOW_SIT_NPC) return;

        if(!GPM.getPManager().hasPermission(player, "PlayerSit")) return;

        if(!GPM.getEnvironmentUtil().isInAllowedWorld(player)) return;

        if(GPM.getCManager().PS_EMPTY_HAND_ONLY && player.getInventory().getItemInMainHand().getType() != Material.AIR) return;

        if(!player.isValid() || !target.isValid() || player.isSneaking() || player.getGameMode() == GameMode.SPECTATOR) return;

        if(GPM.getCManager().FEATUREFLAGS.contains("DISABLE_PLAYERSIT_ELYTRA") && player.isGliding()) return;

        if((player.getGameMode() == GameMode.CREATIVE && player.getOpenInventory().getType() != InventoryType.CREATIVE) || (player.getGameMode() != GameMode.CREATIVE && player.getOpenInventory().getType() != InventoryType.CRAFTING)) return;

        if(GPM.getCrawlManager().isCrawling(player)) return;

        double distance = GPM.getCManager().PS_MAX_DISTANCE;

        if(distance > 0d && target.getLocation().clone().add(0, target.getHeight() / 2, 0).distance(player.getLocation().clone().add(0, player.getHeight() / 2, 0)) > distance) return;

        if(GPM.getPlotSquaredLink() != null && !GPM.getPlotSquaredLink().canCreateSeat(target.getLocation(), player)) return;

        if(GPM.getWorldGuardLink() != null && !GPM.getWorldGuardLink().checkFlag(target.getLocation(), GPM.getWorldGuardLink().getFlag("playersit"))) return;

        if(GPM.getGriefPreventionLink() != null && !GPM.getGriefPreventionLink().check(target.getLocation(), player)) return;

        if(GPM.getPassengerUtil().isInPassengerList(target, player) || GPM.getPassengerUtil().isInPassengerList(player, target)) return;

        long amount = GPM.getPassengerUtil().getPassengerAmount(target) + 1 + GPM.getPassengerUtil().getVehicleAmount(target) + GPM.getPassengerUtil().getPassengerAmount(player);

        if(GPM.getCManager().PS_MAX_STACK > 0 && GPM.getCManager().PS_MAX_STACK <= amount) return;

        Entity highestEntity = GPM.getPassengerUtil().getHighestEntity(target);

        if(!(highestEntity instanceof Player)) return;

        Player highestPlayer = (Player) highestEntity;

        boolean isNPC = GPM.getPassengerUtil().isNPC(highestPlayer);

        if((isNPC && !GPM.getCManager().PS_ALLOW_SIT_NPC) || (!isNPC && !GPM.getCManager().PS_ALLOW_SIT)) return;

        if(!GPM.getToggleManager().canPlayerSit(player.getUniqueId()) || !GPM.getToggleManager().canPlayerSit(highestPlayer.getUniqueId())) return;

        GPM.getPlayerSitManager().sitOnPlayer(player, highestPlayer);
    }

}