package dev.geco.gsit.manager;

import java.io.*;
import java.net.*;
import java.util.*;

import org.bukkit.plugin.Plugin;

public class UManager {

    private final Plugin pl;

    private final String r;

    private String s = null;

    private boolean v = true;

    public UManager(Plugin Pl, String Resource) {
        pl = Pl;
        r = Resource;
    }

    private String requestSpigotVersion() {
        String vs = null;
        try(Closer c = Closer.create()) {
            HttpURLConnection con = (HttpURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=" + r).openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("GET");
            con.setConnectTimeout(1000);
            vs = c.register(new BufferedReader(c.register(new InputStreamReader(con.getInputStream())))).readLine();
        } catch(IOException e) { }
        return vs;
    }

    public String getPluginVersion() { return pl.getDescription().getVersion(); }

    public String getLatestVersion() { return s; }

    public boolean checkVersion() {
        s = requestSpigotVersion();
        String cv = getPluginVersion();
        if(s == null || cv == null) return true;
        List<Integer> pl = new ArrayList<>(), vl = new ArrayList<>();
        for(String i : shortVersion(cv).split("\\.")) pl.add(Integer.parseInt(i));
        for(String i : shortVersion(s).split("\\.")) vl.add(Integer.parseInt(i));
        if(pl.size() > vl.size()) {
            v = true;
            return true;
        }
        for(int i = 0; i < pl.size(); i++) {
            v = true;
            if(pl.get(i) > vl.get(i)) return true;
            else if(pl.get(i) < vl.get(i)) {
                v = false;
                return false;
            }
        }
        return v;
    }

    public boolean isLatestVersion() { return v; }

    private String shortVersion(String V) { return V.replace(" ", "").replace("[", "").replace("]", ""); }

    public boolean updatePlugin() { return false; }

    private static class Closer implements Closeable {

        private final List<Closeable> l = new ArrayList<>();

        public static Closer create() { return new Closer(); }

        public <C extends Closeable> C register(C c) {
            l.add(c);
            return c;
        }

        @Override
        public void close() { for(Closeable c : l) closeQuietly(c); }

        public void closeQuietly(Closeable c) { try { c.close(); } catch (Throwable e) { } }

    }

}