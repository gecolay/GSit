package dev.geco.gsit.service;

import dev.geco.gsit.GSitMain;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class ToggleService {

    private final GSitMain gSitMain;
    private final HashMap<UUID, Boolean> sitCache = new HashMap<>();
    private final HashMap<UUID, Boolean> playersitCache = new HashMap<>();
    private final HashMap<UUID, Boolean> crawlCache = new HashMap<>();

    public ToggleService(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    public void createDataTables() {
        try {
            gSitMain.getDataService().execute("CREATE TABLE IF NOT EXISTS sit_toggle (uuid TEXT);");
            gSitMain.getDataService().execute("CREATE TABLE IF NOT EXISTS player_toggle (uuid TEXT);");
            gSitMain.getDataService().execute("CREATE TABLE IF NOT EXISTS crawl_toggle (uuid TEXT);");
        } catch(SQLException e) { e.printStackTrace(); }
        sitCache.clear();
        playersitCache.clear();
        crawlCache.clear();
    }

    public void clearEntitySitToggleCache(UUID entityUuid) {
        sitCache.remove(entityUuid);
        playersitCache.remove(entityUuid);
        crawlCache.remove(entityUuid);
    }

    public boolean canEntityUseSit(UUID entityUuid) {
        if(sitCache.containsKey(entityUuid)) return sitCache.get(entityUuid);
        try {
            ResultSet resultSet = gSitMain.getDataService().executeAndGet("SELECT uuid FROM sit_toggle WHERE uuid = ?", entityUuid.toString());
            boolean canUseSit = gSitMain.getConfigService().S_DEFAULT_SIT_MODE == !resultSet.next();
            sitCache.put(entityUuid, canUseSit);
            return canUseSit;
        } catch(Throwable e) { e.printStackTrace(); }
        return true;
    }

    public boolean canPlayerUsePlayerSit(UUID playerUuid) {
        if(playersitCache.containsKey(playerUuid)) return playersitCache.get(playerUuid);
        try {
            ResultSet resultSet = gSitMain.getDataService().executeAndGet("SELECT uuid FROM player_toggle WHERE uuid = ?", playerUuid.toString());
            boolean canUsePlayerSit = gSitMain.getConfigService().PS_DEFAULT_SIT_MODE == !resultSet.next();
            playersitCache.put(playerUuid, canUsePlayerSit);
            return canUsePlayerSit;
        } catch(Throwable e) { e.printStackTrace(); }
        return true;
    }

    public boolean canPlayerUseCrawl(UUID playerUuid) {
        if(crawlCache.containsKey(playerUuid)) return crawlCache.get(playerUuid);
        try {
            ResultSet resultSet = gSitMain.getDataService().executeAndGet("SELECT uuid FROM crawl_toggle WHERE uuid = ?", playerUuid.toString());
            boolean canUseCrawl = gSitMain.getConfigService().C_DEFAULT_CRAWL_MODE == !resultSet.next();
            crawlCache.put(playerUuid, canUseCrawl);
            return canUseCrawl;
        } catch(Throwable e) { e.printStackTrace(); }
        return true;
    }

    public void setEntityCanUseSit(UUID entityUuid, boolean canUseSit) {
        try {
            boolean deleteEntry = (canUseSit && gSitMain.getConfigService().S_DEFAULT_SIT_MODE) || (!canUseSit && !gSitMain.getConfigService().S_DEFAULT_SIT_MODE);
            if(deleteEntry) gSitMain.getDataService().execute("DELETE FROM sit_toggle WHERE uuid = ?", entityUuid.toString());
            else gSitMain.getDataService().execute("INSERT INTO sit_toggle (uuid) VALUES (?)", entityUuid.toString());
            sitCache.put(entityUuid, canUseSit);
        } catch(SQLException e) { e.printStackTrace(); }
    }

    public void setPlayerCanUsePlayerSit(UUID playerUuid, boolean canUsePlayerSit) {
        try {
            boolean deleteEntry = (canUsePlayerSit && gSitMain.getConfigService().PS_DEFAULT_SIT_MODE) || (!canUsePlayerSit && !gSitMain.getConfigService().PS_DEFAULT_SIT_MODE);
            if(deleteEntry) gSitMain.getDataService().execute("DELETE FROM player_toggle WHERE uuid = ?", playerUuid.toString());
            else gSitMain.getDataService().execute("INSERT INTO player_toggle (uuid) VALUES (?)", playerUuid.toString());
            playersitCache.put(playerUuid, canUsePlayerSit);
        } catch(SQLException e) { e.printStackTrace(); }
    }

    public void setPlayerCanUseCrawl(UUID playerUuid, boolean canUseCrawl) {
        try {
            boolean deleteEntry = (canUseCrawl && gSitMain.getConfigService().C_DEFAULT_CRAWL_MODE) || (!canUseCrawl && !gSitMain.getConfigService().C_DEFAULT_CRAWL_MODE);
            if(deleteEntry) gSitMain.getDataService().execute("DELETE FROM crawl_toggle WHERE uuid = ?", playerUuid.toString());
            else gSitMain.getDataService().execute("INSERT INTO crawl_toggle (uuid) VALUES (?)", playerUuid.toString());
            crawlCache.put(playerUuid, canUseCrawl);
        } catch(SQLException e) { e.printStackTrace(); }
    }

}