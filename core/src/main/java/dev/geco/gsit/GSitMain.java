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

    private CManager cManager;
    public CManager getCManager() { return cManager; }

    private String prefix;
    public String getPrefix() { return prefix; }

    private ISitManager sitManager;
    public ISitManager getSitManager() { return sitManager; }

    private IPoseManager poseManager;
    public IPoseManager getPoseManager() { return poseManager; }

    private IPlayerSitManager playerSitManager;
    public IPlayerSitManager getPlayerSitManager() { return playerSitManager; }

    private ICrawlManager crawlManager;
    public ICrawlManager getCrawlManager() { return crawlManager; }

    private IEmoteManager emoteManager;
    public IEmoteManager getEmoteManager() { return emoteManager; }

    private ToggleManager toggleManager;
    public ToggleManager getToggleManager() { return toggleManager; }

    private UManager uManager;
    public UManager getUManager() { return uManager; }

    private PManager pManager;
    public PManager getPManager() { return pManager; }

    private MManager mManager;
    public MManager getMManager() { return mManager; }

    private EmoteUtil emoteUtil;
    public EmoteUtil getEmoteUtil() { return emoteUtil; }

    private PassengerUtil passengerUtil;
    public PassengerUtil getPassengerUtil() { return passengerUtil; }

    private SitUtil sitUtil;
    public SitUtil getSitUtil() { return sitUtil; }

    private PoseUtil poseUtil;
    public PoseUtil getPoseUtil() { return poseUtil; }

    private ISpawnUtil spawnUtil;
    public ISpawnUtil getSpawnUtil() { return spawnUtil; }

    private ITeleportUtil teleportUtil;
    public ITeleportUtil getPlayerUtil() { return teleportUtil; }

    private WorldGuardLink worldGuardLink;
    public WorldGuardLink getWorldGuardLink() { return worldGuardLink; }

    private PlaceholderAPILink placeholderAPILink;
    public PlaceholderAPILink getPlaceholderAPILink() { return placeholderAPILink; }

    private GriefPreventionLink griefPreventionLink;
    public GriefPreventionLink getGriefPreventionLink() { return griefPreventionLink; }

    private PlotSquaredLink plotSquaredLink;
    public PlotSquaredLink getPlotSquaredLink() { return plotSquaredLink; }

    public final int SERVER = Bukkit.getVersion().contains("Paper") ? 2 : Bukkit.getVersion().contains("Spigot") ? 1 : Bukkit.getVersion().contains("Bukkit") ? 0 : 3;

    public final String NAME = "GSit";

    public final String RESOURCE = "62325";

    private final List<String> LANG_LIST = new ArrayList<>(); {
        LANG_LIST.add("de_de");
        LANG_LIST.add("en_en");
        LANG_LIST.add("es_es");
        LANG_LIST.add("fi_fi");
        LANG_LIST.add("fr_fr");
        LANG_LIST.add("it_it");
        LANG_LIST.add("ja_jp");
        LANG_LIST.add("pl_pl");
        LANG_LIST.add("pt_br");
        LANG_LIST.add("ru_ru");
        LANG_LIST.add("uk_ua");
        LANG_LIST.add("zh_cn");
        LANG_LIST.add("zh_tw");
    }

    private final List<String> EMOTES = new ArrayList<>(); {

    }

    private static GSitMain GPM;

    public static GSitMain getInstance() { return GPM; }

    private void loadSettings() {

        copyLangFiles();

        copyEmoteFiles();

        messages = YamlConfiguration.loadConfiguration(new File("plugins/" + NAME + "/" + PluginValues.LANG_PATH, getConfig().getString("Lang.lang", "en_en") + PluginValues.YML_FILETYP));

        prefix = getMessages().getString("Plugin.plugin-prefix", "[" + NAME + "]");

        getEmoteManager().reloadEmotes();

        getToggleManager().loadToggleData();
    }

    private void linkBStats() {

        BStatsLink bstats = new BStatsLink(getInstance(), 4914);

        bstats.addCustomChart(new BStatsLink.SimplePie("plugin_language", () -> getConfig().getString("Lang.lang", "en_en").toLowerCase()));

        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_sit_feature", () -> {
            int count = getSitManager().getFeatureUsedCount();
            getSitManager().resetFeatureUsedCount();
            return count;
        }));

        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_pose_feature", () -> {
            if(getPoseManager() == null) return 0;
            int count = getPoseManager().getFeatureUsedCount();
            getPoseManager().resetFeatureUsedCount();
            return count;
        }));

        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_psit_feature", () -> {
            int count = getPlayerSitManager().getFeatureUsedCount();
            getPlayerSitManager().resetFeatureUsedCount();
            return count;
        }));

        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_crawl_feature", () -> {
            if(getCrawlManager() == null) return 0;
            int count = getCrawlManager().getFeatureUsedCount();
            getCrawlManager().resetFeatureUsedCount();
            return count;
        }));

        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_emote_feature", () -> {
            int count = getEmoteManager().getFeatureUsedCount();
            getEmoteManager().resetFeatureUsedCount();
            return count;
        }));
    }

    public void onLoad() {

        GPM = this;

        saveDefaultConfig();

        cManager = new CManager(getInstance());
        uManager = new UManager(getInstance(), RESOURCE);
        pManager = new PManager(getInstance());
        mManager = new MManager(getInstance());
        sitManager = new SitManager(getInstance());
        playerSitManager = new PlayerSitManager(getInstance());
        emoteManager = new EmoteManager(getInstance());
        toggleManager = new ToggleManager(getInstance());

        emoteUtil = new EmoteUtil();
        passengerUtil = new PassengerUtil(getInstance());
        sitUtil = new SitUtil(getInstance());
        poseUtil = new PoseUtil(getInstance());

        if(Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {

            worldGuardLink = new WorldGuardLink(getInstance());
            worldGuardLink.registerFlags();
        }
    }

    public void onEnable() {

        loadSettings();
        if(!versionCheck()) return;

        poseManager = NMSManager.isNewerOrVersion(17, 0) ? (IPoseManager) NMSManager.getPackageObject("gsit", "manager.PoseManager", getInstance()) : null;
        crawlManager = NMSManager.isNewerOrVersion(17, 0) ? (ICrawlManager) NMSManager.getPackageObject("gsit", "manager.CrawlManager", getInstance()) : null;
        spawnUtil = NMSManager.isNewerOrVersion(17, 0) ? (ISpawnUtil) NMSManager.getPackageObject("gsit", "util.SpawnUtil", null) : new SpawnUtil();
        teleportUtil = NMSManager.isNewerOrVersion(17, 0) ? (ITeleportUtil) NMSManager.getPackageObject("gsit", "util.TeleportUtil", null) : new TeleportUtil();

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
            if(worldGuardLink == null) {
                worldGuardLink = new WorldGuardLink(getInstance());
                getWorldGuardLink().registerFlags();
            }
            getMManager().sendMessage(s, "Plugin.plugin-link", "%Link%", "WorldGuard");
        } else worldGuardLink = null;

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderAPILink = new PlaceholderAPILink(getInstance());
            getMManager().sendMessage(s, "Plugin.plugin-link", "%Link%", "PlaceholderAPI");
            getPlaceholderAPILink().register();
        } else placeholderAPILink = null;

        if(Bukkit.getPluginManager().getPlugin("GriefPrevention") != null && Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")) {
            griefPreventionLink = new GriefPreventionLink(getInstance());
            getMManager().sendMessage(s, "Plugin.plugin-link", "%Link%", "GriefPrevention");
        } else griefPreventionLink = null;

        if(Bukkit.getPluginManager().getPlugin("PlotSquared") != null && Bukkit.getPluginManager().isPluginEnabled("PlotSquared")) {
            plotSquaredLink = new PlotSquaredLink(getInstance());
            if(getPlotSquaredLink().isVersionSupported()) getMManager().sendMessage(s, "Plugin.plugin-link", "%Link%", "PlotSquared");
            else plotSquaredLink = null;
        } else plotSquaredLink = null;
    }

    private void copyLangFiles() { for(String lang : LANG_LIST) if(!new File("plugins/" + NAME + "/" + PluginValues.LANG_PATH + "/" + lang + PluginValues.YML_FILETYP).exists()) saveResource(PluginValues.LANG_PATH + "/" + lang + PluginValues.YML_FILETYP, false); }

    public void reload(CommandSender Sender) {

        reloadConfig();

        getCManager().reload();
        getSitManager().clearSeats();
        if(getPoseManager() != null) getPoseManager().clearPoses();
        if(getCrawlManager() != null) getCrawlManager().clearCrawls();
        getEmoteManager().reloadEmotes();
        getToggleManager().saveToggleData();

        if(getPlaceholderAPILink() != null) getPlaceholderAPILink().unregister();

        loadSettings();
        loadPluginDepends(Sender);
        checkForUpdates();
    }

    private void checkForUpdates() {

        if(getCManager().CHECK_FOR_UPDATES) {

            getUManager().checkVersion();

            if(!getUManager().isLatestVersion()) {

                String message = getMManager().getMessage("Plugin.plugin-update", "%Name%", NAME, "%NewVersion%", getUManager().getLatestVersion(), "%Version%", getUManager().getPluginVersion(), "%Path%", getDescription().getWebsite());

                for(Player player : Bukkit.getOnlinePlayers()) if(getPManager().hasPermission(player, "Update")) player.sendMessage(message);

                Bukkit.getConsoleSender().sendMessage(message);
            }
        }
    }

    private boolean versionCheck() {

        if(SERVER < 1 || !NMSManager.isNewerOrVersion(13, 0) || (NMSManager.isNewerOrVersion(17, 0) && NMSManager.getPackageObject("gsit", "manager.PoseManager", getInstance()) == null)) {

            String version = Bukkit.getServer().getClass().getPackage().getName();

            getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-version", "%Version%", SERVER < 1 ? "Bukkit" : version.substring(version.lastIndexOf('.') + 1));

            checkForUpdates();

            Bukkit.getPluginManager().disablePlugin(getInstance());

            return false;
        }

        return true;
    }

    private void copyEmoteFiles() { for(String l : EMOTES) if(!new File("plugins/" + NAME + "/" + PluginValues.EMOTES_PATH + "/" + l + PluginValues.GEX_FILETYP).exists()) saveResource(PluginValues.EMOTES_PATH + "/" + l + PluginValues.GEX_FILETYP, false); }

}