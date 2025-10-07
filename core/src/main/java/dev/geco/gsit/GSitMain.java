package dev.geco.gsit;

import dev.geco.gsit.api.event.GSitLoadedEvent;
import dev.geco.gsit.api.event.GSitReloadEvent;
import dev.geco.gsit.cmd.GBellyflopCommand;
import dev.geco.gsit.cmd.GCrawlCommand;
import dev.geco.gsit.cmd.GLayBackCommand;
import dev.geco.gsit.cmd.GLayCommand;
import dev.geco.gsit.cmd.GSitCommand;
import dev.geco.gsit.cmd.GSitReloadCommand;
import dev.geco.gsit.cmd.GSpinCommand;
import dev.geco.gsit.cmd.tab.EmptyTabComplete;
import dev.geco.gsit.cmd.tab.GCrawlTabComplete;
import dev.geco.gsit.cmd.tab.GSitTabComplete;
import dev.geco.gsit.event.BlockEventHandler;
import dev.geco.gsit.event.EntityEventHandler;
import dev.geco.gsit.event.SitEventHandler;
import dev.geco.gsit.event.PlayerEventHandler;
import dev.geco.gsit.event.PlayerSitEventHandler;
import dev.geco.gsit.event.feature.SpinConfusionEventHandler;
import dev.geco.gsit.metric.BStatsMetric;
import dev.geco.gsit.link.GriefPreventionLink;
import dev.geco.gsit.link.PlaceholderAPILink;
import dev.geco.gsit.link.PlotSquaredLink;
import dev.geco.gsit.link.WorldGuardLink;
import dev.geco.gsit.service.ConfigService;
import dev.geco.gsit.service.CrawlService;
import dev.geco.gsit.service.DataService;
import dev.geco.gsit.service.MessageService;
import dev.geco.gsit.service.PermissionService;
import dev.geco.gsit.service.PlayerSitService;
import dev.geco.gsit.service.PoseService;
import dev.geco.gsit.service.SitService;
import dev.geco.gsit.service.ToggleService;
import dev.geco.gsit.service.TaskService;
import dev.geco.gsit.service.UpdateService;
import dev.geco.gsit.service.VersionService;
import dev.geco.gsit.service.message.PaperMessageService;
import dev.geco.gsit.service.message.SpigotMessageService;
import dev.geco.gsit.util.LegacyEntityUtil;
import dev.geco.gsit.util.EnvironmentUtil;
import dev.geco.gsit.util.EntityUtil;
import dev.geco.gsit.util.PassengerUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class GSitMain extends JavaPlugin {

    public static final String NAME = "GSit";
    public static final String RESOURCE_ID = "62325";

    private final int BSTATS_RESOURCE_ID = 4914;
    private static GSitMain gSitMain;
    private VersionService versionService;
    private ConfigService configService;
    private MessageService messageService;
    private UpdateService updateService;
    private PermissionService permissionService;
    private TaskService taskService;
    private DataService dataService;
    private SitService sitService;
    private PlayerSitService playerSitService;
    private PoseService poseService;
    private CrawlService crawlService;
    private ToggleService toggleService;
    private EntityEventHandler entityEventHandler;
    private PassengerUtil passengerUtil;
    private EnvironmentUtil environmentUtil;
    private EntityUtil entityUtil;
    private GriefPreventionLink griefPreventionLink;
    private PlaceholderAPILink placeholderAPILink;
    private PlotSquaredLink plotSquaredLink;
    private WorldGuardLink worldGuardLink;
    private boolean supportsTaskFeature = false;
    private boolean isPaperServer = false;
    private boolean isFoliaServer = false;

    public static GSitMain getInstance() { return gSitMain; }

    public VersionService getVersionManager() { return versionService; }

    public ConfigService getConfigService() { return configService; }

    public MessageService getMessageService() { return messageService; }

    public UpdateService getUpdateService() { return updateService; }

    public PermissionService getPermissionService() { return permissionService; }

    public TaskService getTaskService() { return taskService; }

    public DataService getDataService() { return dataService; }

    public SitService getSitService() { return sitService; }

    public PlayerSitService getPlayerSitService() { return playerSitService; }

    public PoseService getPoseService() { return poseService; }

    public CrawlService getCrawlService() { return crawlService; }

    public ToggleService getToggleService() { return toggleService; }

    public EntityEventHandler getEntityEventHandler() { return entityEventHandler; }

    public PassengerUtil getPassengerUtil() { return passengerUtil; }

    public EnvironmentUtil getEnvironmentUtil() { return environmentUtil; }

    public EntityUtil getEntityUtil() { return entityUtil; }

    public GriefPreventionLink getGriefPreventionLink() { return griefPreventionLink; }

    public PlaceholderAPILink getPlaceholderAPILink() { return placeholderAPILink; }

    public PlotSquaredLink getPlotSquaredLink() { return plotSquaredLink; }

    public WorldGuardLink getWorldGuardLink() { return worldGuardLink; }

    public boolean supportsTaskFeature() { return supportsTaskFeature; }

    public boolean isPaperServer() { return isPaperServer; }

    public boolean isFoliaServer() { return isFoliaServer; }

    public void onLoad() {
        gSitMain = this;

        versionService = new VersionService(this);
        configService = new ConfigService(this);

        updateService = new UpdateService(this);
        permissionService = new PermissionService();
        taskService = new TaskService(this);
        dataService = new DataService(this);
        sitService = new SitService(this);
        playerSitService = new PlayerSitService(this);
        poseService = new PoseService(this);
        crawlService = new CrawlService(this);
        toggleService = new ToggleService(this);

        entityEventHandler = new EntityEventHandler(this);

        passengerUtil = new PassengerUtil();
        environmentUtil = new EnvironmentUtil(this);

        loadFeatures();

        messageService = isPaperServer && versionService.isNewerOrVersion(18, 2) ? new PaperMessageService(this) : new SpigotMessageService(this);
    }

    public void onEnable() {
        if(!versionCheck()) return;

        entityUtil = versionService.isNewerOrVersion(18, 0) ? (EntityUtil) versionService.getPackageObjectInstance("util.EntityUtil", this) : new LegacyEntityUtil(this);

        loadPluginDependencies();
        loadSettings(Bukkit.getConsoleSender());

        setupCommands();
        setupEvents();
        setupBStatsMetric();

        Bukkit.getPluginManager().callEvent(new GSitLoadedEvent(this));

        messageService.sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-enabled");

        printPluginLinks(Bukkit.getConsoleSender());
        updateService.checkForUpdates();
    }

    public void onDisable() {
        unload();
        messageService.sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-disabled");
    }

    private void loadSettings(CommandSender sender) {
        if(!connectDatabase(sender)) return;
        toggleService.createDataTables();
    }

    public void reload(CommandSender sender) {
        GSitReloadEvent reloadEvent = new GSitReloadEvent(this);
        Bukkit.getPluginManager().callEvent(reloadEvent);
        if(reloadEvent.isCancelled()) return;

        unload();
        configService.reload();
        messageService.loadMessages();
        loadPluginDependencies();
        loadSettings(sender);
        printPluginLinks(sender);
        updateService.checkForUpdates();

        Bukkit.getPluginManager().callEvent(new GSitLoadedEvent(this));
    }

    private void unload() {
        dataService.close();
        sitService.removeAllSeats();
        playerSitService.removeAllPlayerSitStacks();
        poseService.removeAllPoses();
        crawlService.removeAllCrawls();

        if(placeholderAPILink != null) placeholderAPILink.unregister();
        if(worldGuardLink != null) worldGuardLink.unregisterFlagHandlers();
    }

    private void setupCommands() {
        getCommand("gsit").setExecutor(new GSitCommand(this));
        getCommand("gsit").setTabCompleter(new GSitTabComplete(this));
        getCommand("glay").setExecutor(new GLayCommand(this));
        getCommand("glay").setTabCompleter(new EmptyTabComplete());
        getCommand("glay").setPermissionMessage(messageService.getMessage("Messages.command-permission-error"));
        getCommand("glayback").setExecutor(new GLayBackCommand(this));
        getCommand("glayback").setTabCompleter(new EmptyTabComplete());
        getCommand("glayback").setPermissionMessage(messageService.getMessage("Messages.command-permission-error"));
        getCommand("gbellyflop").setExecutor(new GBellyflopCommand(this));
        getCommand("gbellyflop").setTabCompleter(new EmptyTabComplete());
        getCommand("gbellyflop").setPermissionMessage(messageService.getMessage("Messages.command-permission-error"));
        getCommand("gspin").setExecutor(new GSpinCommand(this));
        getCommand("gspin").setTabCompleter(new EmptyTabComplete());
        getCommand("gspin").setPermissionMessage(messageService.getMessage("Messages.command-permission-error"));
        getCommand("gcrawl").setExecutor(new GCrawlCommand(this));
        getCommand("gcrawl").setTabCompleter(new GCrawlTabComplete(this));
        getCommand("gsitreload").setExecutor(new GSitReloadCommand(this));
        getCommand("gsitreload").setTabCompleter(new EmptyTabComplete());
        getCommand("gsitreload").setPermissionMessage(messageService.getMessage("Messages.command-permission-error"));
    }

    private void setupEvents() {
        getServer().getPluginManager().registerEvents(new PlayerEventHandler(this), this);
        getServer().getPluginManager().registerEvents(new PlayerSitEventHandler(this), this);
        getServer().getPluginManager().registerEvents(new BlockEventHandler(this), this);
        getServer().getPluginManager().registerEvents(new SitEventHandler(this), this);

        Listener mcvEntityEventHandler = versionService.isNewerOrVersion(18, 0) ? (Listener) versionService.getPackageObjectInstance("event.EntityEventHandler", this) : null;
        if(mcvEntityEventHandler == null) mcvEntityEventHandler = (Listener) versionService.getLegacyPackageObjectInstance("event.EntityEventHandler", this);
        if(mcvEntityEventHandler != null) getServer().getPluginManager().registerEvents(mcvEntityEventHandler, this);

        getServer().getPluginManager().registerEvents(new SpinConfusionEventHandler(this), this);
    }

    private boolean versionCheck() {
        if(!versionService.isNewerOrVersion(16, 0) || (versionService.isNewerOrVersion(18, 0) && !versionService.isAvailable())) {
            messageService.sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-version", "%Version%", versionService.getServerVersion());
            updateService.checkForUpdates();
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }
        return true;
    }

    private boolean connectDatabase(CommandSender sender) {
        boolean connected = dataService.connect();
        if(connected) return true;
        messageService.sendMessage(sender, "Plugin.plugin-data");
        Bukkit.getPluginManager().disablePlugin(this);
        return false;
    }

    private void loadFeatures() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            supportsTaskFeature = true;
        } catch(ClassNotFoundException e) { supportsTaskFeature = false; }

        try {
            Class.forName("io.papermc.paper.event.entity.EntityMoveEvent");
            isPaperServer = true;
        } catch(ClassNotFoundException e) { isPaperServer = false; }

        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
            isFoliaServer = true;
        } catch(ClassNotFoundException e) { isFoliaServer = false; }

        if(Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuardLink = new WorldGuardLink();
            worldGuardLink.registerFlags();
        }
    }

    private void loadPluginDependencies() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("GriefPrevention");
        if(plugin != null && plugin.isEnabled() && configService.TRUSTED_REGION_ONLY) griefPreventionLink = new GriefPreventionLink(this);
        else griefPreventionLink = null;

        plugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if(plugin != null && plugin.isEnabled()) {
            placeholderAPILink = new PlaceholderAPILink(this);
            placeholderAPILink.register();
        } else placeholderAPILink = null;

        plugin = Bukkit.getPluginManager().getPlugin("PlotSquared");
        if(plugin != null && plugin.isEnabled() && configService.TRUSTED_REGION_ONLY) {
            plotSquaredLink = new PlotSquaredLink(this);
            if(!plotSquaredLink.isPlotSquaredVersionSupported()) plotSquaredLink = null;
        } else plotSquaredLink = null;

        plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if(plugin != null && plugin.isEnabled()) {
            if(worldGuardLink == null) {
                worldGuardLink = new WorldGuardLink();
                worldGuardLink.registerFlags();
            }
            worldGuardLink.registerFlagHandlers();
        } else worldGuardLink = null;
    }

    private void printPluginLinks(CommandSender sender) {
        if(griefPreventionLink != null) messageService.sendMessage(sender, "Plugin.plugin-link", "%Link%", Bukkit.getPluginManager().getPlugin("GriefPrevention").getName());
        if(placeholderAPILink != null) messageService.sendMessage(sender, "Plugin.plugin-link", "%Link%", Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getName());
        if(plotSquaredLink != null) messageService.sendMessage(sender, "Plugin.plugin-link", "%Link%", Bukkit.getPluginManager().getPlugin("PlotSquared").getName());
        if(worldGuardLink != null) messageService.sendMessage(sender, "Plugin.plugin-link", "%Link%", Bukkit.getPluginManager().getPlugin("WorldGuard").getName());
    }

    private void setupBStatsMetric() {
        BStatsMetric bStatsMetric = new BStatsMetric(this, BSTATS_RESOURCE_ID);

        bStatsMetric.addCustomChart(new BStatsMetric.SimplePie("plugin_language", () -> configService.L_LANG));
        bStatsMetric.addCustomChart(new BStatsMetric.AdvancedPie("minecraft_version_player_amount", () -> Map.of(versionService.getServerVersion(), Bukkit.getOnlinePlayers().size())));
        bStatsMetric.addCustomChart(new BStatsMetric.SingleLineChart("use_sit_feature", () -> sitService.getSitUsageCount()));
        bStatsMetric.addCustomChart(new BStatsMetric.SingleLineChart("seconds_sit_feature", () -> (int) sitService.getSitUsageTimeInSeconds()));
        bStatsMetric.addCustomChart(new BStatsMetric.SingleLineChart("use_psit_feature", () -> playerSitService.getPlayerSitUsageCount()));
        bStatsMetric.addCustomChart(new BStatsMetric.SingleLineChart("seconds_psit_feature", () -> (int) playerSitService.getPlayerSitUsageTimeInSeconds()));
        bStatsMetric.addCustomChart(new BStatsMetric.SingleLineChart("use_pose_feature", () -> poseService.getPoseUsageCount()));
        bStatsMetric.addCustomChart(new BStatsMetric.SingleLineChart("seconds_pose_feature", () -> (int) poseService.getPoseUsageTimeInSeconds()));
        bStatsMetric.addCustomChart(new BStatsMetric.SingleLineChart("use_crawl_feature", () -> crawlService.getCrawlUsageCount()));
        bStatsMetric.addCustomChart(new BStatsMetric.SingleLineChart("seconds_crawl_feature", () -> (int) crawlService.getCrawlUsageTimeInSeconds()));

        sitService.resetSitUsageStats();
        playerSitService.resetPlayerSitUsageStats();
        poseService.resetPoseUsageStats();
        crawlService.resetCrawlUsageStats();
    }

}