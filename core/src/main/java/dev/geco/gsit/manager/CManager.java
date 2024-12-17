package dev.geco.gsit.manager;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

import org.bukkit.*;
import org.bukkit.configuration.file.*;

import dev.geco.gsit.GSitMain;

public class CManager {

    public String L_LANG;
    public boolean L_CLIENT_LANG;

    public boolean CHECK_FOR_UPDATE;
    public boolean DEBUG;
    public boolean GET_UP_DAMAGE;
    public boolean GET_UP_SNEAK;
    public boolean GET_UP_RETURN;
    public boolean GET_UP_BREAK;
    public boolean ALLOW_UNSAFE;
    public boolean SAME_BLOCK_REST;
    public boolean CENTER_BLOCK;
    public boolean CUSTOM_MESSAGE;

    public final HashMap<Material, Double> S_SITMATERIALS = new HashMap<>();
    public boolean S_BOTTOM_PART_ONLY;
    public boolean S_EMPTY_HAND_ONLY;
    public double S_MAX_DISTANCE;
    public boolean S_DEFAULT_SIT_MODE;

    public boolean PS_ALLOW_SIT;
    public boolean PS_ALLOW_SIT_NPC;
    public long PS_MAX_STACK;
    public boolean PS_SNEAK_EJECTS;
    public boolean PS_BOTTOM_RETURN;
    public boolean PS_EMPTY_HAND_ONLY;
    public double PS_MAX_DISTANCE;
    public boolean PS_DEFAULT_SIT_MODE;

    public boolean P_INTERACT;
    public boolean P_LAY_REST;
    public boolean P_LAY_SNORING_SOUNDS;
    public boolean P_LAY_SNORING_NIGHT_ONLY;
    public boolean P_LAY_NIGHT_SKIP;

    public boolean C_GET_UP_SNEAK;
    public boolean C_DOUBLE_SNEAK;
    public boolean C_DEFAULT_CRAWL_MODE;

    public boolean TRUSTED_REGION_ONLY;
    public List<String> WORLDBLACKLIST = new ArrayList<>();
    public List<String> WORLDWHITELIST = new ArrayList<>();
    public final List<Material> MATERIALBLACKLIST = new ArrayList<>();
    public List<String> COMMANDBLACKLIST = new ArrayList<>();
    public boolean ENHANCED_COMPATIBILITY;
    public List<String> FEATUREFLAGS = new ArrayList<>();

    private final GSitMain GPM;

    public CManager(GSitMain GPluginMain) {

        GPM = GPluginMain;

        if(!GPM.getSVManager().isNewerOrVersion(18, 2)) {
            try {
                File configFile = new File(GPM.getDataFolder(), "config.yml");
                FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                InputStream configSteam = GPM.getResource("config.yml");
                if(configSteam != null) {
                    FileConfiguration configSteamConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(configSteam, StandardCharsets.UTF_8));
                    if(!config.getKeys(true).equals(configSteamConfig.getKeys(true))) {
                        config.setDefaults(configSteamConfig);
                        YamlConfigurationOptions options = (YamlConfigurationOptions) config.options();
                        options.parseComments(true).copyDefaults(true).width(500);
                        config.loadFromString(config.saveToString());
                        for(String comments : config.getKeys(true)) config.setComments(comments, configSteamConfig.getComments(comments));
                        config.save(configFile);
                    }
                } else GPM.saveDefaultConfig();
            } catch (Throwable e) { GPM.saveDefaultConfig(); }
        } else GPM.saveDefaultConfig();

