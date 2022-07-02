package dev.geco.gsit.manager;

import java.io.*;
import java.util.*;

import dev.geco.gsit.api.event.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;
import dev.geco.gsit.values.*;

public class EmoteManager implements IEmoteManager {

    private final GSitMain GPM;

    public EmoteManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    private int feature_used = 0;

    public int getFeatureUsedCount() { return feature_used; }

    public void resetFeatureUsedCount() { feature_used = 0; }

    private final List<GEmote> available_emotes = new ArrayList<>();

    public List<GEmote> getAvailableEmotes() { return new ArrayList<>(available_emotes); }

    public GEmote getEmoteByName(String Name) { return available_emotes.stream().filter(e -> e.getId().equalsIgnoreCase(Name)).findFirst().orElse(null); }

    public List<GEmote> reloadEmotes() {
        clearEmotes();
        available_emotes.clear();
        try {
            File path = new File("plugins/" + GPM.NAME + "/" + PluginValues.EMOTES_PATH);
            if(!path.exists()) path.mkdirs();
            for(File f : path.listFiles()) {
                String fn = f.getName().toLowerCase();
                if(fn.endsWith(PluginValues.GEX_FILETYP)) available_emotes.add(GPM.getEmoteUtil().createEmoteFromRawData(f));
            }
        } catch (Exception ignored) { }
        return getAvailableEmotes();
    }

    private final HashMap<LivingEntity, GEmote> emotes = new HashMap<>();

    public HashMap<LivingEntity, GEmote> getEmotes() { return new HashMap<>(emotes); }

    public boolean isEmoting(LivingEntity Entity) { return getEmote(Entity) != null; }

    public GEmote getEmote(LivingEntity Entity) {
        for(Map.Entry<LivingEntity, GEmote> e : getEmotes().entrySet()) if(Entity.equals(e.getKey())) return e.getValue();
        return null;
    }

    public void clearEmotes() { for(LivingEntity e : getEmotes().keySet()) stopEmote(e); }

    public boolean startEmote(LivingEntity Entity, GEmote Emote) {

        PreEntityEmoteEvent preEvent = new PreEntityEmoteEvent(Entity, Emote);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return false;

        if(!available_emotes.contains(Emote) || Emote.getParts().size() == 0) return false;

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