package dev.geco.gsit.event;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.object.GSeat;
import dev.geco.gsit.object.GStopReason;
import dev.geco.gsit.object.IGCrawl;
import dev.geco.gsit.object.IGPose;
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

import java.util.*;

public class PlayerEventHandler implements Listener {

    private static final double MAX_DOUBLE_SNEAK_PITCH = 85d;
    private static final long MAX_DOUBLE_SNEAK_TIME = 400;

    private final GSitMain gSitMain;
    private final Set<String> commandBlacklist;
    private final Map<Player, Long> doubleSneakCrawlPlayers = new HashMap<>();
    private final boolean doubleSneakEnabled;
    private final boolean getUpDamageEnabled;

    public PlayerEventHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        this.doubleSneakEnabled = gSitMain.getConfigService().C_DOUBLE_SNEAK;
        this.getUpDamageEnabled = gSitMain.getConfigService().GET_UP_DAMAGE;
        
        // Pre-process command blacklist to lowercase
        List<String> commands = gSitMain.getConfigService().COMMANDBLACKLIST;
        this.commandBlacklist = commands.isEmpty() ? Collections.emptySet() : 
            new HashSet<>(commands.size());
        for (String cmd : commands) {
            commandBlacklist.add(cmd.toLowerCase());
        }
    }

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        gSitMain.getUpdateService().checkForUpdates(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        stopActions(player, GStopReason.DISCONNECT, true);
        gSitMain.getToggleService().clearEntitySitToggleCache(player.getUniqueId());
        doubleSneakCrawlPlayers.remove(player);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerTeleportEvent(PlayerTeleportEvent event) {
        stopActions(event.getPlayer(), GStopReason.TELEPORT, false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerDeathEvent(PlayerDeathEvent event) {
        stopActions(event.getEntity(), GStopReason.DEATH, false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityDamageEvent(EntityDamageEvent event) {
        if (!getUpDamageEnabled || event.getDamage() <= 0d) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        stopActions(player, GStopReason.DAMAGE, true);
    }

    private void stopActions(Player player, GStopReason stopReason, boolean useSafeDismount) {
        // Cache services
        final var sitService = gSitMain.getSitService();
        final var poseService = gSitMain.getPoseService();
        final var crawlService = gSitMain.getCrawlService();
        
        GSeat seat = sitService.getSeatByEntity(player);
        if (seat != null) sitService.removeSeat(seat, stopReason, useSafeDismount);
        
        IGPose pose = poseService.getPoseByPlayer(player);
        if (pose != null) poseService.removePose(pose, stopReason, useSafeDismount);
        
        IGCrawl crawl = crawlService.getCrawlByPlayer(player);
        if (crawl != null) crawlService.stopCrawl(crawl, stopReason);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        if (commandBlacklist.isEmpty()) return;
        
        String message = event.getMessage();
        if (message.length() <= 1) return;
        
        Player player = event.getPlayer();
        if (!isPlayerInAction(player)) return;
        
        // Extract command and check blacklist
        String command = message.substring(1).split("\\s+", 2)[0].toLowerCase();
        if (!commandBlacklist.contains(command)) return;
        
        gSitMain.getMessageService().sendMessage(player, "Messages.action-blocked-error");
        event.setCancelled(true);
    }
    
    private boolean isPlayerInAction(Player player) {
        return gSitMain.getSitService().isEntitySitting(player) || 
               gSitMain.getPoseService().isPlayerPosing(player) || 
               gSitMain.getPlayerSitService().isPlayerInPlayerSitStack(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerToggleSneakEvent(PlayerToggleSneakEvent event) {
        if (!doubleSneakEnabled || !event.isSneaking()) return;
        
        Player player = event.getPlayer();
        if (!isValidForCrawl(player)) return;
        
        long currentTime = System.currentTimeMillis();
        Long lastSneakTime = doubleSneakCrawlPlayers.get(player);
        
        if (lastSneakTime == null) {
            doubleSneakCrawlPlayers.put(player, currentTime);
            return;
        }
        
        // Check if double-sneak within time window
        if (currentTime - lastSneakTime > MAX_DOUBLE_SNEAK_TIME) {
            doubleSneakCrawlPlayers.put(player, currentTime);
            return;
        }
        
        // Final checks before starting crawl
        if (!hasCrawlPermission(player) || !isInAllowedWorld(player) || !canUseInLocation(player)) {
            doubleSneakCrawlPlayers.remove(player);
            return;
        }
        
        doubleSneakCrawlPlayers.remove(player);
        gSitMain.getCrawlService().startCrawl(player);
    }
    
    private boolean isValidForCrawl(Player player) {
        return player.isValid() && 
               player.isOnGround() && 
               player.getVehicle() == null && 
               !player.isSleeping() && 
               player.getLocation().getPitch() >= MAX_DOUBLE_SNEAK_PITCH && 
               gSitMain.getCrawlService().isAvailable() && 
               !gSitMain.getCrawlService().isPlayerCrawling(player) && 
               gSitMain.getToggleService().canPlayerUseCrawl(player.getUniqueId());
    }
    
    private boolean hasCrawlPermission(Player player) {
        return gSitMain.getPermissionService().hasPermission(player, "CrawlSneak", "Crawl.*");
    }
    
    private boolean isInAllowedWorld(Player player) {
        return gSitMain.getEnvironmentUtil().isEntityInAllowedWorld(player);
    }
    
    private boolean canUseInLocation(Player player) {
        return gSitMain.getEnvironmentUtil().canUseInLocation(player.getLocation(), player, "crawl");
    }
}
