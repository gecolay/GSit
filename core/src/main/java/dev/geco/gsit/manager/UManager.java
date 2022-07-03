package dev.geco.gsit.manager;

import java.io.*;
import java.net.*;
import java.util.*;

import org.bukkit.plugin.*;

public class UManager {

    private final Plugin plugin;

    private final String resource;

    private String spigotVersion = null;

    private boolean latestVersion = true;

    public UManager(Plugin Plugin, String Resource) {

        plugin = Plugin;
        resource = Resource;
    }

    private String requestSpigotVersion() {

        String vs = null;

        try(Closer c = Closer.create()) {

            HttpURLConnection con = (HttpURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resource).openConnection();

            con.setDoOutput(true);
            con.setRequestMethod("GET");
            con.setConnectTimeout(1000);

            vs = c.register(new BufferedReader(c.register(new InputStreamReader(con.getInputStream())))).readLine();
        } catch (Exception ignored) { }

        return vs;
    }

    public String getPluginVersion() { return plugin.getDescription().getVersion(); }

    public String getLatestVersion() { return spigotVersion; }

    public boolean checkVersion() {

        try {

            spigotVersion = requestSpigotVersion();

            String cv = getPluginVersion();

            if(spigotVersion == null || cv == null) return true;

            List<Integer> pl = new ArrayList<>(), vl = new ArrayList<>();

            for(String i : shortVersion(cv).split("\\.")) pl.add(Integer.parseInt(i));

            for(String i : shortVersion(spigotVersion).split("\\.")) vl.add(Integer.parseInt(i));

            if(pl.size() > vl.size()) {

                latestVersion = true;

                return true;
            }

            for(int i = 0; i < pl.size(); i++) {

                latestVersion = true;

                if(pl.get(i) > vl.get(i)) return true;
                else if(pl.get(i) < vl.get(i)) {

                    latestVersion = false;

                    return false;
                }
            }
        } catch (Exception | Error e) { latestVersion = true; }

        return latestVersion;
    }

    public boolean isLatestVersion() { return latestVersion; }

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