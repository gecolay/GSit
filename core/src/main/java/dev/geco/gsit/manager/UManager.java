package dev.geco.gsit.manager;

import java.io.*;
import java.net.*;
import java.time.*;
import java.util.*;
import java.util.function.*;

import org.bukkit.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;

public class UManager {

    private final GSitMain GPM;
    private LocalDate lastCheck = null;
    private String spigotVersion = null;
    private boolean latestVersion = true;

    public UManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    public void checkForUpdates() {
        if(!GPM.getCManager().CHECK_FOR_UPDATE) return;
        checkVersion();
        if(latestVersion) return;
        for(Player player : Bukkit.getOnlinePlayers()) if(GPM.getPManager().hasPermission(player, "Update")) GPM.getMManager().sendMessage(player, "Plugin.plugin-update", "%Name%", GSitMain.NAME, "%NewVersion%", spigotVersion, "%Version%", GPM.getDescription().getVersion(), "%Path%", GPM.getDescription().getWebsite());
        GPM.getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-update", "%Name%", GSitMain.NAME, "%NewVersion%", spigotVersion, "%Version%", GPM.getDescription().getVersion(), "%Path%", GPM.getDescription().getWebsite());
    }

    public void loginCheckForUpdates(Player Player) {
        if(!GPM.getCManager().CHECK_FOR_UPDATE) return;
        if(!GPM.getPManager().hasPermission(Player, "Update")) return;
        checkVersion();
        if(latestVersion) return;
        GPM.getMManager().sendMessage(Player, "Plugin.plugin-update", "%Name%", GSitMain.NAME, "%NewVersion%", spigotVersion, "%Version%", GPM.getDescription().getVersion(), "%Path%", GPM.getDescription().getWebsite());
    }

    private void getSpigotVersion(final Consumer<String> VersionConsumer) {
        GPM.getTManager().run(() -> {
            try(InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + GPM.RESOURCE).openStream();
                 Scanner scanner = new Scanner(inputStream)) {
                if(scanner.hasNext() && VersionConsumer != null) VersionConsumer.accept(scanner.next());
            } catch (IOException e) {
                if(e.getMessage().contains("50")) return;
                e.printStackTrace();
            }
        }, false);
    }

    private void checkVersion() {
        LocalDate today = LocalDate.now();
        if(lastCheck != null && lastCheck.equals(today)) return;
        lastCheck = today;
        try {
            getSpigotVersion(sVersion -> {
                spigotVersion = sVersion;
                if(spigotVersion == null) {
                    latestVersion = true;
                    return;
                }
                String pluginVersion = GPM.getDescription().getVersion();
                String[] pluginVersionParts = shortVersion(pluginVersion).split("\\.");
                String[] spigotVersionParts = shortVersion(spigotVersion).split("\\.");
                int minLength = Math.min(pluginVersionParts.length, spigotVersionParts.length);
                for(int i = 0; i < minLength; i++) {
                    int pluginPart = Integer.parseInt(pluginVersionParts[i]);
                    int spigotPart = Integer.parseInt(spigotVersionParts[i]);
                    if(pluginPart < spigotPart) {
                        latestVersion = false;
                        return;
                    } else if(pluginPart > spigotPart) {
                        latestVersion = true;
                        return;
                    }
                }
                latestVersion = pluginVersionParts.length >= spigotVersionParts.length;
            });
        } catch (Throwable e) { latestVersion = true; }
    }

    private String shortVersion(String Version) { return Version.replaceAll("[\\[\\] ]", ""); }

}