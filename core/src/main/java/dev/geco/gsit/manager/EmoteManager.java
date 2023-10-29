package dev.geco.gsit.manager;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.jar.*;

import org.bukkit.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.*;
import dev.geco.gsit.objects.*;

public class EmoteManager {

    private final GSitMain GPM;

    public static final List<String> FILE_EXTENSIONS = Arrays.asList(".gex", ".yml");

    public EmoteManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    private int emote_used = 0;
    private long emote_used_seconds = 0;

    public int getEmoteUsedCount() { return emote_used; }
    public long getEmoteUsedSeconds() { return emote_used_seconds; }

    public void resetFeatureUsedCount() {
        emote_used = 0;
        emote_used_seconds = 0;
    }

    public void createTable() {
        GPM.getDManager().execute("CREATE TABLE IF NOT EXISTS emote_save (uuid TEXT, emote TEXT);");
    }

    private final HashMap<String, GEmote> available_emotes = new HashMap<>();

    public List<GEmote> getAvailableEmotes() { return new ArrayList<>(available_emotes.values()); }

    public GEmote getEmoteByName(String Name) { return available_emotes.get(Name.toLowerCase()); }

    public void reloadEmotes() {
        clearEmotes();
        try {
            File directory = new File(GPM.getDataFolder(), "emotes/");
            if(!directory.exists()) {
                try(JarFile jarFile = new JarFile(Paths.get(GPM.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toString())) {
                    Enumeration<JarEntry> jarFiles = jarFile.entries();
                    while(jarFiles.hasMoreElements()) {
                        JarEntry jarEntry = jarFiles.nextElement();
                        if(!jarEntry.getName().startsWith("emotes") || jarEntry.isDirectory()) continue;
                        if(!new File(GPM.getDataFolder(), jarEntry.getName()).exists()) GPM.saveResource(jarEntry.getName(), false);
                    }
                } catch (Throwable e) { e.printStackTrace(); }
            }
            if(!directory.exists()) return;
            for(File emoteFile : Objects.requireNonNull(directory.listFiles())) {
                if(FILE_EXTENSIONS.stream().anyMatch(extension -> emoteFile.getName().endsWith(extension))) {
                    GEmote emote = GPM.getEmoteUtil().createEmoteFromRawData(emoteFile);
                    if(emote != null) available_emotes.put(emote.getId(), GPM.getEmoteUtil().createEmoteFromRawData(emoteFile));
                }
            }
        } catch (Throwable e) { e.printStackTrace(); }
    }

    private final HashMap<Player, GEmote> emotes = new HashMap<>();

    public HashMap<Player, GEmote> getEmotes() { return new HashMap<>(emotes); }

    public boolean isEmoting(Player Player) { return getEmote(Player) != null; }

    public GEmote getEmote(Player Player) {

        for(Map.Entry<Player, GEmote> emote : getEmotes().entrySet()) if(Player.equals(emote.getKey())) return emote.getValue();
        return null;
    }

    public void clearEmotes() {
        for(Player player : getEmotes().keySet()) stopEmote(player);
        available_emotes.clear();
    }

    public boolean startEmote(Player Player, GEmote Emote) {

        PreEntityEmoteEvent preEvent = new PreEntityEmoteEvent(Player, Emote);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return false;

        if(!available_emotes.containsValue(Emote) || Emote.getParts().isEmpty()) return false;

        if(!stopEmote(Player)) return false;

        Emote.start(Player);

        emotes.put(Player, Emote);

        emote_used++;

        Bukkit.getPluginManager().callEvent(new EntityEmoteEvent(Player, Emote));

        return true;
    }

    public boolean stopEmote(Player Player) {

        if(!isEmoting(Player)) return true;

        GEmote emote = getEmote(Player);

        PreEntityStopEmoteEvent preEvent = new PreEntityStopEmoteEvent(Player, emote);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return false;

        emote.stop(Player);

        emotes.remove(Player);

        GPM.getDManager().execute("DELETE FROM emote_save WHERE uuid = ?", Player.getUniqueId().toString());

        Bukkit.getPluginManager().callEvent(new EntityStopEmoteEvent(Player, emote));

        emote_used_seconds += emote.getSeconds();

        return true;
    }

    public void restoreEmote(Player Player) {

        try {
            ResultSet resultSet = GPM.getDManager().executeAndGet("SELECT emote FROM emote_save WHERE uuid = ?", Player.getUniqueId().toString());
            if(!resultSet.next()) return;
            String name = resultSet.getString("emote");
            GEmote emote = getEmoteByName(name);
            if(emote != null) startEmote(Player, emote);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void saveEmote(Player Player) {

        GEmote emote = getEmote(Player);

        GPM.getDManager().execute("DELETE FROM emote_save WHERE uuid = ?", Player.getUniqueId().toString());
        GPM.getDManager().execute("INSERT INTO emote_save (uuid, emote) VALUES (?, ?)", Player.getUniqueId().toString(), emote.getId());
    }

}