package dev.geco.gsit;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;

import dev.geco.gsit.cmd.*;
import dev.geco.gsit.cmd.tab.*;
import dev.geco.gsit.events.*;
import dev.geco.gsit.link.*;
import dev.geco.gsit.manager.*;
import dev.geco.gsit.objects.*;
import dev.geco.gsit.util.*;
import dev.geco.gsit.values.*;

public class GSitMain extends JavaPlugin {

    private FileConfiguration messages;

    public FileConfiguration getMessages() { return messages; }

    private CManager cmanager;

    public CManager getCManager() { return cmanager; }

    private String prefix;

    public String getPrefix() { return prefix; }

    private Values values;

    public Values getValues() { return values; }

    private ISitManager sitmanager;

    public ISitManager getSitManager() { return sitmanager; }

    private IPoseManager posemanager;

    public IPoseManager getPoseManager() { return posemanager; }

    private IPlayerSitManager playersitmanager;

    public IPlayerSitManager getPlayerSitManager() { return playersitmanager; }

    private ICrawlManager crawlmanager;

    public ICrawlManager getCrawlManager() { return crawlmanager; }

    private ToggleManager togglemanager;

    public ToggleManager getToggleManager() { return togglemanager; }

    private UManager umanager;

    public UManager getUManager() { return umanager; }

    private PManager pmanager;

    public PManager getPManager() { return pmanager; }

    private MManager mmanager;

    public MManager getMManager() { return mmanager; }

    private PassengerUtil passengerutil;

    public PassengerUtil getPassengerUtil() { return passengerutil; }

    private SitUtil situtil;

    public SitUtil getSitUtil() { return situtil; }

    private PoseUtil poseutil;

    public PoseUtil getPoseUtil() { return poseutil; }

    private ITeleportUtil teleportutil;

    public ITeleportUtil getTeleportUtil() { return teleportutil; }

    private PlSqLink plsqlink;

    public PlSqLink getPlotSquared() { return plsqlink; }

    private WoGuLink wogulink;

    public WoGuLink getWorldGuard() { return wogulink; }

    public final String NAME = "GSit";

    public final String RESOURCE = "62325";

    private static GSitMain GPM;

    public static GSitMain getInstance() { return GPM; }

    private void setupSettings() {
        copyLangFiles();
        messages = YamlConfiguration.loadConfiguration(new File("plugins/" + NAME + "/" + Values.LANG_PATH, getConfig().getString("Lang.lang", "en_en") + Values.YML_FILETYP));
        prefix = getMessages().getString("Plugin.plugin-prefix");
        getToggleManager().loadToggleData();
    }

