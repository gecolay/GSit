package dev.geco.gsit;

import java.util.*;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.event.*;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.*;

import dev.geco.gsit.api.event.*;
import dev.geco.gsit.cmd.*;
import dev.geco.gsit.cmd.tab.*;
import dev.geco.gsit.events.*;
import dev.geco.gsit.events.features.*;
import dev.geco.gsit.link.*;
import dev.geco.gsit.manager.*;
import dev.geco.gsit.manager.mm.*;
import dev.geco.gsit.util.*;

public class GSitMain extends JavaPlugin {

    public static final String NAME = "GSit";
    public static final String RESOURCE = "62325";

    private static GSitMain GPM;
    private CManager cManager;
    private MManager mManager;
    private UManager uManager;
    private PManager pManager;
    private TManager tManager;
    private DManager dManager;
    private SVManager svManager;
    private SitManager sitManager;
    private PlayerSitManager playerSitManager;
    private PoseManager poseManager;
    private CrawlManager crawlManager;
    private ToggleManager toggleManager;
    private EntityEventsHandler entityEventsHandler;
    private PassengerUtil passengerUtil;
    private EnvironmentUtil environmentUtil;
    private IEntityUtil entityUtil;
    private GriefPreventionLink griefPreventionLink;
    private PlaceholderAPILink placeholderAPILink;
    private PlotSquaredLink plotSquaredLink;
    private WorldGuardLink worldGuardLink;
    private boolean supportsPaperFeature = false;
    private boolean supportsTaskFeature = false;

    public static GSitMain getInstance() { return GPM; }

    public CManager getCManager() { return cManager; }

    public MManager getMManager() { return mManager; }

    public UManager getUManager() { return uManager; }

    public PManager getPManager() { return pManager; }

    public TManager getTManager() { return tManager; }

    public DManager getDManager() { return dManager; }

    public SVManager getSVManager() { return svManager; }

    public SitManager getSitManager() { return sitManager; }

    public PlayerSitManager getPlayerSitManager() { return playerSitManager; }

    public PoseManager getPoseManager() { return poseManager; }

    public CrawlManager getCrawlManager() { return crawlManager; }

    public ToggleManager getToggleManager() { return toggleManager; }

    public EntityEventsHandler getEntityEventsHandler() { return entityEventsHandler; }

    public PassengerUtil getPassengerUtil() { return passengerUtil; }

    public EnvironmentUtil getEnvironmentUtil() { return environmentUtil; }

    public IEntityUtil getEntityUtil() { return entityUtil; }

    public GriefPreventionLink getGriefPreventionLink() { return griefPreventionLink; }

    public PlaceholderAPILink getPlaceholderAPILink() { return placeholderAPILink; }

    public PlotSquaredLink getPlotSquaredLink() { return plotSquaredLink; }

    public WorldGuardLink getWorldGuardLink() { return worldGuardLink; }

    public boolean supportsPaperFeature() { return supportsPaperFeature; }

    public boolean supportsTaskFeature() { return supportsTaskFeature; }

    public void onLoad() {

        GPM = this;

        svManager = new SVManager(this);
        cManager = new CManager(this);
        dManager = new DManager(this);
        uManager = new UManager(this);
        pManager = new PManager(this);
        tManager = new TManager(this);
        sitManager = new SitManager(this);
        playerSitManager = new PlayerSitManager(this);
        poseManager = new PoseManager(this);
        crawlManager = new CrawlManager(this);
        toggleManager = new ToggleManager(this);

        entityEventsHandler = new EntityEventsHandler(this);

        passengerUtil = new PassengerUtil();
        environmentUtil = new EnvironmentUtil(this);

        loadFeatures();

        mManager = supportsPaperFeature && svManager.isNewerOrVersion(18, 2) ? new MPaperManager(this) : new MSpigotManager(this);
    }

