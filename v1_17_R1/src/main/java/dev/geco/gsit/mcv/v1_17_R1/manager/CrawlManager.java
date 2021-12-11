package dev.geco.gsit.mcv.v1_17_R1.manager;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;
import dev.geco.gsit.mcv.v1_17_R1.objects.*;
import dev.geco.gsit.api.event.*;

public class CrawlManager implements ICrawlManager {

    private final GSitMain GPM;

    public CrawlManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    private int feature_used = 0;

    public int getFeatureUsedCount() { return feature_used; }

    public void resetFeatureUsedCount() { feature_used = 0; }

    private final List<IGCrawl> crawls = new ArrayList<IGCrawl>();

    public List<IGCrawl> getCrawls() { return new ArrayList<IGCrawl>(crawls); }

    public boolean isCrawling(Player Player) { return getCrawl(Player) != null; }

    public IGCrawl getCrawl(Player Player) {
        for(IGCrawl s : getCrawls()) if(Player.equals(s.getPlayer())) return s;
        return null;
    }

    public void clearCrawls() { for(IGCrawl c : getCrawls()) stopCrawl(c, GetUpReason.PLUGIN); }

    public IGCrawl startCrawl(Player Player) {

        PrePlayerCrawlEvent pplace = new PrePlayerCrawlEvent(Player);

        Bukkit.getPluginManager().callEvent(pplace);

        if(pplace.isCancelled()) return null;

        IGCrawl crawl = new GCrawl(Player);

        crawl.start();

        crawls.add(crawl);

        feature_used++;

        Bukkit.getPluginManager().callEvent(new PlayerCrawlEvent(crawl));

        return crawl;

    }

    public boolean stopCrawl(IGCrawl Crawl, GetUpReason Reason) {

        PrePlayerGetUpCrawlEvent pplaguce = new PrePlayerGetUpCrawlEvent(Crawl, Reason);

        Bukkit.getPluginManager().callEvent(pplaguce);

        if(pplaguce.isCancelled()) return false;

        crawls.remove(Crawl);

        Crawl.stop();

        Bukkit.getPluginManager().callEvent(new PlayerGetUpCrawlEvent(Crawl, Reason));

        return true;

    }

}