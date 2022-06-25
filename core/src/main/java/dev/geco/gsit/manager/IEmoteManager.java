package dev.geco.gsit.manager;

import java.util.*;

import org.bukkit.entity.*;

import dev.geco.gsit.objects.*;

public interface IEmoteManager {

    int getFeatureUsedCount();

    void resetFeatureUsedCount();

    List<GEmote> getAvailableEmotes();

    GEmote getEmoteByName(String Name);

    List<GEmote> reloadEmotes();

    HashMap<LivingEntity, GEmote> getEmotes();

    void clearEmotes();

    boolean isEmoting(LivingEntity Entity);

    GEmote getEmote(LivingEntity Entity);

    boolean startEmote(LivingEntity Entity, GEmote Emote);

    boolean stopEmote(LivingEntity Entity);

}