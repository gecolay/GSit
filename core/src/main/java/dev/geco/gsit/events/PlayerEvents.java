package dev.geco.gsit.events;

import java.util.*;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;

import org.spigotmc.event.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class PlayerEvents implements Listener {

    private final GSitMain GPM;

    private final double MAX_DOUBLE_SNEAK_PITCH = 85d;

    private final long MAX_DOUBLE_SNEAK_TIME = 400;

    private final HashMap<Player, Long> crawl_players = new HashMap<>();

    public PlayerEvents(GSitMain GPluginMain) { GPM = GPluginMain; }

    @EventHandler
    public void PJoiE(PlayerJoinEvent Event) {

        Player player = Event.getPlayer();

        GPM.getUManager().loginCheckForUpdates(player);

        if(GPM.getPackageUtil() != null) GPM.getPackageUtil().registerPlayer(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PQuiE(PlayerQuitEvent Event) {

        Player player = Event.getPlayer();

        GPM.getSitManager().removeSeat(player, GetUpReason.QUIT, true);

        GPM.getPoseManager().removePose(player, GetUpReason.QUIT, true);

        GPM.getCrawlManager().stopCrawl(player, GetUpReason.QUIT);

        GPM.getToggleManager().clearToggleCache(player.getUniqueId());

        crawl_players.remove(player);

        if(GPM.getPackageUtil() != null) GPM.getPackageUtil().unregisterPlayer(player);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void PTelE(PlayerTeleportEvent Event) {

        Player player = Event.getPlayer();

        if(!GPM.getSitManager().removeSeat(player, GetUpReason.TELEPORT, false)) Event.setCancelled(true);

        if(!GPM.getPoseManager().removePose(player, GetUpReason.TELEPORT, false)) Event.setCancelled(true);

        if(!GPM.getCrawlManager().stopCrawl(player, GetUpReason.TELEPORT)) Event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void EDisE(EntityDismountEvent Event) {

        Entity entity = Event.getEntity();

        if(!(entity instanceof Player)) return;

        Player player = (Player) entity;

        if(GPM.getSitManager().isSitting(player) && (!GPM.getCManager().GET_UP_SNEAK || (!GPM.getSitManager().removeSeat(player, GetUpReason.GET_UP, true)))) Event.setCancelled(true);

        if(GPM.getPoseManager().isPosing(player) && (!GPM.getCManager().GET_UP_SNEAK || !GPM.getPoseManager().removePose(player, GetUpReason.GET_UP, true))) Event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void EDamE(EntityDamageEvent Event) {

        Entity entity = Event.getEntity();

        if(!GPM.getCManager().GET_UP_DAMAGE || !(entity instanceof Player) || Event.getFinalDamage() <= 0d) return;

        Player player = (Player) entity;

        GPM.getSitManager().removeSeat(player, GetUpReason.DAMAGE, true);

        GPM.getPoseManager().removePose(player, GetUpReason.DAMAGE, true);

        GPM.getCrawlManager().stopCrawl(player, GetUpReason.DAMAGE);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void PComPE(PlayerCommandPreprocessEvent Event) {

        Player player = Event.getPlayer();

        String message = Event.getMessage();

        if(message.length() > 1 && (GPM.getSitManager().isSitting(player) || GPM.getPoseManager().isPosing(player))) {

            message = message.substring(1).split(" ")[0].toLowerCase();

            if(GPM.getCManager().COMMANDBLACKLIST.stream().anyMatch(message::equalsIgnoreCase)) {

                GPM.getMManager().sendMessage(player, "Messages.action-blocked-error");

                Event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PTogSE(PlayerToggleSneakEvent Event) {

        Player player = Event.getPlayer();

        if(!GPM.getCManager().C_DOUBLE_SNEAK) return;

        if(!Event.isSneaking() || player.getLocation().getPitch() < MAX_DOUBLE_SNEAK_PITCH || !GPM.getCrawlManager().isAvailable()) return;

        if(!player.isValid() || !player.isOnGround() || player.getVehicle() != null || player.isSleeping() || GPM.getCrawlManager().isCrawling(player)) return;

        if(!GPM.getToggleManager().canCrawl(player.getUniqueId())) return;

        if(!crawl_players.containsKey(player)) {

            crawl_players.put(player, System.currentTimeMillis());

            return;
        }

        long last = crawl_players.get(player);

        crawl_players.put(player, System.currentTimeMillis());

        if(last >= System.currentTimeMillis() - MAX_DOUBLE_SNEAK_TIME) {

            if(!GPM.getPManager().hasPermission(player, "CrawlSneak", "Crawl.*")) return;

            if(!GPM.getPManager().hasPermission(player, "ByPass.Region", "ByPass.*") && !GPM.getEnvironmentUtil().isInAllowedWorld(player)) return;

            if(GPM.getWorldGuardLink() != null && !GPM.getWorldGuardLink().checkFlag(player.getLocation(), GPM.getWorldGuardLink().getFlag("crawl"))) return;

            if(GPM.getCrawlManager().startCrawl(player) == null) crawl_players.remove(player);
        }
    }

}