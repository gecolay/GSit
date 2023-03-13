package dev.geco.gsit.manager;

import java.io.*;
import java.sql.*;

import org.bukkit.configuration.file.*;

import dev.geco.gsit.GSitMain;

public class DManager {

    private final GSitMain GPM;

    private final FileConfiguration dataConfig;

    private Connection connection;

    public DManager(GSitMain GPluginMain) {
        GPM = GPluginMain;
        File dataFile = new File(GPM.getDataFolder(), "data/data.yml");
        if(!dataFile.exists()) GPM.saveResource("data/data.yml", false);
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public String getType() { return dataConfig.getString("Database.type", "sqlite"); }

    public boolean connect() {
        try {
            String type = getType();
            if(type.equals("sqlite")) {
                connection = getConnection("sqlite", "", "", "", "", "");
                return true;
            }
            String host = dataConfig.getString("Database.host", "");
            String port = dataConfig.getString("Database.port", "");
            String database = dataConfig.getString("Database.database", "");
            String user = dataConfig.getString("Database.user", "");
            String password = dataConfig.getString("Database.password", "");
            connection = getConnection(type, host, port, "", user, password);
            execute("CREATE DATABASE IF NOT EXISTS " + database);
            connection = getConnection(type, host, port, database, user, password);
            return true;
        } catch (Exception e) { e.printStackTrace(); }
        connection = null;
        return false;
    }

    private Connection getConnection(String Type, String Host, String Port, String Database, String User, String Password) {
        try {
            switch (Type.toLowerCase()) {
                case "mysql":
                    return DriverManager.getConnection("jdbc:mysql://" + Host + ":" + Port + "/" + Database, User, Password);
                case "sqlite":
                    return DriverManager.getConnection("jdbc:sqlite:" + new File(GPM.getDataFolder(), "data/data.db").getPath());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean execute(String Query, Object... Data) {
        if(connection == null) return false;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(Query);
            for(int i = 1; i <= Data.length; i++) preparedStatement.setObject(i, Data[i - 1]);
            return preparedStatement.execute();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public ResultSet executeAndGet(String Query, Object... Data) {
        if(connection == null) return null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(Query);
            for(int i = 1; i <= Data.length; i++) preparedStatement.setObject(i, Data[i - 1]);
            return preparedStatement.executeQuery();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public void close() { try { if(connection != null) connection.close(); } catch (Exception ignored) { } }

}