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

        entityEventsHandler = new EntityEventsHandler(getInstance());

        passengerUtil = new PassengerUtil();
        environmentUtil = new EnvironmentUtil(getInstance());

        loadFeatures();

        mManager = supportsPaperFeature() && getSVManager().isNewerOrVersion(18, 2) ? new MPaperManager(getInstance()) : new MSpigotManager(getInstance());
    }

    public void onEnable() {

        if(!versionCheck()) return;

        entityUtil = getSVManager().isNewerOrVersion(18, 0) ? (IEntityUtil) getSVManager().getPackageObject("util.EntityUtil", getInstance()) : new EntityUtil(getInstance());

        loadPluginDependencies();
        loadSettings(Bukkit.getConsoleSender());

        setupCommands();
        setupEvents();
        linkBStats();

        Bukkit.getPluginManager().callEvent(new GSitLoadedEvent(getInstance()));

        getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-enabled");

        printPluginLinks(Bukkit.getConsoleSender());
        getUManager().checkForUpdates();
    }

    public void onDisable() {

        unload();
        getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-disabled");
    }

    private void loadSettings(CommandSender Sender) {

        if(!connectDatabase(Sender)) return;

        getToggleManager().createTable();
    }

    public void reload(CommandSender Sender) {
        GSitReloadEvent reloadEvent = new GSitReloadEvent(getInstance());
        Bukkit.getPluginManager().callEvent(reloadEvent);
        if(reloadEvent.isCancelled()) return;
        unload();
        getCManager().reload();
        getMManager().loadMessages();
        loadPluginDependencies();
        loadSettings(Sender);
        printPluginLinks(Sender);
        getUManager().checkForUpdates();
        Bukkit.getPluginManager().callEvent(new GSitLoadedEvent(getInstance()));
    }

    private void unload() {

        getDManager().close();
        getSitManager().clearSeats();
        getPlayerSitManager().clearSeats();
        getPoseManager().clearPoses();
        getCrawlManager().clearCrawls();

        if(getPlaceholderAPILink() != null) getPlaceholderAPILink().unregister();
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

        Listener entityEvents = getSVManager().isNewerOrVersion(18, 0) ? (Listener) getSVManager().getPackageObject("events.EntityEvents", getInstance()) : null;
        if(entityEvents == null) entityEvents = (Listener) getSVManager().getLegacyPackageObject("events.EntityEvents", getInstance());
        if(entityEvents != null) getServer().getPluginManager().registerEvents(entityEvents, getInstance());

        getServer().getPluginManager().registerEvents(new SpinConfusionEvent(getInstance()), getInstance());
    }

    private boolean versionCheck() {
        if(!getSVManager().isNewerOrVersion(16, 0) || (getSVManager().isNewerOrVersion(18, 0) && !getSVManager().isAvailable())) {
            getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-version", "%Version%", getSVManager().getServerVersion());
            getUManager().checkForUpdates();
            Bukkit.getPluginManager().disablePlugin(getInstance());
            return false;
        }
        return true;
    }

    private boolean connectDatabase(CommandSender Sender) {
        boolean connect = getDManager().connect();
        if(connect) return true;
        getMManager().sendMessage(Sender, "Plugin.plugin-data");
        Bukkit.getPluginManager().disablePlugin(getInstance());
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
            worldGuardLink = new WorldGuardLink(getInstance());
            getWorldGuardLink().registerFlags();
        }
    }

    private void loadPluginDependencies() {

        Plugin plugin = Bukkit.getPluginManager().getPlugin("GriefPrevention");
        if(plugin != null && plugin.isEnabled()) griefPreventionLink = new GriefPreventionLink(getInstance());
        else griefPreventionLink = null;

        plugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if(plugin != null && plugin.isEnabled()) {
            placeholderAPILink = new PlaceholderAPILink(getInstance());
            getPlaceholderAPILink().register();
        } else placeholderAPILink = null;

        plugin = Bukkit.getPluginManager().getPlugin("PlotSquared");
        if(plugin != null && plugin.isEnabled()) {
            plotSquaredLink = new PlotSquaredLink(getInstance());
            if(!getPlotSquaredLink().isVersionSupported()) plotSquaredLink = null;
        } else plotSquaredLink = null;

        plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if(plugin != null && plugin.isEnabled()) {
            if(worldGuardLink == null) {
                worldGuardLink = new WorldGuardLink(getInstance());
                getWorldGuardLink().registerFlags();
            }
        } else worldGuardLink = null;
    }

    private void printPluginLinks(CommandSender Sender) {
        if(griefPreventionLink != null) getMManager().sendMessage(Sender, "Plugin.plugin-link", "%Link%", Bukkit.getPluginManager().getPlugin("GriefPrevention").getName());
        if(placeholderAPILink != null) getMManager().sendMessage(Sender, "Plugin.plugin-link", "%Link%", Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getName());
        if(plotSquaredLink != null) getMManager().sendMessage(Sender, "Plugin.plugin-link", "%Link%", Bukkit.getPluginManager().getPlugin("PlotSquared").getName());
        if(worldGuardLink != null) getMManager().sendMessage(Sender, "Plugin.plugin-link", "%Link%", Bukkit.getPluginManager().getPlugin("WorldGuard").getName());
    }

    private void linkBStats() {

        BStatsLink bstats = new BStatsLink(getInstance(), 4914);

        bstats.addCustomChart(new BStatsLink.SimplePie("plugin_language", () -> getCManager().L_LANG));
        bstats.addCustomChart(new BStatsLink.AdvancedPie("minecraft_version_player_amount", () -> Map.of(GPM.getSVManager().getServerVersion(), Bukkit.getOnlinePlayers().size())));
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

}