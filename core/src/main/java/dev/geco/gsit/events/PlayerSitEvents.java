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
    public void PTogSE(PlayerToggleSneakEvent e) {

        Player p = e.getPlayer();

        if(!GPM.getCManager().PS_SNEAK_EJECTS || !e.isSneaking() || p.isFlying() || p.isInsideVehicle()) return;

        boolean r = GPM.getPlayerSitManager().stopPlayerSit(p, GetUpReason.KICKED);

        if(!r) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PDeaE(PlayerDeathEvent e) {

        if(e.getEntity().isInsideVehicle()) GPM.getPlayerSitManager().stopPlayerSit(e.getEntity(), GetUpReason.DEATH);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PQuiE(PlayerQuitEvent e) {

        if(e.getPlayer().isInsideVehicle()) GPM.getPlayerSitManager().stopPlayerSit(e.getPlayer(), GetUpReason.QUIT);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void EDisE(EntityDismountEvent e) {

        boolean r = GPM.getPlayerSitManager().stopPlayerSit(e.getEntity(), GetUpReason.GET_UP);

        if(!r) e.setCancelled(true);

        GPM.getPlayerSitManager().stopPlayerSit(e.getDismounted(), GetUpReason.GET_UP);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PIntAEE(PlayerInteractAtEntityEvent e) {

        Entity E = e.getRightClicked();

        if(!(E instanceof Player)) return;

        Player p = e.getPlayer();

        Player t = (Player) E;

        if(!GPM.getCManager().PS_ALLOW_SIT && !GPM.getCManager().PS_ALLOW_SIT_NPC) return;

        if(!GPM.getPManager().hasNormalPermission(p, "PlayerSit")) return;

        if(GPM.getCManager().WORLDBLACKLIST.contains(p.getWorld().getName()) && !GPM.getPManager().hasPermission(p, "ByPass.World", "ByPass.*")) return;

        if(GPM.getCManager().PS_EMPTY_HAND_ONLY && p.getInventory().getItemInMainHand().getType() != Material.AIR) return;

        if(!p.isValid() || !t.isValid() || p.isSneaking() || p.isInsideVehicle() || p.getGameMode() == GameMode.SPECTATOR) return;

        if(GPM.getCrawlManager() != null && GPM.getCrawlManager().isCrawling(p)) return;

        double d = GPM.getCManager().PS_MAX_DISTANCE;

        if(d > 0d && t.getLocation().add(0, t.getHeight() / 2, 0).distance(p.getLocation().add(0, p.getHeight() / 2, 0)) > d) return;

        if(GPM.getWorldGuardLink() != null && !GPM.getWorldGuardLink().checkFlag(t.getLocation(), GPM.getWorldGuardLink().PLAYERSIT_FLAG)) return;

        if(GPM.getGriefPreventionLink() != null && !GPM.getGriefPreventionLink().check(t.getLocation(), p)) return;

        if(GPM.getPassengerUtil().isInPassengerList(t, p) || GPM.getPassengerUtil().isInPassengerList(p, t)) return;

        long a = GPM.getPassengerUtil().getPassengerAmount(t) + 1 + GPM.getPassengerUtil().getVehicleAmount(t) + GPM.getPassengerUtil().getPassengerAmount(p);

        if(GPM.getCManager().PS_MAX_STACK > 0 && GPM.getCManager().PS_MAX_STACK <= a) return;

        Entity s = GPM.getPassengerUtil().getHighestEntity(t);

        if(!(s instanceof Player)) return;

        Player z = (Player) s;

        if(!GPM.getToggleManager().canPlayerSit(p.getUniqueId()) || !GPM.getToggleManager().canPlayerSit(z.getUniqueId())) return;

        boolean n = GPM.getPassengerUtil().isNPC(z);

        if(n && !GPM.getCManager().PS_ALLOW_SIT_NPC) return;

        if(!n && !GPM.getCManager().PS_ALLOW_SIT) return;

        boolean r = GPM.getPlayerSitManager().sitOnPlayer(p, z);

        if(r) e.setCancelled(true);
    }

}