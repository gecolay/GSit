package dev.geco.gsit.service;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.PlayerCrawlEvent;
import dev.geco.gsit.api.event.PlayerStopCrawlEvent;
import dev.geco.gsit.api.event.PrePlayerCrawlEvent;
import dev.geco.gsit.api.event.PrePlayerStopCrawlEvent;
import dev.geco.gsit.model.Crawl;
import dev.geco.gsit.model.CrawlType;
import dev.geco.gsit.model.StopReason;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class CrawlService {

    private final GSitMain gSitMain;
    private final boolean available;
    private final HashMap<UUID, Crawl> crawls = new HashMap<>();
    private final HashMap<UUID, Long> doubleSneakCrawlPlayers = new HashMap<>();
    private final HashMap<UUID, Boolean> corridorCrawlPlayers = new HashMap<>();
    private int crawlCount = 0;
    private long crawlTime = 0;

    public CrawlService(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        available = gSitMain.getVersionManager().isNewerOrVersion(1, 18);
    }

    public boolean isAvailable() { return available; }

    public HashMap<UUID, Crawl> getAllCrawls() { return crawls; }

    public HashMap<UUID, Long> getDoubleSneakCrawlPlayers() { return doubleSneakCrawlPlayers; }

    public boolean isPlayerCrawling(Player player) { return crawls.containsKey(player.getUniqueId()); }

    public Crawl getCrawlByPlayer(Player player) { return crawls.get(player.getUniqueId()); }

    public HashMap<UUID, Boolean> getCorridorCrawlPlayers() { return corridorCrawlPlayers; }

    public void removeAllCrawls() { for(Crawl crawl : new ArrayList<>(crawls.values())) stopCrawl(crawl, StopReason.PLUGIN); }

    public void clearPlayerCrawlTracking(UUID playerUuid) {
        doubleSneakCrawlPlayers.remove(playerUuid);
        corridorCrawlPlayers.remove(playerUuid);
    }

    public Crawl startCrawl(Player player, CrawlType crawlType) { return startCrawl(player, crawlType, gSitMain.getConfigService().C_LAYERS); }

    public Crawl startCrawl(Player player, CrawlType crawlType, int layers) {
        PrePlayerCrawlEvent prePlayerCrawlEvent = new PrePlayerCrawlEvent(player, crawlType);
        Bukkit.getPluginManager().callEvent(prePlayerCrawlEvent);
        if(prePlayerCrawlEvent.isCancelled()) return null;

        if(gSitMain.getConfigService().CUSTOM_MESSAGE) gSitMain.getMessageService().sendActionBarMessage(player, "Messages.action-crawl-info");

        Crawl crawl = gSitMain.getEntityUtil().createCrawl(player, crawlType, layers);
        if(crawl == null) return null;

        crawl.start();
        crawls.put(player.getUniqueId(), crawl);
        if(crawlType == CrawlType.CORRIDOR) corridorCrawlPlayers.put(player.getUniqueId(), false);
        crawlCount++;
        Bukkit.getPluginManager().callEvent(new PlayerCrawlEvent(crawl));

        return crawl;
    }

    public boolean stopCrawl(Crawl crawl, StopReason stopReason) {
        PrePlayerStopCrawlEvent prePlayerStopCrawlEvent = new PrePlayerStopCrawlEvent(crawl, stopReason);
        Bukkit.getPluginManager().callEvent(prePlayerStopCrawlEvent);
        if(prePlayerStopCrawlEvent.isCancelled() && stopReason.isCancellable()) return false;

        crawls.remove(crawl.getPlayer().getUniqueId());
        corridorCrawlPlayers.remove(crawl.getPlayer().getUniqueId());
        crawl.stop();
        Bukkit.getPluginManager().callEvent(new PlayerStopCrawlEvent(crawl, stopReason));
        crawlTime += crawl.getLifetimeInNanoSeconds();

        return true;
    }

    public int getCrawlCount() { return this.crawlCount; }

    public int getCrawlTime() { return Math.toIntExact(this.crawlTime / 1_000_000_000); }

    public void resetCrawlStats() {
        crawlCount = 0;
        crawlTime = 0;
    }

}