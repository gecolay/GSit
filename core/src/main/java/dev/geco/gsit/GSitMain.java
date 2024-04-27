package dev.geco.gsit;

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

    private SVManager svManager;
    public SVManager getSVManager() { return svManager; }

    private CManager cManager;
    public CManager getCManager() { return cManager; }

    private DManager dManager;
    public DManager getDManager() { return dManager; }

    private SitManager sitManager;
    public SitManager getSitManager() { return sitManager; }

    private PlayerSitManager playerSitManager;
    public PlayerSitManager getPlayerSitManager() { return playerSitManager; }

    private PoseManager poseManager;
    public PoseManager getPoseManager() { return poseManager; }

    private CrawlManager crawlManager;
    public CrawlManager getCrawlManager() { return crawlManager; }

    private ToggleManager toggleManager;
    public ToggleManager getToggleManager() { return toggleManager; }

    private UManager uManager;
    public UManager getUManager() { return uManager; }

    private PManager pManager;
    public PManager getPManager() { return pManager; }

    private TManager tManager;
    public TManager getTManager() { return tManager; }

    private MManager mManager;
    public MManager getMManager() { return mManager; }

    private PassengerUtil passengerUtil;
    public PassengerUtil getPassengerUtil() { return passengerUtil; }

    private EnvironmentUtil environmentUtil;
    public EnvironmentUtil getEnvironmentUtil() { return environmentUtil; }

    private IEntityUtil entityUtil;
    public IEntityUtil getEntityUtil() { return entityUtil; }

    private IPackageUtil packageUtil;
    public IPackageUtil getPackageUtil() { return packageUtil; }

    private GriefPreventionLink griefPreventionLink;
    public GriefPreventionLink getGriefPreventionLink() { return griefPreventionLink; }

    private PlaceholderAPILink placeholderAPILink;
    public PlaceholderAPILink getPlaceholderAPILink() { return placeholderAPILink; }

    private PlotSquaredLink plotSquaredLink;
    public PlotSquaredLink getPlotSquaredLink() { return plotSquaredLink; }

    private ViaVersionLink viaVersionLink;
    public ViaVersionLink getViaVersionLink() { return viaVersionLink; }

    private WorldGuardLink worldGuardLink;
    public WorldGuardLink getWorldGuardLink() { return worldGuardLink; }

    private boolean supportsPaperFeature = false;
    public boolean supportsPaperFeature() { return supportsPaperFeature; }

    private boolean supportsTaskFeature = false;
    public boolean supportsTaskFeature() { return supportsTaskFeature; }

    public final String NAME = "GSit";

    public final String RESOURCE = "62325";

    private static GSitMain GPM;

    public static GSitMain getInstance() { return GPM; }

    private void loadSettings(CommandSender Sender) {

        if(!connectDatabase(Sender)) return;

        getToggleManager().createTable();

        if(getPackageUtil() != null) getPackageUtil().registerPlayers();
    }

    private void linkBStats() {

        BStatsLink bstats = new BStatsLink(getInstance(), 4914);

        bstats.addCustomChart(new BStatsLink.SimplePie("plugin_language", () -> getCManager().L_LANG));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_sit_feature", () -> getSitManager().getSitUsedCount()));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("seconds_sit_feature", () -> (int) getSitManager().getSitUsedSeconds()));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_psit_feature", () -> getPlayerSitManager().getPlayerSitUsedCount()));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("seconds_psit_feature", () -> (int) getPlayerSitManager().getPlayerSitUsedSeconds()));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_pose_feature", () -> getPoseManager().getPoseUsedCount()));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("seconds_pose_feature", () -> (int) getPoseManager().getPoseUsedSeconds()));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_crawl_feature", () -> getCrawlManager().getCrawlUsedCount()));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("seconds_crawl_feature", () -> (int) getCrawlManager().getCrawlUsedSeconds()));

        getSitManager().resetFeatureUsedCount();
        getPlayerSitManager().resetFeatureUsedCount();
        getPoseManager().resetFeatureUsedCount();
        getCrawlManager().resetFeatureUsedCount();
    }

    public void onLoad() {

        GPM = this;

        svManager = new SVManager(getInstance());
        cManager = new CManager(getInstance());
        dManager = new DManager(getInstance());
        uManager = new UManager(getInstance());
        pManager = new PManager(getInstance());
        tManager = new TManager(getInstance());
        sitManager = new SitManager(getInstance());
        playerSitManager = new PlayerSitManager(getInstance());
        poseManager = new PoseManager(getInstance());
        crawlManager = new CrawlManager(getInstance());
        toggleManager = new ToggleManager(getInstance());

        passengerUtil = new PassengerUtil();
        environmentUtil = new EnvironmentUtil(getInstance());

        preloadPluginDependencies();

        mManager = supportsPaperFeature() && getSVManager().isNewerOrVersion(18, 2) ? new MPaperManager(getInstance()) : new MSpigotManager(getInstance());
    }

    public void onEnable() {

        if(!versionCheck()) return;

        entityUtil = getSVManager().isNewerOrVersion(17, 0) ? (IEntityUtil) getSVManager().getPackageObject("util.EntityUtil") : new EntityUtil();
        packageUtil = getSVManager().isNewerOrVersion(17, 0) ? (IPackageUtil) getSVManager().getPackageObject("util.PackageUtil") : null;

        loadSettings(Bukkit.getConsoleSender());

        setupCommands();
        setupEvents();
        linkBStats();

        getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-enabled");

        loadPluginDependencies(Bukkit.getConsoleSender());
        getUManager().checkForUpdates();
    }

    public void onDisable() {

        unload();
        getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-disabled");
    }

    private void unload() {

        getDManager().close();
        getSitManager().clearSeats();
        getPlayerSitManager().clearSeats();
        getPoseManager().clearPoses();
        getCrawlManager().clearCrawls();

        if(getPlaceholderAPILink() != null) getPlaceholderAPILink().unregister();
        if(getPackageUtil() != null) getPackageUtil().unregisterPlayers();
    }

    private void setupCommands() {

        getCommand("gsit").setExecutor(new GSitCommand(getInstance()));
        getCommand("gsit").setTabCompleter(new GSitTabComplete(getInstance()));
        getCommand("glay").setExecutor(new GLayCommand(getInstance()));
        getCommand("glay").setTabCompleter(new EmptyTabComplete());
        getCommand("glay").setPermissionMessage(getMManager().getMessage("Messages.command-permission-error"));
        getCommand("gbellyflop").setExecutor(new GBellyFlopCommand(getInstance()));
        getCommand("gbellyflop").setTabCompleter(new EmptyTabComplete());
        getCommand("gbellyflop").setPermissionMessage(getMManager().getMessage("Messages.command-permission-error"));
        getCommand("gspin").setExecutor(new GSpinCommand(getInstance()));
        getCommand("gspin").setTabCompleter(new EmptyTabComplete());
        getCommand("gspin").setPermissionMessage(getMManager().getMessage("Messages.command-permission-error"));
        getCommand("gcrawl").setExecutor(new GCrawlCommand(getInstance()));
        getCommand("gcrawl").setTabCompleter(new GCrawlTabComplete(getInstance()));
        getCommand("gsitreload").setExecutor(new GSitReloadCommand(getInstance()));
        getCommand("gsitreload").setTabCompleter(new EmptyTabComplete());
        getCommand("gsitreload").setPermissionMessage(getMManager().getMessage("Messages.command-permission-error"));
    }

    private void setupEvents() {

        getServer().getPluginManager().registerEvents(new PlayerEvents(getInstance()), getInstance());
        getServer().getPluginManager().registerEvents(new PlayerSitEvents(getInstance()), getInstance());
        getServer().getPluginManager().registerEvents(new BlockEvents(getInstance()), getInstance());
        getServer().getPluginManager().registerEvents(new InteractEvents(getInstance()), getInstance());

        Listener entityEvents = getSVManager().isNewerOrVersion(17, 0) ? (Listener) getSVManager().getPackageObject("events.EntityEvents", getInstance()) : null;
        if(entityEvents == null) entityEvents = (Listener) getSVManager().getLegacyPackageObject("events.EntityEvents", getInstance());
        if(entityEvents != null) getServer().getPluginManager().registerEvents(entityEvents, getInstance());

        getServer().getPluginManager().registerEvents(new SpinConfusionEvent(getInstance()), getInstance());
    }

    private void preloadPluginDependencies() {

        try {
            Class.forName("io.papermc.paper.event.entity.EntityMoveEvent");
            supportsPaperFeature = true;
        } catch (ClassNotFoundException ignored) { supportsPaperFeature = false; }

        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            supportsTaskFeature = true;
        } catch (ClassNotFoundException ignored) { supportsTaskFeature = false; }

        if(Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuardLink = new WorldGuardLink(getInstance());
            getWorldGuardLink().registerFlags();
        }
    }

    private void loadPluginDependencies(CommandSender Sender) {

        Plugin plugin = Bukkit.getPluginManager().getPlugin("GriefPrevention");

        if(plugin != null && plugin.isEnabled()) {
            griefPreventionLink = new GriefPreventionLink(getInstance());
            getMManager().sendMessage(Sender, "Plugin.plugin-link", "%Link%", plugin.getName());
        } else griefPreventionLink = null;

        plugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");

        if(plugin != null && plugin.isEnabled()) {
            placeholderAPILink = new PlaceholderAPILink(getInstance());
            getMManager().sendMessage(Sender, "Plugin.plugin-link", "%Link%", plugin.getName());
            getPlaceholderAPILink().register();
        } else placeholderAPILink = null;

        plugin = Bukkit.getPluginManager().getPlugin("PlotSquared");

        if(plugin != null && plugin.isEnabled()) {
            plotSquaredLink = new PlotSquaredLink(getInstance());
            if(getPlotSquaredLink().isVersionSupported()) getMManager().sendMessage(Sender, "Plugin.plugin-link", "%Link%", plugin.getName());
            else plotSquaredLink = null;
        } else plotSquaredLink = null;

        plugin = Bukkit.getPluginManager().getPlugin("ViaVersion");

        if(getPackageUtil() != null && plugin != null && plugin.isEnabled()) {
            viaVersionLink = new ViaVersionLink(getInstance());
            getMManager().sendMessage(Sender, "Plugin.plugin-link", "%Link%", plugin.getName());
        } else viaVersionLink = null;

        plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");

        if(plugin != null && plugin.isEnabled()) {
            if(worldGuardLink == null) {
                worldGuardLink = new WorldGuardLink(getInstance());
                getWorldGuardLink().registerFlags();
            }
            getMManager().sendMessage(Sender, "Plugin.plugin-link", "%Link%", plugin.getName());
        } else worldGuardLink = null;
    }

    public void reload(CommandSender Sender) {

        Bukkit.getPluginManager().callEvent(new GSitReloadEvent(getInstance()));

        getCManager().reload();
        getMManager().loadMessages();

        unload();

        loadSettings(Sender);
        loadPluginDependencies(Sender);
        getUManager().checkForUpdates();
    }

    private boolean connectDatabase(CommandSender Sender) {

        boolean connect = getDManager().connect();

        if(connect) return true;

        getMManager().sendMessage(Sender, "Plugin.plugin-data");

        Bukkit.getPluginManager().disablePlugin(getInstance());

        return false;
    }

    private boolean versionCheck() {

        if(!getSVManager().isNewerOrVersion(16, 0) || (getSVManager().isNewerOrVersion(17, 0) && !getSVManager().isAvailable())) {

            getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-version", "%Version%", getSVManager().getServerVersion());

            getUManager().checkForUpdates();

            Bukkit.getPluginManager().disablePlugin(getInstance());

            return false;
        }

        return true;
    }

}