package dev.geco.gsit.event;

import dev.geco.gsit.GSitMain;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.HashMap;
import java.util.UUID;

public class CrawlEventHandler implements Listener {

    private final GSitMain gSitMain;
    private final HashMap<UUID, Long> doubleSneakCrawlPlayers = new HashMap<>();

    public CrawlEventHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        doubleSneakCrawlPlayers.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerToggleSneakEvent(PlayerToggleSneakEvent event) {
        if((!gSitMain.getConfigService().C_DOUBLE_SNEAK && !gSitMain.getConfigService().C_CORRIDOR_CRAWL) || !event.isSneaking() || !gSitMain.getCrawlService().isAvailable()) return;

        Player player = event.getPlayer();
        if(!player.isValid() || !player.isOnGround() || player.getVehicle() != null || player.isSleeping() || gSitMain.getCrawlService().isPlayerCrawling(player)) return;

        UUID playerId = player.getUniqueId();
        if(!gSitMain.getToggleService().canPlayerUseCrawl(playerId)) return;

        if(gSitMain.getConfigService().C_DOUBLE_SNEAK && player.getLocation().getPitch() >= gSitMain.getConfigService().C_DOUBLE_SNEAK_PITCH) {
            if(!doubleSneakCrawlPlayers.containsKey(playerId)) {
                doubleSneakCrawlPlayers.put(playerId, System.currentTimeMillis());
                return;
            }

            long last = doubleSneakCrawlPlayers.get(playerId);
            doubleSneakCrawlPlayers.put(playerId, System.currentTimeMillis());
            if(last < System.currentTimeMillis() - gSitMain.getConfigService().C_DOUBLE_SNEAK_TIME) return;

            doubleSneakCrawlPlayers.remove(playerId);

            startSneakCrawl(player);
            return;
        }

        if(gSitMain.getConfigService().C_CORRIDOR_CRAWL && isFacingCrawlCorridor(player)) startSneakCrawl(player);
    }

    private void startSneakCrawl(Player player) {
        if(!gSitMain.getPermissionService().hasPermission(player, "CrawlSneak", "Crawl.*")) return;

        if(!gSitMain.getEnvironmentUtil().isEntityInAllowedWorld(player)) return;

        if(!gSitMain.getEnvironmentUtil().canUseInLocation(player.getLocation(), player, "crawl")) return;

        gSitMain.getCrawlService().startCrawl(player);
    }

    private boolean isFacingCrawlCorridor(Player player) {
        Block feetBlock = player.getLocation().getBlock();
        Block frontBlock = feetBlock.getRelative(getCardinalFacing(player.getLocation().getYaw()));
        return frontBlock.isPassable() && !frontBlock.getRelative(BlockFace.UP).isPassable();
    }

    private BlockFace getCardinalFacing(float yaw) {
        yaw = ((yaw % 360f) + 360f) % 360f;
        if(yaw >= 315f || yaw < 45f) return BlockFace.SOUTH;
        if(yaw < 135f) return BlockFace.WEST;
        if(yaw < 225f) return BlockFace.NORTH;
        return BlockFace.EAST;
    }

}