package dev.geco.gsit.event;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.model.Seat;
import dev.geco.gsit.model.StopReason;
import dev.geco.gsit.model.Crawl;
import dev.geco.gsit.model.Pose;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
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
    private final HashMap<Player, Long> doubleSneakCrawlPlayers = new HashMap<>();

    public PlayerEventHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) { gSitMain.getUpdateService().checkForUpdates(event.getPlayer()); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        stopActions(player, StopReason.DISCONNECT, true);
        gSitMain.getToggleService().clearEntitySitToggleCache(player.getUniqueId());
        doubleSneakCrawlPlayers.remove(player);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerTeleportEvent(PlayerTeleportEvent event) { stopActions(event.getPlayer(), StopReason.TELEPORT, false); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerDeathEvent(PlayerDeathEvent event) { stopActions(event.getEntity(), StopReason.DEATH, false); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityDamageEvent(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if(!gSitMain.getConfigService().GET_UP_DAMAGE || !(entity instanceof Player player) || event.getDamage() <= 0d) return;
        stopActions(player, StopReason.DAMAGE, true);
    }

    private void stopActions(Player player, StopReason stopReason, boolean useSafeDismount) {
        Seat seat = gSitMain.getSitService().getSeatByEntity(player);
        if(seat != null) gSitMain.getSitService().removeSeat(seat, stopReason, useSafeDismount);
        Pose pose = gSitMain.getPoseService().getPoseByPlayer(player);
        if(pose != null) gSitMain.getPoseService().removePose(pose, stopReason, useSafeDismount);
        Crawl crawl = gSitMain.getCrawlService().getCrawlByPlayer(player);
        if(crawl != null) gSitMain.getCrawlService().stopCrawl(crawl, stopReason);
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

        if(!doubleSneakCrawlPlayers.containsKey(player)) {
            doubleSneakCrawlPlayers.put(player, System.currentTimeMillis());
            return;
        }

        long last = doubleSneakCrawlPlayers.get(player);
        doubleSneakCrawlPlayers.put(player, System.currentTimeMillis());
        if(last < System.currentTimeMillis() - MAX_DOUBLE_SNEAK_TIME) return;

        if(!gSitMain.getPermissionService().hasPermission(player, "CrawlSneak", "Crawl.*")) return;

        if(!gSitMain.getEnvironmentUtil().isEntityInAllowedWorld(player)) return;

        if(!gSitMain.getEnvironmentUtil().canUseInLocation(player.getLocation(), player, "crawl")) return;

        doubleSneakCrawlPlayers.remove(player);

        gSitMain.getCrawlService().startCrawl(player);
    }

}