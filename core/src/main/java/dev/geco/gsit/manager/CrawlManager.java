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

    public CrawlManager(GSitMain GPluginMain) {
        GPM = GPluginMain;
        available = NMSManager.hasPackageClass("objects.GCrawl");
    }

    public boolean isAvailable() { return available; }

    private int feature_used = 0;

    public int getFeatureUsedCount() { return feature_used; }

    public void resetFeatureUsedCount() { feature_used = 0; }

    private final List<IGCrawl> crawls = new ArrayList<>();

    public List<IGCrawl> getCrawls() { return new ArrayList<>(crawls); }

    public boolean isCrawling(Player Player) { return getCrawl(Player) != null; }

    public IGCrawl getCrawl(Player Player) { return getCrawls().stream().filter(crawl -> Player.equals(crawl.getPlayer())).findFirst().orElse(null); }

    public void clearCrawls() { for(IGCrawl crawl : getCrawls()) stopCrawl(crawl.getPlayer(), GetUpReason.PLUGIN); }

    public IGCrawl startCrawl(Player Player) {

        PrePlayerCrawlEvent preEvent = new PrePlayerCrawlEvent(Player);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return null;

        IGCrawl crawl = getCrawlInstance(Player);

        crawl.start();

        crawls.add(crawl);

        feature_used++;

        Bukkit.getPluginManager().callEvent(new PlayerCrawlEvent(crawl));

        return crawl;
    }

    private IGCrawl getCrawlInstance(Player Player) {
        try {
            Class<?> petClass = Class.forName("dev.geco.gsit.mcv." + NMSManager.getPackageVersion() + ".objects.GCrawl");
            return (IGCrawl) petClass.getConstructor(Player.class).newInstance(Player);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean stopCrawl(Player Player, GetUpReason Reason) {

        if(!isCrawling(Player)) return true;

        IGCrawl crawl = getCrawl(Player);

        PrePlayerGetUpCrawlEvent preEvent = new PrePlayerGetUpCrawlEvent(crawl, Reason);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return false;

        crawls.remove(crawl);

        crawl.stop();

        Bukkit.getPluginManager().callEvent(new PlayerGetUpCrawlEvent(crawl, Reason));

        return true;
    }

}