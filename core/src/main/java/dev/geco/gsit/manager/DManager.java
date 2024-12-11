package dev.geco.gsit.manager;

import java.io.*;
import java.sql.*;

import org.bukkit.configuration.file.*;

import dev.geco.gsit.GSitMain;

public class DManager {

    private final GSitMain GPM;
    protected final int MAX_RETRIES = 3;
    private Connection connection;
    private String type = null;
    private String host = null;
    private String port = null;
    private String database = null;
    private String user = null;
    private String password = null;
    private int retries = 0;

    public DManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    public boolean connect() {
        if(isConnected()) return true;
        File dataFile = new File(GPM.getDataFolder(), "data/data.yml");
        if(!dataFile.exists()) GPM.saveResource("data/data.yml", false);
        FileConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        type = dataConfig.getString("Database.type", "sqlite").toLowerCase();
        host = dataConfig.getString("Database.host", "");
        port = dataConfig.getString("Database.port", "");
        database = dataConfig.getString("Database.database", "");
        user = dataConfig.getString("Database.user", "");
        password = dataConfig.getString("Database.password", "");
        return reconnect();
    }

    public boolean isConnected() {
        try {
            if(connection != null && !connection.isClosed()) return true;
        } catch (SQLException ignored) { }
        return false;
    }

    private boolean reconnect() {
        try {
            if(type.equals("sqlite")) Class.forName("org.sqlite.JDBC");
            connection = getConnection(false);
            if(connection != null) {
                if(!type.equals("sqlite")) {
                    execute("CREATE DATABASE IF NOT EXISTS " + database);
                    connection = getConnection(true);
                }
                if(connection != null) {
                    retries = 0;
                    return true;
                }
            }
        } catch (Throwable e) { e.printStackTrace(); }
        if(retries == MAX_RETRIES) return false;
        retries++;
        return reconnect();
    }

    private Connection getConnection(boolean WithDatabase) throws SQLException {
        switch(type) {
            case "mysql":
                return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + (WithDatabase ? "/" + database : ""), user, password);
            case "sqlite":
                return DriverManager.getConnection("jdbc:sqlite:" + new File(GPM.getDataFolder(), "data/data.db").getPath());
        }
        return null;
    }

    public void execute(String Query, Object... Data) throws SQLException {
        ensureConnection();
        try(PreparedStatement preparedStatement = connection.prepareStatement(Query)) {
            for(int i = 1; i <= Data.length; i++) preparedStatement.setObject(i, Data[i - 1]);
            preparedStatement.executeUpdate();
        }
    }

    public ResultSet executeAndGet(String Query, Object... Data) throws SQLException {
        ensureConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(Query);
        for(int i = 1; i <= Data.length; i++) preparedStatement.setObject(i, Data[i - 1]);
        return preparedStatement.executeQuery();
    }

    private void ensureConnection() throws SQLException {
        if(isConnected()) return;
        if(!reconnect()) throw new SQLException("Failed to reconnect to the " + type + " database.");
    }

    public void close() { try { if(connection != null && !connection.isClosed()) connection.close(); } catch (SQLException ignored) { } }

}