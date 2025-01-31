package dev.geco.gsit.service;

import dev.geco.gsit.GSitMain;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigService {

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

    private final GSitMain gSitMain;

    public ConfigService(GSitMain gSitMain) {
        this.gSitMain = gSitMain;

        if(!gSitMain.getVersionManager().isNewerOrVersion(18, 2)) {
            try {
                File configFile = new File(gSitMain.getDataFolder(), "config.yml");
                FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                InputStream configSteam = gSitMain.getResource("config.yml");
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
                } else gSitMain.saveDefaultConfig();
            } catch(Throwable e) { gSitMain.saveDefaultConfig(); }
        } else gSitMain.saveDefaultConfig();

        reload();
    }

    public void reload() {
        gSitMain.reloadConfig();

        L_LANG = gSitMain.getConfig().getString("Lang.lang", "en_us").toLowerCase();
        L_CLIENT_LANG = gSitMain.getConfig().getBoolean("Lang.client-lang", true);

        CHECK_FOR_UPDATE = gSitMain.getConfig().getBoolean("Options.check-for-update", true);
        DEBUG = gSitMain.getConfig().getBoolean("Options.debug", false);
        GET_UP_DAMAGE = gSitMain.getConfig().getBoolean("Options.get-up-damage", false);
        GET_UP_SNEAK = gSitMain.getConfig().getBoolean("Options.get-up-sneak", true);
        GET_UP_RETURN = gSitMain.getConfig().getBoolean("Options.get-up-return", false);
        GET_UP_BREAK = gSitMain.getConfig().getBoolean("Options.get-up-break", true);
        ALLOW_UNSAFE = gSitMain.getConfig().getBoolean("Options.allow-unsafe", false);
        SAME_BLOCK_REST = gSitMain.getConfig().getBoolean("Options.same-block-rest", false);
        CENTER_BLOCK = gSitMain.getConfig().getBoolean("Options.center-block", true);
        CUSTOM_MESSAGE = gSitMain.getConfig().getBoolean("Options.custom-message", true);

        S_SITMATERIALS.clear();
        for(String material : gSitMain.getConfig().getStringList("Options.Sit.SitMaterials")) {
            try {
                String[] materialAndOffset = material.split(";");
                if(materialAndOffset[0].startsWith("#")) {
                    for(Material tagMaterial : Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(materialAndOffset[0].substring(1).toLowerCase()), Material.class).getValues()) S_SITMATERIALS.put(tagMaterial, materialAndOffset.length > 1 ? Double.parseDouble(materialAndOffset[1]) : 0d);
                    continue;
                }
                S_SITMATERIALS.put(materialAndOffset[0].equalsIgnoreCase("*") ? Material.AIR : Material.valueOf(materialAndOffset[0].toUpperCase()), materialAndOffset.length > 1 ? Double.parseDouble(materialAndOffset[1]) : 0d);
            } catch(Throwable ignored) { }
        }
        S_BOTTOM_PART_ONLY = gSitMain.getConfig().getBoolean("Options.Sit.bottom-part-only", true);
        S_EMPTY_HAND_ONLY = gSitMain.getConfig().getBoolean("Options.Sit.empty-hand-only", true);
        S_MAX_DISTANCE = gSitMain.getConfig().getDouble("Options.Sit.max-distance", 0d);
        S_DEFAULT_SIT_MODE = gSitMain.getConfig().getBoolean("Options.Sit.default-sit-mode", true);

        PS_ALLOW_SIT = gSitMain.getConfig().getBoolean("Options.PlayerSit.allow-sit", true);
        PS_ALLOW_SIT_NPC = gSitMain.getConfig().getBoolean("Options.PlayerSit.allow-sit-npc", true);
        PS_MAX_STACK = gSitMain.getConfig().getLong("Options.PlayerSit.max-stack", 0);
        PS_SNEAK_EJECTS = gSitMain.getConfig().getBoolean("Options.PlayerSit.sneak-ejects", true);
        PS_BOTTOM_RETURN = gSitMain.getConfig().getBoolean("Options.PlayerSit.bottom-return", false);
        PS_EMPTY_HAND_ONLY = gSitMain.getConfig().getBoolean("Options.PlayerSit.empty-hand-only", true);
        PS_MAX_DISTANCE = gSitMain.getConfig().getDouble("Options.PlayerSit.max-distance", 0d);
        PS_DEFAULT_SIT_MODE = gSitMain.getConfig().getBoolean("Options.PlayerSit.default-sit-mode", true);

        P_INTERACT = gSitMain.getConfig().getBoolean("Options.Pose.interact", false);
        P_LAY_REST = gSitMain.getConfig().getBoolean("Options.Pose.lay-rest", true);
        P_LAY_SNORING_SOUNDS = gSitMain.getConfig().getBoolean("Options.Pose.lay-snoring-sounds", false);
        P_LAY_SNORING_NIGHT_ONLY = gSitMain.getConfig().getBoolean("Options.Pose.lay-snoring-night-only", true);
        P_LAY_NIGHT_SKIP = gSitMain.getConfig().getBoolean("Options.Pose.lay-night-skip", true);

        C_GET_UP_SNEAK = gSitMain.getConfig().getBoolean("Options.Crawl.get-up-sneak", true);
        C_DOUBLE_SNEAK = gSitMain.getConfig().getBoolean("Options.Crawl.double-sneak", false);
        C_DEFAULT_CRAWL_MODE = gSitMain.getConfig().getBoolean("Options.Crawl.default-crawl-mode", true);

        TRUSTED_REGION_ONLY = gSitMain.getConfig().getBoolean("Options.trusted-region-only", false);
        WORLDBLACKLIST = gSitMain.getConfig().getStringList("Options.WorldBlacklist");
        WORLDWHITELIST = gSitMain.getConfig().getStringList("Options.WorldWhitelist");
        MATERIALBLACKLIST.clear();
        for(String material : gSitMain.getConfig().getStringList("Options.MaterialBlacklist")) {
            try {
                if(material.startsWith("#")) MATERIALBLACKLIST.addAll(Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(material.substring(1).toLowerCase()), Material.class).getValues());
                else MATERIALBLACKLIST.add(Material.valueOf(material.toUpperCase()));
            } catch(Throwable ignored) { }
        }
        COMMANDBLACKLIST = gSitMain.getConfig().getStringList("Options.CommandBlacklist");
        ENHANCED_COMPATIBILITY = gSitMain.getConfig().getBoolean("Options.enhanced-compatibility", false);
        FEATUREFLAGS = gSitMain.getConfig().getStringList("Options.FeatureFlags");
    }

}
