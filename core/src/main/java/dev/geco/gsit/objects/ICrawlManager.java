package dev.geco.gsit.objects;

import org.bukkit.entity.Player;

public interface ICrawlManager {

    int getFeatureUsedCount();

    void resetFeatureUsedCount();

    boolean isCrawling(Player Player);

    IGCrawl getCrawl(Player Player);

    void clearCrawls();

    IGCrawl startCrawl(Player Player);

    boolean stopCrawl(IGCrawl Crawl, GetUpReason Reason);

}