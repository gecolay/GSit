package dev.geco.gsit.events;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;

import org.spigotmc.event.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class PlayerEvents implements Listener {
    
    private final GSitMain GPM;
    
    public PlayerEvents(GSitMain GPluginMain) { GPM = GPluginMain; }
    
    @EventHandler
    public void PJoiE(PlayerJoinEvent e) {

        Player p = e.getPlayer();

        if(GPM.getCManager().CHECK_FOR_UPDATES && !GPM.getUManager().isLatestVersion()) {
            String me = GPM.getMManager().getMessage("Plugin.plugin-update", "%Name%", GPM.NAME, "%NewVersion%", GPM.getUManager().getLatestVersion(), "%Version%", GPM.getUManager().getPluginVersion(), "%Path%", GPM.getDescription().getWebsite());
            if(GPM.getPManager().hasPermission(p, "Update")) p.sendMessage(me);
        }
        
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PQuiE(PlayerQuitEvent e) {

        Player p = e.getPlayer();

        if(GPM.getSitManager().isSitting(p)) {
            GPM.getSitManager().removeSeat(GPM.getSitManager().getSeat(p), GetUpReason.QUIT, true);
        }

        if(GPM.getPoseManager().isPosing(p)) {
            GPM.getPoseManager().removePose(GPM.getPoseManager().getPose(p), GetUpReason.QUIT, true);
        }

    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void PTelE(PlayerTeleportEvent e) {

        Player p = e.getPlayer();

        if(GPM.getSitManager().isSitting(p)) {
            GPM.getSitManager().removeSeat(GPM.getSitManager().getSeat(p), GetUpReason.TELEPORT, false);
        }

        if(GPM.getPoseManager().isPosing(p)) {
            GPM.getPoseManager().removePose(GPM.getPoseManager().getPose(p), GetUpReason.TELEPORT, false);
        }
        
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void EDisE(EntityDismountEvent e) {

        Entity E = e.getEntity();

        if(!(E instanceof Player)) return;

        Player p = (Player) E;

        if(GPM.getSitManager().isSitting(p)) {
            GPM.getSitManager().removeSeat(GPM.getSitManager().getSeat(p), GetUpReason.GET_UP, true);
        }

        if(GPM.getPoseManager().isPosing(p)) {
            GPM.getPoseManager().removePose(GPM.getPoseManager().getPose(p), GetUpReason.GET_UP, true);
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PTogSE(PlayerToggleSneakEvent e) {

        Player p = e.getPlayer();

        if(!GPM.getCManager().PS_SNEAK_EJECTS || !e.isSneaking() || p.isFlying() || p.getPassengers().isEmpty()) return;






    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void EDamE(EntityDamageEvent e) {

        Entity E = e.getEntity();

        if(!GPM.getCManager().GET_UP_DAMAGE || !(E instanceof Player) || e.getFinalDamage() <= 0d) return;

        Player p = (Player) E;

        if(GPM.getSitManager().isSitting(p)) {
            GPM.getSitManager().removeSeat(GPM.getSitManager().getSeat(p), GetUpReason.DAMAGE, true);
        }

        if(GPM.getPoseManager().isPosing(p)) {
            GPM.getPoseManager().removePose(GPM.getPoseManager().getPose(p), GetUpReason.DAMAGE, true);
        }

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void PComPE(PlayerCommandPreprocessEvent e) {

        Player p = e.getPlayer();
        String m = e.getMessage();

        if(m.length() > 1 && GPM.getSitManager().isSitting(p)) {

            m = m.substring(1).split(" ")[0].toLowerCase();

            if(GPM.getCManager().BLOCKEDCOMMANDLIST.stream().anyMatch(m::equalsIgnoreCase)) {

                GPM.getMManager().sendMessage(p, "Messages.action-blocked-error");

                e.setCancelled(true);

            }

        }

    }

}