    public void onEnable() {

        if(!versionCheck()) return;

        entityUtil = svManager.isNewerOrVersion(18, 0) ? (IEntityUtil) svManager.getPackageObject("util.EntityUtil", this) : new EntityUtil(this);

        loadPluginDependencies();
        loadSettings(Bukkit.getConsoleSender());

        setupCommands();
        setupEvents();
        linkBStats();

        Bukkit.getPluginManager().callEvent(new GSitLoadedEvent(this));

        mManager.sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-enabled");

        printPluginLinks(Bukkit.getConsoleSender());
        uManager.checkForUpdates();
    }

    public void onDisable() {

        unload();
        mManager.sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-disabled");
    }

    private void loadSettings(CommandSender Sender) {

        if(!connectDatabase(Sender)) return;

        toggleManager.createTable();
    }

    public void reload(CommandSender Sender) {
        GSitReloadEvent reloadEvent = new GSitReloadEvent(this);
        Bukkit.getPluginManager().callEvent(reloadEvent);
        if(reloadEvent.isCancelled()) return;
        unload();
        cManager.reload();
        mManager.loadMessages();
        loadPluginDependencies();
        loadSettings(Sender);
        printPluginLinks(Sender);
        uManager.checkForUpdates();
        Bukkit.getPluginManager().callEvent(new GSitLoadedEvent(this));
    }

    private void unload() {

        dManager.close();
        sitManager.clearSeats();
        playerSitManager.clearSeats();
        poseManager.clearPoses();
        crawlManager.clearCrawls();

        if(placeholderAPILink != null) placeholderAPILink.unregister();
    }

    private void setupCommands() {

        getCommand("gsit").setExecutor(new GSitCommand(this));
        getCommand("gsit").setTabCompleter(new GSitTabComplete(this));
        getCommand("glay").setExecutor(new GLayCommand(this));
        getCommand("glay").setTabCompleter(new EmptyTabComplete());
        getCommand("glay").setPermissionMessage(mManager.getMessage("Messages.command-permission-error"));
        getCommand("gbellyflop").setExecutor(new GBellyFlopCommand(this));
        getCommand("gbellyflop").setTabCompleter(new EmptyTabComplete());
        getCommand("gbellyflop").setPermissionMessage(mManager.getMessage("Messages.command-permission-error"));
        getCommand("gspin").setExecutor(new GSpinCommand(this));
        getCommand("gspin").setTabCompleter(new EmptyTabComplete());
        getCommand("gspin").setPermissionMessage(mManager.getMessage("Messages.command-permission-error"));
        getCommand("gcrawl").setExecutor(new GCrawlCommand(this));
        getCommand("gcrawl").setTabCompleter(new GCrawlTabComplete(this));
        getCommand("gsitreload").setExecutor(new GSitReloadCommand(this));
        getCommand("gsitreload").setTabCompleter(new EmptyTabComplete());
        getCommand("gsitreload").setPermissionMessage(mManager.getMessage("Messages.command-permission-error"));
    }

    private void setupEvents() {

        getServer().getPluginManager().registerEvents(new PlayerEvents(this), this);
        getServer().getPluginManager().registerEvents(new PlayerSitEvents(this), this);
        getServer().getPluginManager().registerEvents(new BlockEvents(this), this);
        getServer().getPluginManager().registerEvents(new InteractEvents(this), this);

        Listener entityEvents = svManager.isNewerOrVersion(18, 0) ? (Listener) svManager.getPackageObject("events.EntityEvents", this) : null;
        if(entityEvents == null) entityEvents = (Listener) svManager.getLegacyPackageObject("events.EntityEvents", this);
        if(entityEvents != null) getServer().getPluginManager().registerEvents(entityEvents, this);

        getServer().getPluginManager().registerEvents(new SpinConfusionEvent(this), this);
    }

    private boolean versionCheck() {
        if(!svManager.isNewerOrVersion(16, 0) || (svManager.isNewerOrVersion(18, 0) && !svManager.isAvailable())) {
            mManager.sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-version", "%Version%", svManager.getServerVersion());
            uManager.checkForUpdates();
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }
        return true;
    }