    private void linkBStats() {
        BStatsLink bstats = new BStatsLink(getInstance(), 4914);
        bstats.addCustomChart(new BStatsLink.SimplePie("plugin_language", new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getConfig().getString("Lang.lang", "en_en").toLowerCase();
            }
        }));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_sit_feature", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                if(getSitManager() == null) return 0;
                int c = getSitManager().getFeatureUsedCount();
                getSitManager().resetFeatureUsedCount();
                return c;
            }
        }));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_pose_feature", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                if(getPoseManager() == null) return 0;
                int c = getPoseManager().getFeatureUsedCount();
                getPoseManager().resetFeatureUsedCount();
                return c;
            }
        }));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_psit_feature", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                if(getPlayerSitManager() == null) return 0;
                int c = getPlayerSitManager().getFeatureUsedCount();
                getPlayerSitManager().resetFeatureUsedCount();
                return c;
            }
        }));
        bstats.addCustomChart(new BStatsLink.SingleLineChart("use_crawl_feature", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                if(getCrawlManager() == null) return 0;
                int c = getCrawlManager().getFeatureUsedCount();
                getCrawlManager().resetFeatureUsedCount();
                return c;
            }
        }));
    }

    public void onLoad() {
        GPM = this;
        saveDefaultConfig();
        cmanager = new CManager(getInstance());
        values = new Values();
        umanager = new UManager(getInstance(), RESOURCE);
        pmanager = new PManager(getInstance());
        mmanager = new MManager(getInstance());
        togglemanager = new ToggleManager(getInstance());
        passengerutil = new PassengerUtil(getInstance());
        situtil = new SitUtil(getInstance());
        poseutil = new PoseUtil(getInstance());
        if(Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            wogulink = new WoGuLink(getInstance());
            wogulink.registerFlags();
        }
    }

    public void onEnable() {
        if(!versionCheck()) return;
        sitmanager = NMSManager.isNewerOrVersion(17, 0) ? (ISitManager) NMSManager.getPackageObject("gsit", "manager.SitManager", getInstance()) : new SitManager(getInstance());
        posemanager = NMSManager.isNewerOrVersion(17, 0) ? (IPoseManager) NMSManager.getPackageObject("gsit", "manager.PoseManager", getInstance()) : null;
        playersitmanager = new PlayerSitManager(getInstance());
        crawlmanager = NMSManager.isNewerOrVersion(17, 0) ? (ICrawlManager) NMSManager.getPackageObject("gsit", "manager.CrawlManager", getInstance()) : null;
        teleportutil = NMSManager.isNewerOrVersion(17, 0) ? (ITeleportUtil) NMSManager.getPackageObject("gsit", "util.TeleportUtil", null) : null;
        getCommand("gsit").setExecutor(new GSitCommand(getInstance()));
        getCommand("gsit").setTabCompleter(new GSitTabComplete(getInstance()));
        getCommand("glay").setExecutor(new GLayCommand(getInstance()));
        getCommand("gbellyflop").setExecutor(new GBellyFlopCommand(getInstance()));
        getCommand("gcrawl").setExecutor(new GCrawlCommand(getInstance()));
        getCommand("gsitreload").setExecutor(new GSitReloadCommand(getInstance()));
        getServer().getPluginManager().registerEvents(new PlayerEvents(getInstance()), getInstance());
        getServer().getPluginManager().registerEvents(new PlayerSitEvents(getInstance()), getInstance());
        getServer().getPluginManager().registerEvents(new BlockEvents(getInstance()), getInstance());
        getServer().getPluginManager().registerEvents(new InteractEvents(getInstance()), getInstance());
        setupSettings();
        linkBStats();
        getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-enabled");
        loadPluginDepends(Bukkit.getConsoleSender());
        updateCheck();
    }

    public void onDisable() {
        getSitManager().clearSeats();
        if(getPoseManager() != null) getPoseManager().clearPoses();
        getToggleManager().saveToggleData();
        getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-disabled");
    }

    private void loadPluginDepends(CommandSender s) {
        if(Bukkit.getPluginManager().getPlugin("PlotSquared") != null && Bukkit.getPluginManager().isPluginEnabled("PlotSquared")) {
            plsqlink = new PlSqLink(getInstance());
            getMManager().sendMessage(s, "Plugin.plugin-link", "%Link%", "PlotSquared");
        } else plsqlink = null;
        if(Bukkit.getPluginManager().getPlugin("WorldGuard") != null && Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            if(wogulink == null) {
                wogulink = new WoGuLink(getInstance());
                wogulink.registerFlags();
            }
            getMManager().sendMessage(s, "Plugin.plugin-link", "%Link%", "WorldGuard");
        } else wogulink = null;
    }

    public void copyLangFiles() { for(String l : Arrays.asList("cz_cz", "de_de", "en_en", "es_es", "fi_fi", "fr_fr", "ja_jp", "pl_pl", "pt_br", "ru_ru", "sk_sk", "tr_tr", "zh_cn", "zh_tw")) if(!new File("plugins/" + NAME + "/" + Values.LANG_PATH + "/" + l + Values.YML_FILETYP).exists()) saveResource(Values.LANG_PATH + "/" + l + Values.YML_FILETYP, false); }

    public void reload(CommandSender s) {
        reloadConfig();
        getCManager().reload();
        getSitManager().clearSeats();
        if(getPoseManager() != null) getPoseManager().clearPoses();
        getToggleManager().saveToggleData();
        setupSettings();
        loadPluginDepends(s);
        updateCheck();
    }

    private void updateCheck() {
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
        List<String> version_list = new ArrayList<>(); {
            version_list.add("v1_17_R1");
            version_list.add("v1_18_R1");
        }
        String v = Bukkit.getServer().getClass().getPackage().getName();
        v = v.substring(v.lastIndexOf('.') + 1);
        if(!NMSManager.isNewerOrVersion(14, 0) || (NMSManager.isNewerOrVersion(17, 0) && !version_list.contains(v))) {
            getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-version", "%Version%", v);
            updateCheck();
            Bukkit.getPluginManager().disablePlugin(getInstance());
            return false;
        }
        return true;
    }

}