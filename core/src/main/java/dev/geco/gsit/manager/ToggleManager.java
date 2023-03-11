package dev.geco.gsit.manager;

import java.sql.*;
import java.util.*;

import dev.geco.gsit.GSitMain;

public class ToggleManager {

    private final GSitMain GPM;

    public ToggleManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    public void createTable() {
        GPM.getDManager().execute("CREATE TABLE IF NOT EXISTS sit_toggle (uuid TEXT);");
        GPM.getDManager().execute("CREATE TABLE IF NOT EXISTS player_toggle (uuid TEXT);");
        GPM.getDManager().execute("CREATE TABLE IF NOT EXISTS crawl_toggle (uuid TEXT);");
    }

    public boolean canSit(UUID UUID) {
        try {
            ResultSet resultSet = GPM.getDManager().executeAndGet("SELECT uuid FROM sit_toggle WHERE uuid = ?", UUID.toString());
            return GPM.getCManager().S_DEFAULT_SIT_MODE == !resultSet.next();
        } catch (Exception e) { e.printStackTrace(); }
        return true;
    }

    public boolean canPlayerSit(UUID UUID) {
        try {
            ResultSet resultSet = GPM.getDManager().executeAndGet("SELECT uuid FROM player_toggle WHERE uuid = ?", UUID.toString());
            return GPM.getCManager().PS_DEFAULT_SIT_MODE == !resultSet.next();
        } catch (Exception e) { e.printStackTrace(); }
        return true;
    }

    public boolean canCrawl(UUID UUID) {
        try {
            ResultSet resultSet = GPM.getDManager().executeAndGet("SELECT uuid FROM crawl_toggle WHERE uuid = ?", UUID.toString());
            return !resultSet.next();
        } catch (Exception e) { e.printStackTrace(); }
        return true;
    }

    public void setCanSit(UUID UUID, boolean Toggle) {

        if((Toggle && GPM.getCManager().S_DEFAULT_SIT_MODE) || (!Toggle && !GPM.getCManager().S_DEFAULT_SIT_MODE)) GPM.getDManager().execute("DELETE FROM sit_toggle WHERE uuid = ?", UUID.toString());
        else GPM.getDManager().execute("INSERT INTO sit_toggle (uuid) VALUES (?)", UUID.toString());
    }

    public void setCanPlayerSit(UUID UUID, boolean PlayerToggle) {

        if((PlayerToggle && GPM.getCManager().PS_DEFAULT_SIT_MODE) || (!PlayerToggle && !GPM.getCManager().PS_DEFAULT_SIT_MODE)) GPM.getDManager().execute("DELETE FROM player_toggle WHERE uuid = ?", UUID.toString());
        else GPM.getDManager().execute("INSERT INTO player_toggle (uuid) VALUES (?)", UUID.toString());
    }

    public void setCanCrawl(UUID UUID, boolean Toggle) {

        if(Toggle) GPM.getDManager().execute("DELETE FROM crawl_toggle WHERE uuid = ?", UUID.toString());
        else GPM.getDManager().execute("INSERT INTO crawl_toggle (uuid) VALUES (?)", UUID.toString());
    }

}