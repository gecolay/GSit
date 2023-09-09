package dev.geco.gsit.manager;

import java.io.*;
import java.util.*;

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

    private final List<String> EMOTE_FILES = new ArrayList<>(); {
        EMOTE_FILES.add("happy");
    }

    private final List<GEmote> available_emotes = new ArrayList<>();

    public List<GEmote> getAvailableEmotes() { return new ArrayList<>(available_emotes); }

    public GEmote getEmoteByName(String Name) { return available_emotes.stream().filter(e -> e.getId().equalsIgnoreCase(Name)).findFirst().orElse(null); }

    public void reloadEmotes() {

        clearEmotes();

        try {

            File directory = new File(GPM.getDataFolder(), "emotes/");

            if(!directory.exists()) for(String emote : EMOTE_FILES) if(!new File(GPM.getDataFolder(), "emotes/" + emote + ".gex").exists()) GPM.saveResource("emotes/" + emote + ".gex", false);

            if(!directory.exists()) return;

            for(File emoteFile : directory.listFiles()) {

                if(emoteFile.getName().toLowerCase().endsWith(".gex")) {

                    GEmote emote = GPM.getEmoteUtil().createEmoteFromRawData(emoteFile);

                    if(emote != null) available_emotes.add(GPM.getEmoteUtil().createEmoteFromRawData(emoteFile));
                }
            }
        } catch (Exception ignored) { }
    }

    private final HashMap<LivingEntity, GEmote> emotes = new HashMap<>();

    public HashMap<LivingEntity, GEmote> getEmotes() { return new HashMap<>(emotes); }

    public boolean isEmoting(LivingEntity Entity) { return getEmote(Entity) != null; }

    public GEmote getEmote(LivingEntity Entity) {

        for(Map.Entry<LivingEntity, GEmote> emote : getEmotes().entrySet()) if(Entity.equals(emote.getKey())) return emote.getValue();
        return null;
    }

    public void clearEmotes() {
        for(LivingEntity entity : getEmotes().keySet()) stopEmote(entity);
        available_emotes.clear();
    }

    public boolean startEmote(LivingEntity Entity, GEmote Emote) {

        PreEntityEmoteEvent preEvent = new PreEntityEmoteEvent(Entity, Emote);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return false;

        if(!available_emotes.contains(Emote) || Emote.getParts().isEmpty()) return false;

        if(!stopEmote(Entity)) return false;

        Emote.start(Entity);

        emotes.put(Entity, Emote);

        feature_used++;

        Bukkit.getPluginManager().callEvent(new EntityEmoteEvent(Entity, Emote));

        return true;
    }

    public boolean stopEmote(LivingEntity Entity) {

        if(!isEmoting(Entity)) return true;

        GEmote emote = getEmote(Entity);

        PreEntityStopEmoteEvent preEvent = new PreEntityStopEmoteEvent(Entity, emote);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return false;

        emote.stop(Entity);

        emotes.remove(Entity);

        Bukkit.getPluginManager().callEvent(new EntityStopEmoteEvent(Entity, emote));

        return true;
    }

}