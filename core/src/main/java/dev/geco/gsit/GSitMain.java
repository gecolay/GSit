package dev.geco.gsit;

import java.io.*;
import java.util.*;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.plugin.java.*;

import dev.geco.gsit.api.event.*;
import dev.geco.gsit.cmd.*;
import dev.geco.gsit.cmd.tab.*;
import dev.geco.gsit.events.*;
import dev.geco.gsit.events.features.*;
import dev.geco.gsit.link.*;
import dev.geco.gsit.manager.*;
import dev.geco.gsit.util.*;

public class GSitMain extends JavaPlugin {

    private CManager cManager;
    public CManager getCManager() { return cManager; }

    private DManager dManager;
    public DManager getDManager() { return dManager; }

    private SitManager sitManager;
    public SitManager getSitManager() { return sitManager; }

    private PoseManager poseManager;
    public PoseManager getPoseManager() { return poseManager; }

    private PlayerSitManager playerSitManager;
    public PlayerSitManager getPlayerSitManager() { return playerSitManager; }

    private CrawlManager crawlManager;
    public CrawlManager getCrawlManager() { return crawlManager; }

    private EmoteManager emoteManager;
    public EmoteManager getEmoteManager() { return emoteManager; }

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

    private EnvironmentUtil environmentUtil;
    public EnvironmentUtil getEnvironmentUtil() { return environmentUtil; }

    private IEntityUtil entityUtil;
    public IEntityUtil getEntityUtil() { return entityUtil; }

    private GriefPreventionLink griefPreventionLink;
    public GriefPreventionLink getGriefPreventionLink() { return griefPreventionLink; }

    private PlaceholderAPILink placeholderAPILink;
    public PlaceholderAPILink getPlaceholderAPILink() { return placeholderAPILink; }

    private PlotSquaredLink plotSquaredLink;
    public PlotSquaredLink getPlotSquaredLink() { return plotSquaredLink; }

    private boolean viaBackwardsLink;
    public boolean getViaBackwardsLink() { return viaBackwardsLink; }

    private WorldGuardLink worldGuardLink;
    public WorldGuardLink getWorldGuardLink() { return worldGuardLink; }

    public final String NAME = "GSit";

    public final String RESOURCE = "62325";

    public final int PLAYER_SIT_SEAT_ENTITIES = 2;

    private final List<String> EMOTE_FILES = new ArrayList<>(); {
        EMOTE_FILES.add("happy");
    }

    private static GSitMain GPM;

    public static GSitMain getInstance() { return GPM; }

    private void loadSettings() {

        dManager.connect();

        copyEmoteFiles();

        getEmoteManager().reloadEmotes();

        getToggleManager().createTable();
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

        dManager = new DManager(getInstance());
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
        environmentUtil = new EnvironmentUtil(getInstance());

        preloadPluginDependencies();
    }

    public void onEnable() {

        loadSettings();
        if(!versionCheck()) return;

        entityUtil = NMSManager.isNewerOrVersion(17, 0) ? (IEntityUtil) NMSManager.getPackageObject("util.EntityUtil", null) : new EntityUtil();

        setupCommands();
        setupEvents();
        linkBStats();

        getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-enabled");

        loadPluginDependencies(Bukkit.getConsoleSender());
        GPM.getUManager().checkForUpdates();
    }

    public void onDisable() {

        dManager.close();
        getSitManager().clearSeats();
        getPlayerSitManager().clearSeats();
        getPoseManager().clearPoses();
        getCrawlManager().clearCrawls();
        getEmoteManager().clearEmotes();

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

        getServer().getPluginManager().registerEvents(new SpinConfusionEvent(getInstance()), getInstance());
    }

    private void preloadPluginDependencies() {

        if(Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {

            worldGuardLink = new WorldGuardLink(getInstance());
            worldGuardLink.registerFlags();
        }
    }

    private void loadPluginDependencies(CommandSender Sender) {

        if(Bukkit.getPluginManager().getPlugin("GriefPrevention") != null && Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")) {
            griefPreventionLink = new GriefPreventionLink(getInstance());
            getMManager().sendMessage(Sender, "Plugin.plugin-link", "%Link%", "GriefPrevention");
        } else griefPreventionLink = null;

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderAPILink = new PlaceholderAPILink(getInstance());
            getMManager().sendMessage(Sender, "Plugin.plugin-link", "%Link%", "PlaceholderAPI");
            getPlaceholderAPILink().register();
        } else placeholderAPILink = null;

        if(Bukkit.getPluginManager().getPlugin("PlotSquared") != null && Bukkit.getPluginManager().isPluginEnabled("PlotSquared")) {
            plotSquaredLink = new PlotSquaredLink(getInstance());
            if(getPlotSquaredLink().isVersionSupported()) getMManager().sendMessage(Sender, "Plugin.plugin-link", "%Link%", "PlotSquared");
            else plotSquaredLink = null;
        } else plotSquaredLink = null;

        if(Bukkit.getPluginManager().getPlugin("ViaBackwards") != null && Bukkit.getPluginManager().isPluginEnabled("ViaBackwards")) {
            viaBackwardsLink = true;
            getMManager().sendMessage(Sender, "Plugin.plugin-link", "%Link%", "ViaBackwards");
        } else viaBackwardsLink = false;

        if(Bukkit.getPluginManager().getPlugin("WorldGuard") != null && Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            if(worldGuardLink == null) {
                worldGuardLink = new WorldGuardLink(getInstance());
                getWorldGuardLink().registerFlags();
            }
            getMManager().sendMessage(Sender, "Plugin.plugin-link", "%Link%", "WorldGuard");
        } else worldGuardLink = null;
    }

    private void copyEmoteFiles() { for(String emote : EMOTE_FILES) if(!new File(getDataFolder(), "emotes/" + emote + ".gex").exists()) saveResource("emotes/" + emote + ".gex", false); }

    public void reload(CommandSender Sender) {

        Bukkit.getPluginManager().callEvent(new GSitReloadEvent(getInstance()));

        getCManager().reload();
        getMManager().loadMessages();

        dManager.close();
        getSitManager().clearSeats();
        getPlayerSitManager().clearSeats();
        getPoseManager().clearPoses();
        getCrawlManager().clearCrawls();
        getEmoteManager().reloadEmotes();

        if(getPlaceholderAPILink() != null) getPlaceholderAPILink().unregister();

        loadSettings();
        loadPluginDependencies(Sender);
        GPM.getUManager().checkForUpdates();
    }

    private boolean versionCheck() {

        boolean bukkitBased = false;

        try { Class.forName("org.spigotmc.event.entity.EntityDismountEvent"); } catch (ClassNotFoundException e) { bukkitBased = true; }

        if(bukkitBased || !NMSManager.isNewerOrVersion(13, 0) || (NMSManager.isNewerOrVersion(17, 0) && !NMSManager.hasPackageClass("objects.SeatEntity"))) {

            String version = Bukkit.getServer().getClass().getPackage().getName();

            getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-version", "%Version%", bukkitBased ? "bukkit-based" : version.substring(version.lastIndexOf('.') + 1));

            GPM.getUManager().checkForUpdates();

            Bukkit.getPluginManager().disablePlugin(getInstance());

            return false;
        }

        return true;
    }

}