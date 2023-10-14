package dev.geco.gsit.manager;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

import org.bukkit.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.*;
import dev.geco.gsit.objects.*;

public class EmoteManager {

    private final GSitMain GPM;

    public EmoteManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    private int feature_used = 0;

    public int getFeatureUsedCount() { return feature_used; }

    public void resetFeatureUsedCount() { feature_used = 0; }

    private final List<GEmote> available_emotes = new ArrayList<>();

    public List<GEmote> getAvailableEmotes() { return new ArrayList<>(available_emotes); }

    public GEmote getEmoteByName(String Name) { return available_emotes.stream().filter(e -> e.getId().equalsIgnoreCase(Name)).findFirst().orElse(null); }

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
                if(emoteFile.getName().toLowerCase().endsWith(".gex")) {
                    GEmote emote = GPM.getEmoteUtil().createEmoteFromRawData(emoteFile);
                    if(emote != null) available_emotes.add(GPM.getEmoteUtil().createEmoteFromRawData(emoteFile));
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

        if(!available_emotes.contains(Emote) || Emote.getParts().isEmpty()) return false;

        if(!stopEmote(Player)) return false;

        Emote.start(Player);

        emotes.put(Player, Emote);

        feature_used++;

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

        Bukkit.getPluginManager().callEvent(new EntityStopEmoteEvent(Player, emote));

        return true;
    }

}