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
    public void PJoiE(PlayerJoinEvent Event) {

        Player player = Event.getPlayer();

        if(GPM.getCManager().CHECK_FOR_UPDATES && !GPM.getUManager().isLatestVersion()) {

            String message = GPM.getMManager().getMessage("Plugin.plugin-update", "%Name%", GPM.NAME, "%NewVersion%", GPM.getUManager().getLatestVersion(), "%Version%", GPM.getUManager().getPluginVersion(), "%Path%", GPM.getDescription().getWebsite());

            if(GPM.getPManager().hasPermission(player, "Update")) player.sendMessage(message);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PQuiE(PlayerQuitEvent Event) {

        Player player = Event.getPlayer();

        if(GPM.getSitManager().isSitting(player)) GPM.getSitManager().removeSeat(player, GetUpReason.QUIT, true);

        if(GPM.getPoseManager() != null && GPM.getPoseManager().isPosing(player)) GPM.getPoseManager().removePose(player, GetUpReason.QUIT, true);

        if(GPM.getCrawlManager() != null && GPM.getCrawlManager().isCrawling(player)) GPM.getCrawlManager().stopCrawl(player, GetUpReason.QUIT);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void PTelE(PlayerTeleportEvent Event) {

        Player player = Event.getPlayer();

        if(GPM.getSitManager().isSitting(player) && !GPM.getSitManager().removeSeat(player, GetUpReason.TELEPORT, false)) Event.setCancelled(true);

        if(GPM.getPoseManager() != null && GPM.getPoseManager().isPosing(player) && !GPM.getPoseManager().removePose(player, GetUpReason.TELEPORT, false)) Event.setCancelled(true);

        if(GPM.getCrawlManager() != null && GPM.getCrawlManager().isCrawling(player) && !GPM.getCrawlManager().stopCrawl(player, GetUpReason.TELEPORT)) Event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void EDisE(EntityDismountEvent Event) {

        Entity entity = Event.getEntity();

        if(!(entity instanceof Player)) return;

        Player player = (Player) entity;

        if(GPM.getSitManager().isSitting(player)) {

            if(!GPM.getCManager().GET_UP_SNEAK) Event.setCancelled(true);
            else if(!GPM.getSitManager().removeSeat(player, GetUpReason.GET_UP, true)) Event.setCancelled(true);
        }

        if(GPM.getPoseManager() != null && GPM.getPoseManager().isPosing(player)) {

            if(!GPM.getCManager().GET_UP_SNEAK) Event.setCancelled(true);
            else if(!GPM.getPoseManager().removePose(player, GetUpReason.GET_UP, true)) Event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void EDamE(EntityDamageEvent Event) {

        Entity entity = Event.getEntity();

        if(!GPM.getCManager().GET_UP_DAMAGE || !(entity instanceof Player) || Event.getFinalDamage() <= 0d) return;

        Player player = (Player) entity;

        if(GPM.getSitManager().isSitting(player)) GPM.getSitManager().removeSeat(player, GetUpReason.DAMAGE, true);

        if(GPM.getPoseManager() != null && GPM.getPoseManager().isPosing(player)) GPM.getPoseManager().removePose(player, GetUpReason.DAMAGE, true);

        if(GPM.getCrawlManager() != null && GPM.getCrawlManager().isCrawling(player)) GPM.getCrawlManager().stopCrawl(player, GetUpReason.DAMAGE);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void PComPE(PlayerCommandPreprocessEvent Event) {

        Player player = Event.getPlayer();

        String message = Event.getMessage();

        if(message.length() > 1 && (GPM.getSitManager().isSitting(player) || (GPM.getPoseManager() != null && GPM.getPoseManager().isPosing(player)))) {

            message = message.substring(1).split(" ")[0].toLowerCase();

            if(GPM.getCManager().COMMANDBLACKLIST.stream().anyMatch(message::equalsIgnoreCase)) {

                GPM.getMManager().sendMessage(player, "Messages.action-blocked-error");

                Event.setCancelled(true);
            }
        }
    }

}