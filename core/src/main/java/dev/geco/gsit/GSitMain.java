package dev.geco.gsit;

import java.io.*;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.*;

import dev.geco.gsit.cmd.*;
import dev.geco.gsit.cmd.tab.*;
import dev.geco.gsit.events.*;
import dev.geco.gsit.link.*;
import dev.geco.gsit.manager.*;
import dev.geco.gsit.util.*;
import dev.geco.gsit.values.*;

public class GSitMain extends JavaPlugin {

    private FileConfiguration messages;

    public FileConfiguration getMessages() { return messages; }

    private CManager cmanager;

    public CManager getCManager() { return cmanager; }

    private String prefix;

    public String getPrefix() { return prefix; }

    private ISitManager sitmanager;

    public ISitManager getSitManager() { return sitmanager; }

    private IPoseManager posemanager;

    public IPoseManager getPoseManager() { return posemanager; }

    private IPlayerSitManager playersitmanager;

    public IPlayerSitManager getPlayerSitManager() { return playersitmanager; }

    private ICrawlManager crawlmanager;

    public ICrawlManager getCrawlManager() { return crawlmanager; }

    private IEmoteManager emotemanager;

    public IEmoteManager getEmoteManager() { return emotemanager; }

    private ToggleManager togglemanager;

    public ToggleManager getToggleManager() { return togglemanager; }

    private UManager umanager;

    public UManager getUManager() { return umanager; }

    private PManager pmanager;

    public PManager getPManager() { return pmanager; }

    private MManager mmanager;

    public MManager getMManager() { return mmanager; }

    private EmoteUtil emoteutil;

    public EmoteUtil getEmoteUtil() { return emoteutil; }

    private PassengerUtil passengerutil;

    public PassengerUtil getPassengerUtil() { return passengerutil; }

    private SitUtil situtil;

    public SitUtil getSitUtil() { return situtil; }

    private PoseUtil poseutil;

    public PoseUtil getPoseUtil() { return poseutil; }

    private ISpawnUtil spawnutil;

    public ISpawnUtil getSpawnUtil() { return spawnutil; }

    private IPlayerUtil playerutil;

    public IPlayerUtil getPlayerUtil() { return playerutil; }

    private WoGuLink wogulink;

    public WoGuLink getWorldGuardLink() { return wogulink; }

    private PAPILink papilink;

    public PAPILink getPlaceholderAPILink() { return papilink; }

    private GrPrLink grprlink;

    public GrPrLink getGriefPreventionLink() { return grprlink; }

    public final int SERVER = Bukkit.getVersion().contains("Paper") ? 2 : Bukkit.getVersion().contains("Spigot") ? 1 : 0;

    public final String NAME = "GSit";

    public final String RESOURCE = "62325";

    private static GSitMain GPM;

    public static GSitMain getInstance() { return GPM; }

    private void loadSettings() {
        copyLangFiles();
        copyEmoteFiles();
        messages = YamlConfiguration.loadConfiguration(new File("plugins/" + NAME + "/" + PluginValues.LANG_PATH, getConfig().getString("Lang.lang", "en_en") + PluginValues.YML_FILETYP));
        prefix = getMessages().getString("Plugin.plugin-prefix");
        getEmoteManager().reloadEmotes();
        getToggleManager().loadToggleData();
    }

