package dev.geco.gsit.manager;

import java.util.*;

import dev.geco.gsit.objects.GetUpReason;
import dev.geco.gsit.objects.IGCrawl;
import org.bukkit.entity.Player;

public interface ICrawlManager {

    int getFeatureUsedCount();

    void resetFeatureUsedCount();

    List<IGCrawl> getCrawls();

    boolean isCrawling(Player Player);

    IGCrawl getCrawl(Player Player);

    void clearCrawls();

    IGCrawl startCrawl(Player Player);

    boolean stopCrawl(IGCrawl Crawl, GetUpReason Reason);

}