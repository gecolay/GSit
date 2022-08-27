package dev.geco.gsit;

import java.io.*;
import java.util.*;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.plugin.java.*;

import dev.geco.gsit.cmd.*;
import dev.geco.gsit.cmd.tab.*;
import dev.geco.gsit.events.*;
import dev.geco.gsit.link.*;
import dev.geco.gsit.manager.*;
import dev.geco.gsit.util.*;

public class GSitMain extends JavaPlugin {

    private CManager cManager;
    public CManager getCManager() { return cManager; }

    private SitManager sitManager;
    public SitManager getSitManager() { return sitManager; }

    private PoseManager poseManager;
    public PoseManager getPoseManager() { return poseManager; }

    private IPlayerSitManager playerSitManager;
    public IPlayerSitManager getPlayerSitManager() { return playerSitManager; }

    private CrawlManager crawlManager;
    public CrawlManager getCrawlManager() { return crawlManager; }

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

    private ISpawnUtil spawnUtil;
    public ISpawnUtil getSpawnUtil() { return spawnUtil; }

    private ITeleportUtil teleportUtil;
    public ITeleportUtil getTeleportUtil() { return teleportUtil; }

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

    private final List<String> EMOTE_FILES = new ArrayList<>(); { }

    private static GSitMain GPM;

    public static GSitMain getInstance() { return GPM; }

    private void loadSettings() {

        copyEmoteFiles();

        getEmoteManager().reloadEmotes();

        getToggleManager().loadToggleData();
    }

    private void linkBStats() {

        BStatsLink bstats = new BStatsLink(getInstance(), 4914);

        bstats.addCustomChart(new BStatsLink.SimplePie("plugin_language", () -> getCManager().L_LANG));

        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_sit_feature", () -> {
            int count = getSitManager().getFeatureUsedCount();
            getSitManager().resetFeatureUsedCount();
            return count;
        }));

        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_pose_feature", () -> {
            if(!getPoseManager().isAvailable()) return 0;
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
            if(!getCrawlManager().isAvailable()) return 0;
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

        cManager = new CManager(getInstance());
        uManager = new UManager(getInstance());
        pManager = new PManager(getInstance());
        mManager = new MManager(getInstance());
        sitManager = new SitManager(getInstance());
        poseManager = new PoseManager(getInstance());
        crawlManager = new CrawlManager(getInstance());
        playerSitManager = new PlayerSitManager(getInstance());
        emoteManager = new EmoteManager(getInstance());
        toggleManager = new ToggleManager(getInstance());

        emoteUtil = new EmoteUtil();
        passengerUtil = new PassengerUtil(getInstance());
        sitUtil = new SitUtil(getInstance());

        if(Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {

            worldGuardLink = new WorldGuardLink(getInstance());
            worldGuardLink.registerFlags();
        }
    }

    public void onEnable() {

        loadSettings();
        if(!versionCheck()) return;

        spawnUtil = NMSManager.isNewerOrVersion(17, 0) ? (ISpawnUtil) NMSManager.getPackageObject("util.SpawnUtil", null) : new SpawnUtil();
        teleportUtil = NMSManager.isNewerOrVersion(17, 0) ? (ITeleportUtil) NMSManager.getPackageObject("util.TeleportUtil", null) : new TeleportUtil();

        setupCommands();
        setupEvents();
        linkBStats();

        getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-enabled");

        loadPluginDepends(Bukkit.getConsoleSender());
        GPM.getUManager().checkForUpdates();
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
        getCommand("gcrawl").setTabCompleter(new GCrawlTabComplete(getInstance()));
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

    private void copyEmoteFiles() { for(String emote : EMOTE_FILES) if(!new File(getDataFolder(), "emotes/" + emote + ".gex").exists()) saveResource("emotes/" + emote + ".gex", false); }

    public void reload(CommandSender Sender) {

        getCManager().reload();
        getMManager().loadMessages();

        getSitManager().clearSeats();
        if(getPoseManager() != null) getPoseManager().clearPoses();
        if(getCrawlManager() != null) getCrawlManager().clearCrawls();
        getEmoteManager().reloadEmotes();
        getToggleManager().saveToggleData();

        if(getPlaceholderAPILink() != null) getPlaceholderAPILink().unregister();

        loadSettings();
        loadPluginDepends(Sender);
        GPM.getUManager().checkForUpdates();
    }

    private boolean versionCheck() {

        if(SERVER < 1 || !NMSManager.isNewerOrVersion(13, 0) || (NMSManager.isNewerOrVersion(17, 0) && !NMSManager.hasPackageClass("objects.SeatEntity"))) {

            String version = Bukkit.getServer().getClass().getPackage().getName();

            getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-version", "%Version%", SERVER < 1 ? "Bukkit" : version.substring(version.lastIndexOf('.') + 1));

            GPM.getUManager().checkForUpdates();

            Bukkit.getPluginManager().disablePlugin(getInstance());

            return false;
        }

        return true;
    }

}