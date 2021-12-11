package dev.geco.gsit.mcv.v1_17_R1.manager;

import java.util.*;

import org.bukkit.entity.Player;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;
import dev.geco.gsit.mcv.v1_17_R1.objects.*;

public class CrawlManager implements ICrawlManager {

    private final GSitMain GPM;

    public CrawlManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    private int feature_used = 0;

    public int getFeatureUsedCount() { return feature_used; }

    public void resetFeatureUsedCount() { feature_used = 0; }

    private final List<IGCrawl> crawls = new ArrayList<IGCrawl>();

    public boolean isCrawling(Player Player) { return getCrawl(Player) != null; }

    public IGCrawl getCrawl(Player Player) {
        for(IGCrawl s : crawls) if(Player.equals(s.getPlayer())) return s;
        return null;
    }

    public void clearCrawls() { for(IGCrawl c : crawls) stopCrawl(c, GetUpReason.PLUGIN); }

    public IGCrawl startCrawl(Player Player) {

        IGCrawl crawl = new GCrawl(Player);

        crawl.start();

        crawls.add(crawl);

        feature_used++;

        return crawl;

    }

    public boolean stopCrawl(IGCrawl Crawl, GetUpReason Reason) {

        crawls.remove(Crawl);

        Crawl.stop();

        return true;

    }

}