    private void linkBStats() {
        BStatsLink bstats = new BStatsLink(getInstance(), 4914);
        bstats.addCustomChart(new BStatsLink.SimplePie("plugin_language", () -> getConfig().getString("Lang.lang", "en_en").toLowerCase()));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_sit_feature", () -> {
            int c = getSitManager().getFeatureUsedCount();
            getSitManager().resetFeatureUsedCount();
            return c;
        }));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_pose_feature", () -> {
            if(getPoseManager() == null) return 0;
            int c = getPoseManager().getFeatureUsedCount();
            getPoseManager().resetFeatureUsedCount();
            return c;
        }));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_psit_feature", () -> {
            int c = getPlayerSitManager().getFeatureUsedCount();
            getPlayerSitManager().resetFeatureUsedCount();
            return c;
        }));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_crawl_feature", () -> {
            if(getCrawlManager() == null) return 0;
            int c = getCrawlManager().getFeatureUsedCount();
            getCrawlManager().resetFeatureUsedCount();
            return c;
        }));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_emote_feature", () -> {
            int c = getEmoteManager().getFeatureUsedCount();
            getEmoteManager().resetFeatureUsedCount();
            return c;
        }));
    }

    public void onLoad() {
        GPM = this;
        saveDefaultConfig();
        cmanager = new CManager(getInstance());
        umanager = new UManager(getInstance(), RESOURCE);
        pmanager = new PManager(getInstance());
        mmanager = new MManager(getInstance());
        sitmanager = new SitManager(getInstance());
        playersitmanager = new PlayerSitManager(getInstance());
        emotemanager = new EmoteManager(getInstance());
        togglemanager = new ToggleManager(getInstance());
        emoteutil = new EmoteUtil();
        passengerutil = new PassengerUtil(getInstance());
        situtil = new SitUtil(getInstance());
        poseutil = new PoseUtil(getInstance());
        if(Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            wogulink = new WoGuLink(getInstance());
            wogulink.registerFlags();
        }
    }

    public void onEnable() {
        loadSettings();
        if(!versionCheck()) return;
        posemanager = NMSManager.isNewerOrVersion(17, 0) ? (IPoseManager) NMSManager.getPackageObject("gsit", "manager.PoseManager", getInstance()) : null;
        crawlmanager = NMSManager.isNewerOrVersion(17, 0) ? (ICrawlManager) NMSManager.getPackageObject("gsit", "manager.CrawlManager", getInstance()) : null;
        spawnutil = NMSManager.isNewerOrVersion(17, 0) ? (ISpawnUtil) NMSManager.getPackageObject("gsit", "util.SpawnUtil", null) : new SpawnUtil();
        playerutil = NMSManager.isNewerOrVersion(17, 0) ? (IPlayerUtil) NMSManager.getPackageObject("gsit", "util.PlayerUtil", null) : new PlayerUtil();
        setupCommands();
        setupEvents();
        linkBStats();
        getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-enabled");
        loadPluginDepends(Bukkit.getConsoleSender());
        checkForUpdates();
    }

    public void onDisable() {
        getSitManager().clearSeats();
        if(getPoseManager() != null) getPoseManager().clearPoses();
        if(getCrawlManager() != null) getCrawlManager().clearCrawls();
        getEmoteManager().clearEmotes();
        getToggleManager().saveToggleData();
        if(getPlaceholderAPILink() != null) getPlaceholderAPILink().unregister();
        getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-disabled");
    }

    private void setupCommands() {
        getCommand("gsit").setExecutor(new GSitCommand(getInstance()));
        getCommand("gsit").setTabCompleter(new GSitTabComplete(getInstance()));
        getCommand("glay").setExecutor(new GLayCommand(getInstance()));
        getCommand("glay").setTabCompleter(new EmptyTabComplete());
        getCommand("gbellyflop").setExecutor(new GBellyFlopCommand(getInstance()));
        getCommand("gbellyflop").setTabCompleter(new EmptyTabComplete());
        getCommand("gspin").setExecutor(new GSpinCommand(getInstance()));
        getCommand("gspin").setTabCompleter(new EmptyTabComplete());
        getCommand("gcrawl").setExecutor(new GCrawlCommand(getInstance()));
        getCommand("gcrawl").setTabCompleter(new EmptyTabComplete());
        getCommand("gemote").setExecutor(new GEmoteCommand(getInstance()));
        getCommand("gemote").setTabCompleter(new GEmoteTabComplete(getInstance()));
        getCommand("gsitreload").setExecutor(new GSitReloadCommand(getInstance()));
        getCommand("gsitreload").setTabCompleter(new EmptyTabComplete());
    }

    private void setupEvents() {
        getServer().getPluginManager().registerEvents(new PlayerEvents(getInstance()), getInstance());
        getServer().getPluginManager().registerEvents(new PlayerSitEvents(getInstance()), getInstance());
        getServer().getPluginManager().registerEvents(new BlockEvents(getInstance()), getInstance());
        getServer().getPluginManager().registerEvents(new InteractEvents(getInstance()), getInstance());
    }

    private void loadPluginDepends(CommandSender s) {
        if(Bukkit.getPluginManager().getPlugin("WorldGuard") != null && Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            if(wogulink == null) {
                wogulink = new WoGuLink(getInstance());
                getWorldGuardLink().registerFlags();
            }
            getMManager().sendMessage(s, "Plugin.plugin-link", "%Link%", "WorldGuard");
        } else wogulink = null;
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            papilink = new PAPILink(getInstance());
            getMManager().sendMessage(s, "Plugin.plugin-link", "%Link%", "PlaceholderAPI");
            getPlaceholderAPILink().register();
        } else papilink = null;
        if(Bukkit.getPluginManager().getPlugin("GriefPrevention") != null && Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")) {
            grprlink = new GrPrLink(getInstance());
            getMManager().sendMessage(s, "Plugin.plugin-link", "%Link%", "GriefPrevention");
        } else grprlink = null;
    }

    private void copyLangFiles() { for(String l : Arrays.asList("de_de", "en_en", "es_es", "fi_fi", "fr_fr", "it_it", "pl_pl", "pt_br", "ru_ru", "uk_ua", "zh_cn", "zh_tw")) if(!new File("plugins/" + NAME + "/" + PluginValues.LANG_PATH + "/" + l + PluginValues.YML_FILETYP).exists()) saveResource(PluginValues.LANG_PATH + "/" + l + PluginValues.YML_FILETYP, false); }

    public void reload(CommandSender s) {
        reloadConfig();
        getCManager().reload();
        getSitManager().clearSeats();
        if(getPoseManager() != null) getPoseManager().clearPoses();
        if(getCrawlManager() != null) getCrawlManager().clearCrawls();
        getEmoteManager().clearEmotes();
        getEmoteManager().reloadEmotes();
        getToggleManager().saveToggleData();
        if(getPlaceholderAPILink() != null) getPlaceholderAPILink().unregister();
        loadSettings();
        loadPluginDepends(s);
        checkForUpdates();
    }

    private void checkForUpdates() {
        if(getCManager().CHECK_FOR_UPDATES) {
            getUManager().checkVersion();
            if(!getUManager().isLatestVersion()) {
                String me = getMManager().getMessage("Plugin.plugin-update", "%Name%", NAME, "%NewVersion%", getUManager().getLatestVersion(), "%Version%", getUManager().getPluginVersion(), "%Path%", getDescription().getWebsite());
                for(Player p : Bukkit.getOnlinePlayers()) if(getPManager().hasPermission(p, "Update")) p.sendMessage(me);
                Bukkit.getConsoleSender().sendMessage(me);
            }
        }
    }

    private boolean versionCheck() {
        if(SERVER < 1 || !NMSManager.isNewerOrVersion(13, 0) || (NMSManager.isNewerOrVersion(17, 0) && NMSManager.getPackageObject("gsit", "manager.PoseManager", getInstance()) == null)) {
            String v = Bukkit.getServer().getClass().getPackage().getName();
            getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-version", "%Version%", SERVER < 1 ? "Bukkit" : v.substring(v.lastIndexOf('.') + 1));
            checkForUpdates();
            Bukkit.getPluginManager().disablePlugin(getInstance());
            return false;
        }
        return true;
    }

    private void copyEmoteFiles() { for(String l : Arrays.asList("smile")) if(!new File("plugins/" + NAME + "/" + PluginValues.EMOTES_PATH + "/" + l + PluginValues.GEX_FILETYP).exists()) saveResource(PluginValues.EMOTES_PATH + "/" + l + PluginValues.GEX_FILETYP, false); }

}