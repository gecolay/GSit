package dev.geco.gsit.manager;

import java.io.*;
import java.net.*;
import java.util.*;

import org.bukkit.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;

public class UManager {

    private final GSitMain GPM;

    private String spigotVersion = null;

    private boolean latestVersion = true;

    public UManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    public void checkForUpdates() {

        if(GPM.getCManager().CHECK_FOR_UPDATE) {

            checkVersion();

            if(!latestVersion) {

                for(Player player : Bukkit.getOnlinePlayers()) if(GPM.getPManager().hasPermission(player, "Update")) GPM.getMManager().sendMessage(player, "Plugin.plugin-update", "%Name%", GPM.NAME, "%NewVersion%", spigotVersion, "%Version%", GPM.getDescription().getVersion(), "%Path%", GPM.getDescription().getWebsite());

                GPM.getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-update", "%Name%", GPM.NAME, "%NewVersion%", spigotVersion, "%Version%", GPM.getDescription().getVersion(), "%Path%", GPM.getDescription().getWebsite());
            }
        }
    }

    public void loginCheckForUpdates(Player Player) {

        if(GPM.getCManager().CHECK_FOR_UPDATE && !latestVersion) {

            if(GPM.getPManager().hasPermission(Player, "Update")) GPM.getMManager().sendMessage(Player, "Plugin.plugin-update", "%Name%", GPM.NAME, "%NewVersion%", spigotVersion, "%Version%", GPM.getDescription().getVersion(), "%Path%", GPM.getDescription().getWebsite());
        }
    }

    private String getSpigotVersion() {

        String version = null;

        try(Closer closer = Closer.create()) {

            HttpURLConnection urlConnection = (HttpURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=" + GPM.RESOURCE).openConnection();

            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(1000);

            version = closer.register(new BufferedReader(closer.register(new InputStreamReader(urlConnection.getInputStream())))).readLine();
        } catch (Exception ignored) { }

        return version;
    }

    private void checkVersion() {

        try {

            spigotVersion = getSpigotVersion();

            String pluginVersion = GPM.getDescription().getVersion();

            if(spigotVersion == null) return;

            List<Integer> versionString = new ArrayList<>(), vl = new ArrayList<>();

            for(String i : shortVersion(pluginVersion).split("\\.")) versionString.add(Integer.parseInt(i));

            for(String i : shortVersion(spigotVersion).split("\\.")) vl.add(Integer.parseInt(i));

            if(versionString.size() > vl.size()) {

                latestVersion = true;

                return;
            }

            for(int i = 0; i < versionString.size(); i++) {

                latestVersion = true;

                if(versionString.get(i) > vl.get(i)) return;
                else if(versionString.get(i) < vl.get(i)) {

                    latestVersion = false;
                    return;
                }
            }
        } catch (Throwable e) { latestVersion = true; }
    }

    private String shortVersion(String V) { return V.replace(" ", "").replace("[", "").replace("]", ""); }

    private static class Closer implements Closeable {

        private final List<Closeable> l = new ArrayList<>();

        public static Closer create() { return new Closer(); }

        public <C extends Closeable> C register(C c) {

            l.add(c);

            return c;
        }

        @Override
        public void close() { for(Closeable c : l) closeQuietly(c); }

        public void closeQuietly(Closeable c) { try { c.close(); } catch (Exception ignored) { } }
    }

}