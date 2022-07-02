package dev.geco.gsit.manager;

import java.util.*;

import org.bukkit.entity.*;

import dev.geco.gsit.objects.*;

public interface ICrawlManager {

    int getFeatureUsedCount();

    void resetFeatureUsedCount();

    List<IGCrawl> getCrawls();

    boolean isCrawling(Player Player);

    IGCrawl getCrawl(Player Player);

    void clearCrawls();

    IGCrawl startCrawl(Player Player);

    boolean stopCrawl(Player Player, GetUpReason Reason);

}