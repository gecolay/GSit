package dev.geco.gsit.event;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.object.GetUpReason;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.HashMap;
import java.util.List;

public class PlayerEventHandler implements Listener {

    private final double MAX_DOUBLE_SNEAK_PITCH = 85d;
    private final long MAX_DOUBLE_SNEAK_TIME = 400;

    private final GSitMain gSitMain;
    private final HashMap<Player, Long> crawlPlayers = new HashMap<>();

    public PlayerEventHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    public void playerJoinEvent(PlayerJoinEvent event) { gSitMain.getUpdateService().loginCheckForUpdates(event.getPlayer()); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        gSitMain.getSitService().removeSeat(player, GetUpReason.QUIT, true);
        gSitMain.getPoseService().removePose(player, GetUpReason.QUIT, true);
        gSitMain.getCrawlService().stopCrawl(player, GetUpReason.QUIT);
        gSitMain.getToggleService().clearEntitySitToggleCache(player.getUniqueId());

        crawlPlayers.remove(player);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerTeleportEvent(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if(!gSitMain.getSitService().removeSeat(player, GetUpReason.TELEPORT, false)) event.setCancelled(true);
        if(!gSitMain.getPoseService().removePose(player, GetUpReason.TELEPORT, false)) event.setCancelled(true);
        if(!gSitMain.getCrawlService().stopCrawl(player, GetUpReason.TELEPORT)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityDamageEvent(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if(!gSitMain.getConfigService().GET_UP_DAMAGE || !(entity instanceof Player player) || event.getDamage() <= 0d) return;

        gSitMain.getSitService().removeSeat(player, GetUpReason.DAMAGE, true);
        gSitMain.getPoseService().removePose(player, GetUpReason.DAMAGE, true);
        gSitMain.getCrawlService().stopCrawl(player, GetUpReason.DAMAGE);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        List<String> commands = gSitMain.getConfigService().COMMANDBLACKLIST;
        if(commands.isEmpty()) return;

        String message = event.getMessage();
        Player player = event.getPlayer();
        if(message.length() <= 1 || (!gSitMain.getSitService().isEntitySitting(player) && !gSitMain.getPoseService().isPlayerPosing(player) && !gSitMain.getPlayerSitService().isPlayerInPlayerSitStack(player))) return;

        message = message.substring(1).split(" ")[0].toLowerCase();
        if(commands.stream().noneMatch(message::equalsIgnoreCase)) return;

        gSitMain.getMessageService().sendMessage(player, "Messages.action-blocked-error");
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerToggleSneakEvent(PlayerToggleSneakEvent event) {
        if(!gSitMain.getConfigService().C_DOUBLE_SNEAK) return;

        Player player = event.getPlayer();
        if(!event.isSneaking() || player.getLocation().getPitch() < MAX_DOUBLE_SNEAK_PITCH || !gSitMain.getCrawlService().isAvailable()) return;

        if(!player.isValid() || !player.isOnGround() || player.getVehicle() != null || player.isSleeping() || gSitMain.getCrawlService().isPlayerCrawling(player)) return;

        if(!gSitMain.getToggleService().canPlayerUseCrawl(player.getUniqueId())) return;

        if(!crawlPlayers.containsKey(player)) {
            crawlPlayers.put(player, System.currentTimeMillis());
            return;
        }

        long last = crawlPlayers.get(player);
        crawlPlayers.put(player, System.currentTimeMillis());
        if(last < System.currentTimeMillis() - MAX_DOUBLE_SNEAK_TIME) return;

        if(!gSitMain.getPermissionService().hasPermission(player, "CrawlSneak", "Crawl.*")) return;
        if(!gSitMain.getPermissionService().hasPermission(player, "ByPass.Region", "ByPass.*") && !gSitMain.getEnvironmentUtil().isEntityInAllowedWorld(player)) return;

        if(gSitMain.getWorldGuardLink() != null && !gSitMain.getWorldGuardLink().canUseInLocation(player.getLocation(), gSitMain.getWorldGuardLink().getFlag("crawl"))) return;

        if(gSitMain.getCrawlService().startCrawl(player) == null) crawlPlayers.remove(player);
    }

}