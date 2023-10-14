package dev.geco.gsit.manager;

import java.io.*;
import java.sql.*;

import org.bukkit.configuration.file.*;

import dev.geco.gsit.GSitMain;

public class DManager {

    private final GSitMain GPM;

    private Connection connection;

    public DManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    public boolean connect() {
        File dataFile = new File(GPM.getDataFolder(), "data/data.yml");
        if(!dataFile.exists()) GPM.saveResource("data/data.yml", false);
        FileConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        String type = dataConfig.getString("Database.type", "sqlite");
        String host = dataConfig.getString("Database.host", "");
        String port = dataConfig.getString("Database.port", "");
        String database = dataConfig.getString("Database.database", "");
        String user = dataConfig.getString("Database.user", "");
        String password = dataConfig.getString("Database.password", "");
        if(type.equals("sqlite")) return setupSQLiteConnection();
        else return setupConnection(type, host, port, database, user, password);
    }

    private boolean setupSQLiteConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = getConnection("sqlite", "", "", "", "", "");
            return connection != null;
        } catch (Exception e) { e.printStackTrace(); }
        connection = null;
        return false;
    }

    private boolean setupConnection(String Type, String Host, String Port, String Database, String User, String Password) {
        try {
            connection = getConnection(Type, Host, Port, Database, User, Password);
            if(connection == null) return false;
            execute("CREATE DATABASE IF NOT EXISTS " + Database);
            return true;
        } catch (SQLException e) { e.printStackTrace(); }
        connection = null;
        return false;
    }

    private Connection getConnection(String Type, String Host, String Port, String Database, String User, String Password) throws SQLException {
        switch(Type.toLowerCase()) {
            case "mysql":
                return DriverManager.getConnection("jdbc:mysql://" + Host + ":" + Port + "/" + Database, User, Password);
            case "sqlite":
                return DriverManager.getConnection("jdbc:sqlite:" + new File(GPM.getDataFolder(), "data/data.db").getPath());
        }
        return null;
    }

    public boolean execute(String Query, Object... Data) {
        if(connection == null) return false;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(Query);
            for(int i = 1; i <= Data.length; i++) preparedStatement.setObject(i, Data[i - 1]);
            return preparedStatement.execute();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public ResultSet executeAndGet(String Query, Object... Data) {
        if(connection == null) return null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(Query);
            for(int i = 1; i <= Data.length; i++) preparedStatement.setObject(i, Data[i - 1]);
            return preparedStatement.executeQuery();
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void close() { try { if(connection != null) connection.close(); } catch (SQLException ignored) { } }

}