        reload();
    }

    public void reload() {

        GPM.reloadConfig();

        L_LANG = GPM.getConfig().getString("Lang.lang", "en_us").toLowerCase();
        L_CLIENT_LANG = GPM.getConfig().getBoolean("Lang.client-lang", true);

        CHECK_FOR_UPDATE = GPM.getConfig().getBoolean("Options.check-for-update", true);
        DEBUG = GPM.getConfig().getBoolean("Options.debug", false);
        GET_UP_DAMAGE = GPM.getConfig().getBoolean("Options.get-up-damage", false);
        GET_UP_SNEAK = GPM.getConfig().getBoolean("Options.get-up-sneak", true);
        GET_UP_RETURN = GPM.getConfig().getBoolean("Options.get-up-return", false);
        GET_UP_BREAK = GPM.getConfig().getBoolean("Options.get-up-break", true);
        ALLOW_UNSAFE = GPM.getConfig().getBoolean("Options.allow-unsafe", false);
        SAME_BLOCK_REST = GPM.getConfig().getBoolean("Options.same-block-rest", false);
        CENTER_BLOCK = GPM.getConfig().getBoolean("Options.center-block", true);
        CUSTOM_MESSAGE = GPM.getConfig().getBoolean("Options.custom-message", true);

        S_SITMATERIALS.clear();
        for(String material : GPM.getConfig().getStringList("Options.Sit.SitMaterials")) {
            try {
                String[] materialAndOffset = material.split(";");
                if(materialAndOffset[0].startsWith("#")) {
                    for(Material tagMaterial : Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(materialAndOffset[0].substring(1).toLowerCase()), Material.class).getValues()) S_SITMATERIALS.put(tagMaterial, materialAndOffset.length > 1 ? Double.parseDouble(materialAndOffset[1]) : 0d);
                    continue;
                }
                S_SITMATERIALS.put(materialAndOffset[0].equalsIgnoreCase("*") ? Material.AIR : Material.valueOf(materialAndOffset[0].toUpperCase()), materialAndOffset.length > 1 ? Double.parseDouble(materialAndOffset[1]) : 0d);
            } catch (Throwable ignored) { }
        }
        S_BOTTOM_PART_ONLY = GPM.getConfig().getBoolean("Options.Sit.bottom-part-only", true);
        S_EMPTY_HAND_ONLY = GPM.getConfig().getBoolean("Options.Sit.empty-hand-only", true);
        S_MAX_DISTANCE = GPM.getConfig().getDouble("Options.Sit.max-distance", 0d);
        S_DEFAULT_SIT_MODE = GPM.getConfig().getBoolean("Options.Sit.default-sit-mode", true);

        PS_ALLOW_SIT = GPM.getConfig().getBoolean("Options.PlayerSit.allow-sit", true);
        PS_ALLOW_SIT_NPC = GPM.getConfig().getBoolean("Options.PlayerSit.allow-sit-npc", true);
        PS_MAX_STACK = GPM.getConfig().getLong("Options.PlayerSit.max-stack", 0);
        PS_SNEAK_EJECTS = GPM.getConfig().getBoolean("Options.PlayerSit.sneak-ejects", true);
        PS_BOTTOM_RETURN = GPM.getConfig().getBoolean("Options.PlayerSit.bottom-return", false);
        PS_EMPTY_HAND_ONLY = GPM.getConfig().getBoolean("Options.PlayerSit.empty-hand-only", true);
        PS_MAX_DISTANCE = GPM.getConfig().getDouble("Options.PlayerSit.max-distance", 0d);
        PS_DEFAULT_SIT_MODE = GPM.getConfig().getBoolean("Options.PlayerSit.default-sit-mode", true);

        P_INTERACT = GPM.getConfig().getBoolean("Options.Pose.interact", false);
        P_LAY_REST = GPM.getConfig().getBoolean("Options.Pose.lay-rest", true);
        P_LAY_SNORING_SOUNDS = GPM.getConfig().getBoolean("Options.Pose.lay-snoring-sounds", false);
        P_LAY_SNORING_NIGHT_ONLY = GPM.getConfig().getBoolean("Options.Pose.lay-snoring-night-only", true);
        P_LAY_NIGHT_SKIP = GPM.getConfig().getBoolean("Options.Pose.lay-night-skip", true);

        C_GET_UP_SNEAK = GPM.getConfig().getBoolean("Options.Crawl.get-up-sneak", true);
        C_DOUBLE_SNEAK = GPM.getConfig().getBoolean("Options.Crawl.double-sneak", false);
        C_DEFAULT_CRAWL_MODE = GPM.getConfig().getBoolean("Options.Crawl.default-crawl-mode", true);

        TRUSTED_REGION_ONLY = GPM.getConfig().getBoolean("Options.trusted-region-only", false);
        WORLDBLACKLIST = GPM.getConfig().getStringList("Options.WorldBlacklist");
        WORLDWHITELIST = GPM.getConfig().getStringList("Options.WorldWhitelist");
        MATERIALBLACKLIST.clear();
        for(String material : GPM.getConfig().getStringList("Options.MaterialBlacklist")) {
            try {
                if(material.startsWith("#")) MATERIALBLACKLIST.addAll(Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(material.substring(1).toLowerCase()), Material.class).getValues());
                else MATERIALBLACKLIST.add(Material.valueOf(material.toUpperCase()));
            } catch (Throwable ignored) { }
        }
        COMMANDBLACKLIST = GPM.getConfig().getStringList("Options.CommandBlacklist");
        ENHANCED_COMPATIBILITY = GPM.getConfig().getBoolean("Options.enhanced-compatibility", false);
        FEATUREFLAGS = GPM.getConfig().getStringList("Options.FeatureFlags");
    }

}
