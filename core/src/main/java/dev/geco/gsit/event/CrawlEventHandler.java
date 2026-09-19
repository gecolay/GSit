package dev.geco.gsit.event;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.model.Crawl;
import dev.geco.gsit.model.CrawlType;
import dev.geco.gsit.model.StopReason;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.HashMap;
import java.util.UUID;

public class CrawlEventHandler implements Listener {

    private final GSitMain gSitMain;

    public CrawlEventHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) { gSitMain.getCrawlService().clearPlayerCrawlTracking(event.getPlayer().getUniqueId()); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerToggleSneakEvent(PlayerToggleSneakEvent event) {
        if((!gSitMain.getConfigService().C_DOUBLE_SNEAK && !gSitMain.getConfigService().C_CORRIDOR_CRAWL) || !event.isSneaking() || !gSitMain.getCrawlService().isAvailable()) return;

        Player player = event.getPlayer();
        if(!player.isValid() || !player.isOnGround() || player.getVehicle() != null || player.isSleeping() || gSitMain.getCrawlService().isPlayerCrawling(player)) return;

        UUID playerId = player.getUniqueId();
        if(!gSitMain.getToggleService().canPlayerUseCrawl(playerId)) return;

        HashMap<UUID, Long> doubleSneakCrawlPlayers = gSitMain.getCrawlService().getDoubleSneakCrawlPlayers();

        if(gSitMain.getConfigService().C_DOUBLE_SNEAK && player.getLocation().getPitch() >= gSitMain.getConfigService().C_DOUBLE_SNEAK_PITCH) {
            if(!doubleSneakCrawlPlayers.containsKey(playerId)) {
                doubleSneakCrawlPlayers.put(playerId, System.currentTimeMillis());
                return;
            }

            long last = doubleSneakCrawlPlayers.get(playerId);
            doubleSneakCrawlPlayers.put(playerId, System.currentTimeMillis());
            if(last < System.currentTimeMillis() - gSitMain.getConfigService().C_DOUBLE_SNEAK_TIME) return;

            doubleSneakCrawlPlayers.remove(playerId);

            startSneakCrawl(player, CrawlType.DOUBLE_SNEAK);
            return;
        }

        if(!gSitMain.getConfigService().C_CORRIDOR_CRAWL) return;

        BlockFace facing = getCardinalFacing(player.getLocation().getYaw());
        Block feetBlock = player.getLocation().getBlock();
        if(!isCloseToWall(facing, feetBlock, player.getLocation()) || !isCrawlCorridor(feetBlock, facing)) return;

        startSneakCrawl(player, CrawlType.CORRIDOR);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerMoveEvent(PlayerMoveEvent event) {
        HashMap<UUID, Boolean> corridorCrawlPlayers = gSitMain.getCrawlService().getCorridorCrawlPlayers();
        Player player = event.getPlayer();
        Boolean hasEnteredCorridor = corridorCrawlPlayers.get(player.getUniqueId());
        if(hasEnteredCorridor == null) return;

        Crawl crawl = gSitMain.getCrawlService().getCrawlByPlayer(player);
        if(crawl == null) return;

        Location toLocation = event.getTo();
        Block feetBlock = toLocation.getBlock();

        if(!feetBlock.getRelative(BlockFace.UP).isPassable()) {
            if(!hasEnteredCorridor) corridorCrawlPlayers.put(player.getUniqueId(), true);
            return;
        }

        if(hasEnteredCorridor) {
            gSitMain.getCrawlService().stopCrawl(crawl, StopReason.GET_UP);
            return;
        }

        BlockFace facing = getCardinalFacing(toLocation.getYaw());
        if(isCloseToWall(facing, feetBlock, toLocation) && isCrawlCorridor(feetBlock, facing)) return;

        gSitMain.getCrawlService().stopCrawl(crawl, StopReason.GET_UP);
    }

    private void startSneakCrawl(Player player, CrawlType crawlType) {
        if(!gSitMain.getPermissionService().hasPermission(player, "CrawlSneak", "Crawl.*")) return;

        if(!gSitMain.getEnvironmentUtil().isEntityInAllowedWorld(player)) return;

        if(!gSitMain.getEnvironmentUtil().canUseInLocation(player.getLocation(), player, "crawl")) return;

        gSitMain.getCrawlService().startCrawl(player, crawlType);
    }

    private boolean isCrawlCorridor(Block feetBlock, BlockFace facing) {
        Block frontBlock = feetBlock.getRelative(facing);
        return frontBlock.isPassable() && !frontBlock.getRelative(BlockFace.UP).isPassable();
    }

    private boolean isCloseToWall(BlockFace facing, Block feetBlock, Location location) {
        Location halfStep = location.clone().add(facing.getModX() * 0.5d, 0d, facing.getModZ() * 0.5d);
        return !halfStep.getBlock().equals(feetBlock);
    }

    private BlockFace getCardinalFacing(float yaw) {
        yaw = ((yaw % 360f) + 360f) % 360f;
        if(yaw >= 315f || yaw < 45f) return BlockFace.SOUTH;
        if(yaw < 135f) return BlockFace.WEST;
        if(yaw < 225f) return BlockFace.NORTH;
        return BlockFace.EAST;
    }

}