    private boolean connectDatabase(CommandSender Sender) {
        boolean connect = dManager.connect();
        if(connect) return true;
        mManager.sendMessage(Sender, "Plugin.plugin-data");
        Bukkit.getPluginManager().disablePlugin(this);
        return false;
    }

    private void loadFeatures() {

        try {
            Class.forName("io.papermc.paper.event.entity.EntityMoveEvent");
            supportsPaperFeature = true;
        } catch (ClassNotFoundException ignored) { supportsPaperFeature = false; }

        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            supportsTaskFeature = true;
        } catch (ClassNotFoundException ignored) { supportsTaskFeature = false; }

        if(Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuardLink = new WorldGuardLink(this);
            worldGuardLink.registerFlags();
        }
    }

    private void loadPluginDependencies() {

        Plugin plugin = Bukkit.getPluginManager().getPlugin("GriefPrevention");
        if(plugin != null && plugin.isEnabled()) griefPreventionLink = new GriefPreventionLink(this);
        else griefPreventionLink = null;

        plugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if(plugin != null && plugin.isEnabled()) {
            placeholderAPILink = new PlaceholderAPILink(this);
            placeholderAPILink.register();
        } else placeholderAPILink = null;

        plugin = Bukkit.getPluginManager().getPlugin("PlotSquared");
        if(plugin != null && plugin.isEnabled()) {
            plotSquaredLink = new PlotSquaredLink(this);
            if(!plotSquaredLink.isVersionSupported()) plotSquaredLink = null;
        } else plotSquaredLink = null;

        plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if(plugin != null && plugin.isEnabled()) {
            if(worldGuardLink == null) {
                worldGuardLink = new WorldGuardLink(this);
                worldGuardLink.registerFlags();
            }
        } else worldGuardLink = null;
    }

    private void printPluginLinks(CommandSender Sender) {
        if(griefPreventionLink != null) mManager.sendMessage(Sender, "Plugin.plugin-link", "%Link%", Bukkit.getPluginManager().getPlugin("GriefPrevention").getName());
        if(placeholderAPILink != null) mManager.sendMessage(Sender, "Plugin.plugin-link", "%Link%", Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getName());
        if(plotSquaredLink != null) mManager.sendMessage(Sender, "Plugin.plugin-link", "%Link%", Bukkit.getPluginManager().getPlugin("PlotSquared").getName());
        if(worldGuardLink != null) mManager.sendMessage(Sender, "Plugin.plugin-link", "%Link%", Bukkit.getPluginManager().getPlugin("WorldGuard").getName());
    }

    private void linkBStats() {

        BStatsLink bstats = new BStatsLink(this, 4914);

        bstats.addCustomChart(new BStatsLink.SimplePie("plugin_language", () -> cManager.L_LANG));
        bstats.addCustomChart(new BStatsLink.AdvancedPie("minecraft_version_player_amount", () -> Map.of(svManager.getServerVersion(), Bukkit.getOnlinePlayers().size())));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_sit_feature", () -> sitManager.getSitUsedCount()));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("seconds_sit_feature", () -> (int) sitManager.getSitUsedSeconds()));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_psit_feature", () -> playerSitManager.getPlayerSitUsedCount()));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("seconds_psit_feature", () -> (int) playerSitManager.getPlayerSitUsedSeconds()));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_pose_feature", () -> poseManager.getPoseUsedCount()));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("seconds_pose_feature", () -> (int) poseManager.getPoseUsedSeconds()));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_crawl_feature", () -> crawlManager.getCrawlUsedCount()));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("seconds_crawl_feature", () -> (int) crawlManager.getCrawlUsedSeconds()));

        sitManager.resetFeatureUsedCount();
        playerSitManager.resetFeatureUsedCount();
        poseManager.resetFeatureUsedCount();
        crawlManager.resetFeatureUsedCount();
    }

}