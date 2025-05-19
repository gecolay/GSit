package dev.geco.gsit.service;

import dev.geco.gsit.GSitMain;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class DataService {

    protected final int MAX_RETRIES = 3;

    private final GSitMain gSitMain;
    private Connection connection;
    private String type = null;
    private String host = null;
    private String port = null;
    private String database = null;
    private String user = null;
    private String password = null;
    private int retries = 0;

    public DataService(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    public boolean connect() {
        if(isConnected()) return true;
        File dataFile = new File(gSitMain.getDataFolder(), "data/data.yml");
        if(!dataFile.exists()) gSitMain.saveResource("data/data.yml", false);
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
        } catch(SQLException ignored) { }
        return false;
    }

    private boolean reconnect() {
        try {
            if(type.equals("sqlite")) Class.forName("org.sqlite.JDBC");
            connection = getConnection(false);
            if(connection != null) {
                if(!type.equals("sqlite")) connection = getConnection(true);
                if(connection != null) {
                    retries = 0;
                    return true;
                }
            }
        } catch(Throwable e) { gSitMain.getLogger().log(Level.SEVERE, "Could not connect to database!", e); }
        if(retries == MAX_RETRIES) return false;
        retries++;
        return reconnect();
    }

    private Connection getConnection(boolean withDatabase) throws SQLException {
        switch(type) {
            case "mysql":
                return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + (withDatabase ? "/" + database : "") + "?createDatabaseIfNotExist=true&useUnicode=true", user, password);
            case "sqlite":
                return DriverManager.getConnection("jdbc:sqlite:" + new File(gSitMain.getDataFolder(), "data/data.db").getPath());
        }
        return null;
    }

    public void execute(String query, Object... parameters) throws SQLException {
        ensureConnection();
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for(int i = 1; i <= parameters.length; i++) preparedStatement.setObject(i, parameters[i - 1]);
            preparedStatement.executeUpdate();
        }
    }

    public ResultSet executeAndGet(String query, Object... parameters) throws SQLException {
        ensureConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        for(int i = 1; i <= parameters.length; i++) preparedStatement.setObject(i, parameters[i - 1]);
        return preparedStatement.executeQuery();
    }

    private void ensureConnection() throws SQLException {
        if(isConnected()) return;
        if(!reconnect()) throw new SQLException("Failed to reconnect to the " + type + " database.");
    }

    public void close() { try { if(connection != null && !connection.isClosed()) connection.close(); } catch(SQLException ignored) { } }

}