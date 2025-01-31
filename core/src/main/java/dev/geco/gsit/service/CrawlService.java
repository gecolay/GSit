package dev.geco.gsit.service;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.PlayerCrawlEvent;
import dev.geco.gsit.api.event.PlayerGetUpCrawlEvent;
import dev.geco.gsit.api.event.PrePlayerCrawlEvent;
import dev.geco.gsit.api.event.PrePlayerGetUpCrawlEvent;
import dev.geco.gsit.object.GetUpReason;
import dev.geco.gsit.object.IGCrawl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CrawlService {

    private final GSitMain gSitMain;
    private final boolean available;
    private final List<IGCrawl> crawls = new ArrayList<>();
    private int crawlUsageCount = 0;
    private long crawlUsageNanoTime = 0;

    public CrawlService(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        available = gSitMain.getVersionManager().isNewerOrVersion(18, 0);
    }

    public boolean isAvailable() { return available; }

    public List<IGCrawl> getAllCrawls() { return new ArrayList<>(crawls); }

    public boolean isPlayerCrawling(Player player) { return getCrawlByPlayer(player) != null; }

    public IGCrawl getCrawlByPlayer(Player player) { return crawls.stream().filter(crawl -> player.equals(crawl.getPlayer())).findFirst().orElse(null); }

    public void removeAllCrawls() { for(IGCrawl crawl : getAllCrawls()) stopCrawl(crawl.getPlayer(), GetUpReason.PLUGIN); }

    public IGCrawl startCrawl(Player player) {
        PrePlayerCrawlEvent prePlayerCrawlEvent = new PrePlayerCrawlEvent(player);
        Bukkit.getPluginManager().callEvent(prePlayerCrawlEvent);
        if(prePlayerCrawlEvent.isCancelled()) return null;

        if(gSitMain.getConfigService().CUSTOM_MESSAGE) gSitMain.getMessageService().sendActionBarMessage(player, "Messages.action-crawl-info");

        IGCrawl crawl = gSitMain.getEntityUtil().createCrawl(player);
        crawl.start();
        crawls.add(crawl);
        crawlUsageCount++;
        Bukkit.getPluginManager().callEvent(new PlayerCrawlEvent(crawl));

        return crawl;
    }

    public boolean stopCrawl(Player player, GetUpReason getUpReason) {
        IGCrawl crawl = getCrawlByPlayer(player);
        if(crawl == null) return true;

        PrePlayerGetUpCrawlEvent prePlayerGetUpCrawlEvent = new PrePlayerGetUpCrawlEvent(crawl, getUpReason);
        Bukkit.getPluginManager().callEvent(prePlayerGetUpCrawlEvent);
        if(prePlayerGetUpCrawlEvent.isCancelled()) return false;

        crawls.remove(crawl);
        crawl.stop();
        Bukkit.getPluginManager().callEvent(new PlayerGetUpCrawlEvent(crawl, getUpReason));
        crawlUsageNanoTime += crawl.getNano();

        return true;
    }

    public int getCrawlUsageCount() { return crawlUsageCount; }

    public long getCrawlUsageTimeInSeconds() { return crawlUsageNanoTime / 1_000_000_000; }

    public void resetCrawlUsageStats() {
        crawlUsageCount = 0;
        crawlUsageNanoTime = 0;
    }

}