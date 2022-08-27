package dev.geco.gsit.manager;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

import org.bukkit.*;
import org.bukkit.configuration.file.*;

import dev.geco.gsit.GSitMain;

public class CManager {

    public String L_LANG;

    public boolean CHECK_FOR_UPDATES;

    public boolean GET_UP_DAMAGE;

    public boolean GET_UP_SNEAK;

    public boolean GET_UP_RETURN;

    public boolean GET_UP_BREAK;

    public boolean ALLOW_UNSAFE;

    public boolean SAME_BLOCK_REST;

    public boolean CENTER_BLOCK;


    public final HashMap<Material, Double> S_SITMATERIALS = new HashMap<>();

    public boolean S_BOTTOM_PART_ONLY;

    public boolean S_EMPTY_HAND_ONLY;

    public double S_MAX_DISTANCE;

    public boolean S_SIT_MESSAGE;

    public boolean S_DEFAULT_SIT_MODE;


    public boolean PS_ALLOW_SIT;

    public boolean PS_ALLOW_SIT_NPC;

    public long PS_MAX_STACK;

    public boolean PS_SNEAK_EJECTS;

    public boolean PS_EMPTY_HAND_ONLY;

    public double PS_MAX_DISTANCE;

    public boolean PS_SIT_MESSAGE;

    public boolean PS_DEFAULT_SIT_MODE;


    public boolean P_POSE_MESSAGE;

    public boolean P_INTERACT;

    public boolean P_LAY_REST;

    public boolean P_LAY_SNORING_SOUNDS;

    public boolean P_LAY_SNORING_NIGHT_ONLY;

    public boolean P_LAY_NIGHT_SKIP;


    public boolean C_GET_UP_SNEAK;

    public boolean C_DOUBLE_SNEAK;


    public boolean TRUSTED_REGION_ONLY;

    public List<String> WORLDBLACKLIST = new ArrayList<>();

    public final List<Material> MATERIALBLACKLIST = new ArrayList<>();

    public List<String> COMMANDBLACKLIST = new ArrayList<>();


    private final GSitMain GPM;

    public CManager(GSitMain GPluginMain) {

        GPM = GPluginMain;

        if(NMSManager.isNewerOrVersion(18, 2)) {
            try {
                File configFile = new File(GPM.getDataFolder(), "config.yml");
                FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                InputStream configSteam = GPM.getResource("config.yml");
                if(configSteam != null) {
                    FileConfiguration configSteamConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(configSteam, StandardCharsets.UTF_8));
                    config.setDefaults(configSteamConfig);
                    YamlConfigurationOptions options = (YamlConfigurationOptions) config.options();
                    options.parseComments(true).copyDefaults(true).width(500);
                    config.loadFromString(config.saveToString());
                    for(String comments : config.getKeys(true)) {
                        config.setComments(comments, configSteamConfig.getComments(comments));
                    }
                }
                config.save(configFile);
            } catch (Exception e) {
                e.printStackTrace();
                GPM.saveDefaultConfig();
            }
        } else GPM.saveDefaultConfig();

