package dev.geco.gsit.service;

import dev.geco.gsit.GSitMain;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

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
            gSitMain.getDataService().execute("CREATE TABLE IF NOT EXISTS gsit_sit_toggle (uuid TEXT);");
            gSitMain.getDataService().execute("CREATE TABLE IF NOT EXISTS gsit_player_toggle (uuid TEXT);");
            gSitMain.getDataService().execute("CREATE TABLE IF NOT EXISTS gsit_crawl_toggle (uuid TEXT);");
        } catch(SQLException e) { gSitMain.getLogger().log(Level.SEVERE, "Could not create toggle database tables!", e); }
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
            ResultSet resultSet = gSitMain.getDataService().executeAndGet("SELECT uuid FROM gsit_sit_toggle WHERE uuid = ?", entityUuid.toString());
            boolean canUseSit = gSitMain.getConfigService().S_DEFAULT_SIT_MODE == !resultSet.next();
            sitCache.put(entityUuid, canUseSit);
            return canUseSit;
        } catch(SQLException e) { gSitMain.getLogger().log(Level.SEVERE, "Could not check sit toggle!", e); }
        return true;
    }

    public boolean canPlayerUsePlayerSit(UUID playerUuid) {
        if(playersitCache.containsKey(playerUuid)) return playersitCache.get(playerUuid);
        try {
            ResultSet resultSet = gSitMain.getDataService().executeAndGet("SELECT uuid FROM gsit_player_toggle WHERE uuid = ?", playerUuid.toString());
            boolean canUsePlayerSit = gSitMain.getConfigService().PS_DEFAULT_SIT_MODE == !resultSet.next();
            playersitCache.put(playerUuid, canUsePlayerSit);
            return canUsePlayerSit;
        } catch(SQLException e) { gSitMain.getLogger().log(Level.SEVERE, "Could not check player sit toggle!", e); }
        return true;
    }

    public boolean canPlayerUseCrawl(UUID playerUuid) {
        if(crawlCache.containsKey(playerUuid)) return crawlCache.get(playerUuid);
        try {
            ResultSet resultSet = gSitMain.getDataService().executeAndGet("SELECT uuid FROM gsit_crawl_toggle WHERE uuid = ?", playerUuid.toString());
            boolean canUseCrawl = gSitMain.getConfigService().C_DEFAULT_CRAWL_MODE == !resultSet.next();
            crawlCache.put(playerUuid, canUseCrawl);
            return canUseCrawl;
        } catch(SQLException e) { gSitMain.getLogger().log(Level.SEVERE, "Could not check crawl toggle!", e); }
        return true;
    }

    public void setEntityCanUseSit(UUID entityUuid, boolean canUseSit) {
        try {
            boolean deleteEntry = (canUseSit && gSitMain.getConfigService().S_DEFAULT_SIT_MODE) || (!canUseSit && !gSitMain.getConfigService().S_DEFAULT_SIT_MODE);
            if(deleteEntry) gSitMain.getDataService().execute("DELETE FROM gsit_sit_toggle WHERE uuid = ?", entityUuid.toString());
            else gSitMain.getDataService().execute("INSERT INTO gsit_sit_toggle (uuid) VALUES (?)", entityUuid.toString());
            sitCache.put(entityUuid, canUseSit);
        } catch(SQLException e) { gSitMain.getLogger().log(Level.SEVERE, "Could not set sit toggle!", e); }
    }

    public void setPlayerCanUsePlayerSit(UUID playerUuid, boolean canUsePlayerSit) {
        try {
            boolean deleteEntry = (canUsePlayerSit && gSitMain.getConfigService().PS_DEFAULT_SIT_MODE) || (!canUsePlayerSit && !gSitMain.getConfigService().PS_DEFAULT_SIT_MODE);
            if(deleteEntry) gSitMain.getDataService().execute("DELETE FROM gsit_player_toggle WHERE uuid = ?", playerUuid.toString());
            else gSitMain.getDataService().execute("INSERT INTO gsit_player_toggle (uuid) VALUES (?)", playerUuid.toString());
            playersitCache.put(playerUuid, canUsePlayerSit);
        } catch(SQLException e) { gSitMain.getLogger().log(Level.SEVERE, "Could not set player sit toggle!", e); }
    }

    public void setPlayerCanUseCrawl(UUID playerUuid, boolean canUseCrawl) {
        try {
            boolean deleteEntry = (canUseCrawl && gSitMain.getConfigService().C_DEFAULT_CRAWL_MODE) || (!canUseCrawl && !gSitMain.getConfigService().C_DEFAULT_CRAWL_MODE);
            if(deleteEntry) gSitMain.getDataService().execute("DELETE FROM gsit_crawl_toggle WHERE uuid = ?", playerUuid.toString());
            else gSitMain.getDataService().execute("INSERT INTO gsit_crawl_toggle (uuid) VALUES (?)", playerUuid.toString());
            crawlCache.put(playerUuid, canUseCrawl);
        } catch(SQLException e) { gSitMain.getLogger().log(Level.SEVERE, "Could not set crawl toggle!", e); }
    }

}