package dev.geco.gsit.events;

import java.util.*;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class PlayerEvents implements Listener {

    private final GSitMain GPM;
    private final double MAX_DOUBLE_SNEAK_PITCH = 85d;
    private final long MAX_DOUBLE_SNEAK_TIME = 400;
    private final HashMap<Player, Long> crawl_players = new HashMap<>();

    public PlayerEvents(GSitMain GPluginMain) { GPM = GPluginMain; }

    public void PJoiE(PlayerJoinEvent Event) { GPM.getUManager().loginCheckForUpdates(Event.getPlayer()); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PQuiE(PlayerQuitEvent Event) {

        Player player = Event.getPlayer();

        GPM.getSitManager().removeSeat(player, GetUpReason.QUIT, true);
        GPM.getPoseManager().removePose(player, GetUpReason.QUIT, true);
        GPM.getCrawlManager().stopCrawl(player, GetUpReason.QUIT);
        GPM.getToggleManager().clearToggleCache(player.getUniqueId());

        crawl_players.remove(player);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void PTelE(PlayerTeleportEvent Event) {

        Player player = Event.getPlayer();

        if(!GPM.getSitManager().removeSeat(player, GetUpReason.TELEPORT, false)) Event.setCancelled(true);
        if(!GPM.getPoseManager().removePose(player, GetUpReason.TELEPORT, false)) Event.setCancelled(true);
        if(!GPM.getCrawlManager().stopCrawl(player, GetUpReason.TELEPORT)) Event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void EDamE(EntityDamageEvent Event) {

        Entity entity = Event.getEntity();
        if(!GPM.getCManager().GET_UP_DAMAGE || !(entity instanceof Player player) || Event.getDamage() <= 0d) return;

        GPM.getSitManager().removeSeat(player, GetUpReason.DAMAGE, true);
        GPM.getPoseManager().removePose(player, GetUpReason.DAMAGE, true);
        GPM.getCrawlManager().stopCrawl(player, GetUpReason.DAMAGE);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void PComPE(PlayerCommandPreprocessEvent Event) {

        List<String> commands = GPM.getCManager().COMMANDBLACKLIST;
        if(commands.isEmpty()) return;

        String message = Event.getMessage();
        Player player = Event.getPlayer();
        if(message.length() <= 1 || (!GPM.getSitManager().isSitting(player) && !GPM.getPoseManager().isPosing(player) && !GPM.getPlayerSitManager().isUsingPlayerSit(player))) return;

        message = message.substring(1).split(" ")[0].toLowerCase();
        if(commands.stream().noneMatch(message::equalsIgnoreCase)) return;

        GPM.getMManager().sendMessage(player, "Messages.action-blocked-error");
        Event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PTogSE(PlayerToggleSneakEvent Event) {

        if(!GPM.getCManager().C_DOUBLE_SNEAK) return;

        Player player = Event.getPlayer();
        if(!Event.isSneaking() || player.getLocation().getPitch() < MAX_DOUBLE_SNEAK_PITCH || !GPM.getCrawlManager().isAvailable()) return;

        if(!player.isValid() || !player.isOnGround() || player.getVehicle() != null || player.isSleeping() || GPM.getCrawlManager().isCrawling(player)) return;

        if(!GPM.getToggleManager().canCrawl(player.getUniqueId())) return;

        if(!crawl_players.containsKey(player)) {

            crawl_players.put(player, System.currentTimeMillis());
            return;
        }

        long last = crawl_players.get(player);

        crawl_players.put(player, System.currentTimeMillis());

        if(last < System.currentTimeMillis() - MAX_DOUBLE_SNEAK_TIME) return;

        if(!GPM.getPManager().hasPermission(player, "CrawlSneak", "Crawl.*")) return;

        if(!GPM.getPManager().hasPermission(player, "ByPass.Region", "ByPass.*") && !GPM.getEnvironmentUtil().isInAllowedWorld(player)) return;

        if(GPM.getWorldGuardLink() != null && !GPM.getWorldGuardLink().checkFlag(player.getLocation(), GPM.getWorldGuardLink().getFlag("crawl"))) return;

        if(GPM.getCrawlManager().startCrawl(player) == null) crawl_players.remove(player);
    }

}