        reload();
    }

    public void reload() {

        GPM.reloadConfig();

        L_LANG = GPM.getConfig().getString("Lang.lang", "en_en").toLowerCase();

        CHECK_FOR_UPDATES = GPM.getConfig().getBoolean("Options.check-for-update", true);
        GET_UP_DAMAGE = GPM.getConfig().getBoolean("Options.get-up-damage", false);
        GET_UP_SNEAK = GPM.getConfig().getBoolean("Options.get-up-sneak", true);
        GET_UP_RETURN = GPM.getConfig().getBoolean("Options.get-up-return", false);
        GET_UP_BREAK = GPM.getConfig().getBoolean("Options.get-up-break", true);
        ALLOW_UNSAFE = GPM.getConfig().getBoolean("Options.allow-unsafe", false);
        SAME_BLOCK_REST = GPM.getConfig().getBoolean("Options.same-block-rest", false);
        CENTER_BLOCK = GPM.getConfig().getBoolean("Options.center-block", true);

        S_SITMATERIALS.clear();
        for(String s : GPM.getConfig().getStringList("Options.Sit.SitMaterials")) {

            try {

                String[] m = s.split(";");

                if(m[0].startsWith("#")) {

                    for(Material a : Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(m[0].substring(1).toLowerCase()), Material.class).getValues()) S_SITMATERIALS.put(a, m.length > 1 ? Double.parseDouble(m[1]) : 0d);
                } else S_SITMATERIALS.put(Material.valueOf(m[0].toUpperCase()), m.length > 1 ? Double.parseDouble(m[1]) : 0d);
            } catch (Exception | Error ignored) { }
        }
        S_BOTTOM_PART_ONLY = GPM.getConfig().getBoolean("Options.Sit.bottom-part-only", true);
        S_EMPTY_HAND_ONLY = GPM.getConfig().getBoolean("Options.Sit.empty-hand-only", true);
        S_MAX_DISTANCE = GPM.getConfig().getDouble("Options.Sit.max-distance", 0d);
        S_SIT_MESSAGE = GPM.getConfig().getBoolean("Options.Sit.sit-message", true);
        S_DEFAULT_SIT_MODE = GPM.getConfig().getBoolean("Options.Sit.default-sit-mode", true);

        PS_ALLOW_SIT = GPM.getConfig().getBoolean("Options.PlayerSit.allow-sit", false);
        PS_ALLOW_SIT_NPC = GPM.getConfig().getBoolean("Options.PlayerSit.allow-sit-npc", false);
        PS_MAX_STACK = GPM.getConfig().getLong("Options.PlayerSit.max-stack", 0);
        PS_SNEAK_EJECTS = GPM.getConfig().getBoolean("Options.PlayerSit.sneak-ejects", true);
        PS_EMPTY_HAND_ONLY = GPM.getConfig().getBoolean("Options.PlayerSit.empty-hand-only", true);
        PS_MAX_DISTANCE = GPM.getConfig().getDouble("Options.PlayerSit.max-distance", 0d);
        PS_SIT_MESSAGE = GPM.getConfig().getBoolean("Options.PlayerSit.sit-message", true);
        PS_DEFAULT_SIT_MODE = GPM.getConfig().getBoolean("Options.PlayerSit.default-sit-mode", true);

        P_POSE_MESSAGE = GPM.getConfig().getBoolean("Options.Pose.pose-message", true);
        P_INTERACT = GPM.getConfig().getBoolean("Options.Pose.interact", false);
        P_LAY_REST = GPM.getConfig().getBoolean("Options.Pose.lay-rest", true);
        P_LAY_SNORING_SOUNDS = GPM.getConfig().getBoolean("Options.Pose.lay-snoring-sounds", true);
        P_LAY_SNORING_NIGHT_ONLY = GPM.getConfig().getBoolean("Options.Pose.lay-snoring-night-only", true);
        P_LAY_NIGHT_SKIP = GPM.getConfig().getBoolean("Options.Pose.lay-night-skip", false);

        C_GET_UP_SNEAK = GPM.getConfig().getBoolean("Options.Crawl.get-up-sneak", true);
        C_DOUBLE_SNEAK = GPM.getConfig().getBoolean("Options.Crawl.double-sneak", false);

        TRUSTED_REGION_ONLY = GPM.getConfig().getBoolean("Options.trusted-region-only", false);
        WORLDBLACKLIST = GPM.getConfig().getStringList("Options.WorldBlacklist");
        MATERIALBLACKLIST.clear();
        for(String s : GPM.getConfig().getStringList("Options.MaterialBlacklist")) {

            try {

                if(s.startsWith("#")) MATERIALBLACKLIST.addAll(Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(s.substring(1).toLowerCase()), Material.class).getValues());
                else MATERIALBLACKLIST.add(Material.valueOf(s.toUpperCase()));
            } catch (Exception | Error ignored) { }
        }
        COMMANDBLACKLIST = GPM.getConfig().getStringList("Options.CommandBlacklist");
    }

}