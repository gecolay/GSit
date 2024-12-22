package dev.geco.gsit.manager;

import java.util.*;

import org.bukkit.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.*;
import dev.geco.gsit.objects.*;

public class CrawlManager {

    private final GSitMain GPM;
    private final boolean available;
    private int crawl_used = 0;
    private long crawl_used_nano = 0;
    private final List<IGCrawl> crawls = new ArrayList<>();

    public CrawlManager(GSitMain GPluginMain) {
        GPM = GPluginMain;
        available = GPM.getSVManager().isNewerOrVersion(18, 0);
    }

    public boolean isAvailable() { return available; }

    public int getCrawlUsedCount() { return crawl_used; }

    public long getCrawlUsedSeconds() { return crawl_used_nano / 1_000_000_000; }

    public void resetFeatureUsedCount() {
        crawl_used = 0;
        crawl_used_nano = 0;
    }

    public List<IGCrawl> getCrawls() { return new ArrayList<>(crawls); }

    public boolean isCrawling(Player Player) { return getCrawl(Player) != null; }

    public IGCrawl getCrawl(Player Player) { return crawls.stream().filter(crawl -> Player.equals(crawl.getPlayer())).findFirst().orElse(null); }

    public void clearCrawls() { for(IGCrawl crawl : getCrawls()) stopCrawl(crawl.getPlayer(), GetUpReason.PLUGIN); }

    public IGCrawl startCrawl(Player Player) {

        PrePlayerCrawlEvent preEvent = new PrePlayerCrawlEvent(Player);
        Bukkit.getPluginManager().callEvent(preEvent);
        if(preEvent.isCancelled()) return null;

        if(GPM.getCManager().CUSTOM_MESSAGE) GPM.getMManager().sendActionBarMessage(Player, "Messages.action-crawl-info");

        IGCrawl crawl = GPM.getEntityUtil().createCrawlObject(Player);
        crawl.start();

        crawls.add(crawl);

        crawl_used++;

        Bukkit.getPluginManager().callEvent(new PlayerCrawlEvent(crawl));

        return crawl;
    }

    public boolean stopCrawl(Player Player, GetUpReason Reason) {

        IGCrawl crawl = getCrawl(Player);
        if(crawl == null) return true;

        PrePlayerGetUpCrawlEvent preEvent = new PrePlayerGetUpCrawlEvent(crawl, Reason);
        Bukkit.getPluginManager().callEvent(preEvent);
        if(preEvent.isCancelled()) return false;

        crawls.remove(crawl);

        crawl.stop();

        Bukkit.getPluginManager().callEvent(new PlayerGetUpCrawlEvent(crawl, Reason));

        crawl_used_nano += crawl.getNano();

        return true;
    }

}