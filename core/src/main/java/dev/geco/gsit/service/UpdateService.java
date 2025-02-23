package dev.geco.gsit.service;

import dev.geco.gsit.GSitMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.logging.Level;

public class UpdateService {

    private final String REMOTE_URL = "https://api.spigotmc.org/legacy/update.php?resource=";
    private final GSitMain gSitMain;
    private LocalDate lastCheckDate = null;
    private String latestVersion = null;
    private boolean isLatestVersion = true;

    public UpdateService(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    public void checkForUpdates() {
        if(!gSitMain.getConfigService().CHECK_FOR_UPDATE) return;
        checkVersion();
        if(isLatestVersion) return;
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(!gSitMain.getPermissionService().hasPermission(player, "Update")) continue;
            gSitMain.getMessageService().sendMessage(player, "Plugin.plugin-update", "%Name%", GSitMain.NAME, "%NewVersion%", latestVersion, "%Version%", gSitMain.getDescription().getVersion(), "%Path%", gSitMain.getDescription().getWebsite());
        }
        gSitMain.getMessageService().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-update", "%Name%", GSitMain.NAME, "%NewVersion%", latestVersion, "%Version%", gSitMain.getDescription().getVersion(), "%Path%", gSitMain.getDescription().getWebsite());
    }

    public void checkForUpdates(Player player) {
        if(!gSitMain.getConfigService().CHECK_FOR_UPDATE) return;
        if(!gSitMain.getPermissionService().hasPermission(player, "Update")) return;
        checkVersion();
        if(isLatestVersion) return;
        gSitMain.getMessageService().sendMessage(player, "Plugin.plugin-update", "%Name%", GSitMain.NAME, "%NewVersion%", latestVersion, "%Version%", gSitMain.getDescription().getVersion(), "%Path%", gSitMain.getDescription().getWebsite());
    }

    private void getSpigotVersion(Consumer<String> versionConsumer) {
        gSitMain.getTaskService().run(() -> {
            try(InputStream inputStream = new URL(REMOTE_URL + GSitMain.RESOURCE_ID).openStream();
                Scanner scanner = new Scanner(inputStream)) {
                if(scanner.hasNext() && versionConsumer != null) versionConsumer.accept(scanner.next());
            } catch(IOException e) {
                if(e.getMessage().contains("50")) return;
                gSitMain.getLogger().log(Level.SEVERE, "Could not get remote version!", e);
            }
        }, false);
    }

    private void checkVersion() {
        LocalDate today = LocalDate.now();
        if(lastCheckDate != null && lastCheckDate.equals(today)) return;
        lastCheckDate = today;
        try {
            getSpigotVersion(spigotVersion -> {
                latestVersion = spigotVersion;
                if(latestVersion == null) {
                    isLatestVersion = true;
                    return;
                }
                String pluginVersion = gSitMain.getDescription().getVersion();
                String[] pluginVersionParts = getShortVersion(pluginVersion).split("\\.");
                String[] spigotVersionParts = getShortVersion(latestVersion).split("\\.");
                int minLength = Math.min(pluginVersionParts.length, spigotVersionParts.length);
                for(int i = 0; i < minLength; i++) {
                    int pluginPart = Integer.parseInt(pluginVersionParts[i]);
                    int spigotPart = Integer.parseInt(spigotVersionParts[i]);
                    if(pluginPart < spigotPart) {
                        isLatestVersion = false;
                        return;
                    } else if(pluginPart > spigotPart) {
                        isLatestVersion = true;
                        return;
                    }
                }
                isLatestVersion = pluginVersionParts.length >= spigotVersionParts.length;
            });
        } catch(Throwable e) { isLatestVersion = true; }
    }

    private String getShortVersion(String version) { return version.replaceAll("[\\[\\] ]", ""); }

}