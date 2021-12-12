package dev.geco.gsit.manager;

import java.util.*;

import org.bukkit.*;

import dev.geco.gsit.GSitMain;

public class CManager {

    public boolean CHECK_FOR_UPDATES;

    public boolean DEBUG;

    public boolean CHECK_FEATURE_PERMISSIONS;

    public boolean GET_UP_DAMAGE;

    public boolean REST_SAME_BLOCK;


    public boolean S_BLOCK_CENTER;

    public HashMap<Material, Double> S_SITMATERIALS = new HashMap<Material, Double>();

    public boolean S_ALLOW_UNSAFE;

    public boolean S_GET_UP_RETURN;

    public boolean S_EMPTY_HAND_ONLY;

    public double S_MAX_DISTANCE;

    public boolean S_SHOW_SIT_MESSAGE;

    public boolean S_DEFAULT_SIT_MODE;


    public boolean PS_USE_PLAYERSIT;

    public boolean PS_USE_PLAYERSIT_NPC;

    public long PS_MAX_STACK;

    public boolean PS_SNEAK_EJECTS;

    public boolean PS_EMPTY_HAND_ONLY;

    public boolean PS_SHOW_SIT_MESSAGE;

    public boolean PS_DEFAULT_SIT_MODE;



    public boolean L_BLOCK_CENTER;

    public boolean L_SHOW_LAY_MESSAGE;

    public boolean L_INTERACT;

    public boolean L_RESET_TIME_SINCE_REST;

    public boolean L_SNORING_SOUNDS;

    public boolean L_SNORING_NIGHT_ONLY;

    public boolean L_NIGHT_SKIP;


    public boolean C_GET_UP_SNEAK;


    public boolean REST_TEAM_PLOTS_ONLY;

    public List<String> WORLDBLACKLIST = new ArrayList<String>();

    public List<Material> MATERIALBLACKLIST = new ArrayList<Material>();

    public List<String> BLOCKEDCOMMANDLIST = new ArrayList<String>();


    private final GSitMain GPM;

    public CManager(GSitMain GPluginMain) {
        GPM = GPluginMain;
        reload();
    }

    public void reload() {

        CHECK_FOR_UPDATES = false;//GPM.getConfig().getBoolean("Options.check-for-update", true);
        DEBUG = GPM.getConfig().getBoolean("Options.debug", false);
        CHECK_FEATURE_PERMISSIONS = GPM.getConfig().getBoolean("Options.check-feature-permissions", true);
        GET_UP_DAMAGE = GPM.getConfig().getBoolean("Options.get-up-damage", false);
        REST_SAME_BLOCK = GPM.getConfig().getBoolean("Options.rest-same-block", false);

        S_BLOCK_CENTER = GPM.getConfig().getBoolean("Options.Sit.block-center", true);
        S_SITMATERIALS.clear();
        for(String s : GPM.getConfig().getStringList("Options.Sit.SitMaterials")) {
            try {
                String[] m = s.split(";");
                if(m[0].startsWith("#")) {
                    for(Material a : Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(m[0].substring(1).toLowerCase()), Material.class).getValues()) {
                        S_SITMATERIALS.put(a, m.length > 1 ? Double.parseDouble(m[1]) : 0d);
                    }
                } else S_SITMATERIALS.put(Material.valueOf(m[0].toUpperCase()), m.length > 1 ? Double.parseDouble(m[1]) : 0d);
            } catch(IllegalArgumentException e) { }
        }
        S_ALLOW_UNSAFE = GPM.getConfig().getBoolean("Options.Sit.allow-unsafe", false);
        S_GET_UP_RETURN = GPM.getConfig().getBoolean("Options.Sit.get-up-return", false);
        S_EMPTY_HAND_ONLY = GPM.getConfig().getBoolean("Options.Sit.empty-hand-only", true);
        S_MAX_DISTANCE = GPM.getConfig().getDouble("Options.Sit.max-distance", 0d);
        S_SHOW_SIT_MESSAGE = GPM.getConfig().getBoolean("Options.Sit.show-sit-message", true);
        S_DEFAULT_SIT_MODE = GPM.getConfig().getBoolean("Options.Sit.default-sit-mode", true);

        PS_USE_PLAYERSIT = GPM.getConfig().getBoolean("Options.PlayerSit.use-playersit", false);
        PS_USE_PLAYERSIT_NPC = GPM.getConfig().getBoolean("Options.PlayerSit.use-playersit-npc", false);
        PS_MAX_STACK = GPM.getConfig().getLong("Options.PlayerSit.max-stack", 0);
        PS_SNEAK_EJECTS = GPM.getConfig().getBoolean("Options.PlayerSit.sneak-ejects", true);
        PS_EMPTY_HAND_ONLY = GPM.getConfig().getBoolean("Options.PlayerSit.empty-hand-only", true);
        PS_SHOW_SIT_MESSAGE = GPM.getConfig().getBoolean("Options.PlayerSit.show-sit-message", true);
        PS_DEFAULT_SIT_MODE = GPM.getConfig().getBoolean("Options.PlayerSit.default-sit-mode", true);

        L_BLOCK_CENTER = GPM.getConfig().getBoolean("Options.Lay.block-center", true);
        L_SHOW_LAY_MESSAGE = GPM.getConfig().getBoolean("Options.Lay.show-lay-message", true);
        L_INTERACT = GPM.getConfig().getBoolean("Options.Lay.interact", false);
        L_RESET_TIME_SINCE_REST = GPM.getConfig().getBoolean("Options.Lay.reset-time-since-rest", true);
        L_SNORING_SOUNDS = GPM.getConfig().getBoolean("Options.Lay.snoring-sounds", true);
        L_SNORING_NIGHT_ONLY = GPM.getConfig().getBoolean("Options.Lay.snoring-night-only", true);
        L_NIGHT_SKIP = GPM.getConfig().getBoolean("Options.Lay.night-skip", false);

        C_GET_UP_SNEAK = GPM.getConfig().getBoolean("Options.Crawl.get-up-sneak", true);

        REST_TEAM_PLOTS_ONLY = GPM.getConfig().getBoolean("Options.rest-team-plots-only", false);
        WORLDBLACKLIST = GPM.getConfig().getStringList("Options.WorldBlacklist");
        MATERIALBLACKLIST.clear();
        for(String s : GPM.getConfig().getStringList("Options.MaterialBlacklist")) {
            try {
                if(s.startsWith("#")) MATERIALBLACKLIST.addAll(Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(s.substring(1).toLowerCase()), Material.class).getValues());
                else MATERIALBLACKLIST.add(Material.valueOf(s.toUpperCase()));
            } catch(IllegalArgumentException e) { }
        }
        BLOCKEDCOMMANDLIST = GPM.getConfig().getStringList("Options.BlockedCommandlist");

    }

}