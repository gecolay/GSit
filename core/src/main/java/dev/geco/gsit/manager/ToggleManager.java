package dev.geco.gsit.manager;

import java.sql.*;
import java.util.*;

import dev.geco.gsit.GSitMain;

public class ToggleManager {

    private final GSitMain GPM;
    private final HashMap<UUID, Boolean> sit_cache = new HashMap<>();
    private final HashMap<UUID, Boolean> playersit_cache = new HashMap<>();
    private final HashMap<UUID, Boolean> crawl_cache = new HashMap<>();

    public ToggleManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    public void createTable() {
        try {
            GPM.getDManager().execute("CREATE TABLE IF NOT EXISTS sit_toggle (uuid TEXT);");
            GPM.getDManager().execute("CREATE TABLE IF NOT EXISTS player_toggle (uuid TEXT);");
            GPM.getDManager().execute("CREATE TABLE IF NOT EXISTS crawl_toggle (uuid TEXT);");
        } catch (SQLException e) { e.printStackTrace(); }
        sit_cache.clear();
        playersit_cache.clear();
        crawl_cache.clear();
    }

    public void clearToggleCache(UUID UUID) {
        sit_cache.remove(UUID);
        playersit_cache.remove(UUID);
        crawl_cache.remove(UUID);
    }

    public boolean canSit(UUID UUID) {
        if(sit_cache.containsKey(UUID)) return sit_cache.get(UUID);
        try {
            ResultSet resultSet = GPM.getDManager().executeAndGet("SELECT uuid FROM sit_toggle WHERE uuid = ?", UUID.toString());
            boolean canSit = GPM.getCManager().S_DEFAULT_SIT_MODE == !resultSet.next();
            sit_cache.put(UUID, canSit);
            return canSit;
        } catch (Throwable e) { e.printStackTrace(); }
        return true;
    }

    public boolean canPlayerSit(UUID UUID) {
        if(playersit_cache.containsKey(UUID)) return playersit_cache.get(UUID);
        try {
            ResultSet resultSet = GPM.getDManager().executeAndGet("SELECT uuid FROM player_toggle WHERE uuid = ?", UUID.toString());
            boolean canPlayerSit = GPM.getCManager().PS_DEFAULT_SIT_MODE == !resultSet.next();
            playersit_cache.put(UUID, canPlayerSit);
            return canPlayerSit;
        } catch (Throwable e) { e.printStackTrace(); }
        return true;
    }

    public boolean canCrawl(UUID UUID) {
        if(crawl_cache.containsKey(UUID)) return crawl_cache.get(UUID);
        try {
            ResultSet resultSet = GPM.getDManager().executeAndGet("SELECT uuid FROM crawl_toggle WHERE uuid = ?", UUID.toString());
            boolean canCrawl = GPM.getCManager().C_DEFAULT_CRAWL_MODE == !resultSet.next();
            crawl_cache.put(UUID, canCrawl);
            return canCrawl;
        } catch (Throwable e) { e.printStackTrace(); }
        return true;
    }

    public void setCanSit(UUID UUID, boolean Toggle) {
        try {
            boolean delete = (Toggle && GPM.getCManager().S_DEFAULT_SIT_MODE) || (!Toggle && !GPM.getCManager().S_DEFAULT_SIT_MODE);
            if(delete) GPM.getDManager().execute("DELETE FROM sit_toggle WHERE uuid = ?", UUID.toString());
            else GPM.getDManager().execute("INSERT INTO sit_toggle (uuid) VALUES (?)", UUID.toString());
            sit_cache.put(UUID, Toggle);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void setCanPlayerSit(UUID UUID, boolean PlayerToggle) {
        try {
            boolean delete = (PlayerToggle && GPM.getCManager().PS_DEFAULT_SIT_MODE) || (!PlayerToggle && !GPM.getCManager().PS_DEFAULT_SIT_MODE);
            if(delete) GPM.getDManager().execute("DELETE FROM player_toggle WHERE uuid = ?", UUID.toString());
            else GPM.getDManager().execute("INSERT INTO player_toggle (uuid) VALUES (?)", UUID.toString());
            playersit_cache.put(UUID, PlayerToggle);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void setCanCrawl(UUID UUID, boolean CrawlToggle) {
        try {
            boolean delete = (CrawlToggle && GPM.getCManager().C_DEFAULT_CRAWL_MODE) || (!CrawlToggle && !GPM.getCManager().C_DEFAULT_CRAWL_MODE);
            if(delete) GPM.getDManager().execute("DELETE FROM crawl_toggle WHERE uuid = ?", UUID.toString());
            else GPM.getDManager().execute("INSERT INTO crawl_toggle (uuid) VALUES (?)", UUID.toString());
            crawl_cache.put(UUID, CrawlToggle);
        } catch (SQLException e) { e.printStackTrace(); }
    }

}