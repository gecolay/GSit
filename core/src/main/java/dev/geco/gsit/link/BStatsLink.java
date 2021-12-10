package dev.geco.gsit.link;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

public class BStatsLink {
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public static final int B_STATS_VERSION = 1;
    
    private static final String URL = "https://bStats.org/submitData/bukkit";
    
    private boolean enabled;
    
    private static boolean logFailedRequests;
    
    private static boolean logSentData;
    
    private static boolean logResponseStatusText;
    
    private static String serverUUID;
    
    private final Plugin plugin;
    
    private final int pluginId;
    
    private final List<CustomChart> charts = new ArrayList<>();
    
    public BStatsLink(Plugin plugin, int pluginId) {
        
        this.plugin = plugin;
        this.pluginId = pluginId;
        
        File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
        File configFile = new File(bStatsFolder, "config.yml");
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        
        if(!config.isSet("serverUuid")) {
            
            config.addDefault("enabled", true);
            
            config.addDefault("serverUuid", UUID.randomUUID().toString());
            
            config.addDefault("logFailedRequests", false);
            
            config.addDefault("logSentData", false);
            
            config.addDefault("logResponseStatusText", false);
            
            config.options().header(
                    "bStats collects some data for plugin authors like how many servers are using their plugins.\n" +
                            "To honor their work, you should not disable it.\n" +
                            "This has nearly no effect on the server performance!\n" +
                            "Check out https://bStats.org/ to learn more :)"
            ).copyDefaults(true);
            
            try { config.save(configFile); } catch (IOException ignored) { }
            
        }
        
        enabled = config.getBoolean("enabled", true);
        serverUUID = config.getString("serverUuid");
        logFailedRequests = config.getBoolean("logFailedRequests", false);
        logSentData = config.getBoolean("logSentData", false);
        logResponseStatusText = config.getBoolean("logResponseStatusText", false);
        
        if(enabled) {
            
            boolean found = false;
            
            for (Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
                try {
                    service.getField("B_STATS_VERSION");
                    found = true;
                    break;
                } catch (NoSuchFieldException ignored) { }
            }
            
            Bukkit.getServicesManager().register(BStatsLink.class, this, plugin, ServicePriority.Normal);
            
            if(!found) startSubmitting();
            
        }
        
    }
    
    public boolean isEnabled() { return enabled; }
    
    public void addCustomChart(CustomChart chart) {
        
        if(chart == null) throw new IllegalArgumentException("Chart cannot be null!");
        
        charts.add(chart);
        
    }
    
    private void startSubmitting() {
        
        final Runnable submitTask = () -> {
            
            if (!plugin.isEnabled()) {
                scheduler.shutdown();
                return;
            }
            
            Bukkit.getScheduler().runTask(plugin, this::submitData);
            
        };
        
        long initialDelay = (long) (1000 * 60 * (3 + Math.random() * 3));
        long secondDelay = (long) (1000 * 60 * (Math.random() * 30));
        
        scheduler.schedule(submitTask, initialDelay, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(submitTask, initialDelay + secondDelay, 1000 * 60 * 30, TimeUnit.MILLISECONDS);
        
    }
    
    public JsonObject getPluginData() {
        
        JsonObject data = new JsonObject();
        
        String pluginName = plugin.getDescription().getName();
        String pluginVersion = plugin.getDescription().getVersion();
        
        data.addProperty("pluginName", pluginName);
        data.addProperty("id", pluginId);
        data.addProperty("pluginVersion", pluginVersion);
        
        JsonArray customCharts = new JsonArray();
        
        for(CustomChart customChart : charts) {
            
            JsonObject chart = customChart.getRequestJsonObject();
            
            if(chart == null) continue;
            
            customCharts.add(chart);
            
        }
        
        data.add("customCharts", customCharts);
        
        return data;
        
    }
    
    private JsonObject getServerData() {
        
        int playerAmount;
        
        try {
            
            Method onlinePlayersMethod = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers");
            
            playerAmount = onlinePlayersMethod.getReturnType().equals(Collection.class) ? ((Collection<?>) onlinePlayersMethod.invoke(Bukkit.getServer())).size() : ((Player[]) onlinePlayersMethod.invoke(Bukkit.getServer())).length;
            
        } catch (Exception e) { playerAmount = Bukkit.getOnlinePlayers().size(); }
        
        int onlineMode = Bukkit.getOnlineMode() ? 1 : 0;
        String bukkitVersion = Bukkit.getVersion();
        String bukkitName = Bukkit.getName();
        
        String javaVersion = System.getProperty("java.version");
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String osVersion = System.getProperty("os.version");
        int coreCount = Runtime.getRuntime().availableProcessors();
        
        JsonObject data = new JsonObject();
        
        data.addProperty("serverUUID", serverUUID);
        
        data.addProperty("playerAmount", playerAmount);
        data.addProperty("onlineMode", onlineMode);
        data.addProperty("bukkitVersion", bukkitVersion);
        data.addProperty("bukkitName", bukkitName);
        
        data.addProperty("javaVersion", javaVersion);
        data.addProperty("osName", osName);
        data.addProperty("osArch", osArch);
        data.addProperty("osVersion", osVersion);
        data.addProperty("coreCount", coreCount);
        
        return data;
        
    }
    
    private void submitData() {
        
        final JsonObject data = getServerData();
        
        JsonArray pluginData = new JsonArray();
        
        for(Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
            
            try {
                
                service.getField("B_STATS_VERSION");
                
                for(RegisteredServiceProvider<?> provider : Bukkit.getServicesManager().getRegistrations(service)) {
                    
                    try {
                        
                        Object plugin = provider.getService().getMethod("getPluginData").invoke(provider.getProvider());
                        
                        if (plugin instanceof JsonObject) pluginData.add((JsonObject) plugin);
                        else {
                            
                            try {
                                
                                Class<?> jsonObjectJsonSimple = Class.forName("org.json.simple.JSONObject");
                                
                                if(plugin.getClass().isAssignableFrom(jsonObjectJsonSimple)) {
                                    
                                    Method jsonStringGetter = jsonObjectJsonSimple.getDeclaredMethod("toJSONString");
                                    jsonStringGetter.setAccessible(true);
                                    String jsonString = (String) jsonStringGetter.invoke(plugin);
                                    JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();
                                    pluginData.add(object);
                                    
                                }
                                
                            } catch (ClassNotFoundException e) {
                                
                                if(logFailedRequests) this.plugin.getLogger().log(Level.SEVERE, "Encountered unexpected exception", e);
                                
                            }
                            
                        }
                        
                    } catch (NullPointerException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) { }
                    
                }
                
            } catch (NoSuchFieldException ignored) { }
            
        }
        
        data.add("plugins", pluginData);
        
        new Thread(() -> {
            
            try {
                sendData(plugin, data);
            } catch (Exception e) {
                if(logFailedRequests) plugin.getLogger().log(Level.WARNING, "Could not submit plugin stats of " + plugin.getName(), e);
            }
            
        }).start();
        
    }
    
    private static void sendData(Plugin plugin, JsonObject data) throws Exception {
        
        if(data == null) throw new IllegalArgumentException("Data cannot be null!");
        
        if(Bukkit.isPrimaryThread()) throw new IllegalAccessException("This method must not be called from the main thread!");
        
        if(logSentData) plugin.getLogger().info("Sending data to bStats: " + data);
        
        HttpsURLConnection connection = (HttpsURLConnection) new URL(URL).openConnection();
        
        byte[] compressedData = compress(data.toString());
        
        connection.setRequestMethod("POST");
        connection.addRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Connection", "close");
        connection.addRequestProperty("Content-Encoding", "gzip");
        connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "MC-Server/" + B_STATS_VERSION);
        
        connection.setDoOutput(true);
        
        try(DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
            outputStream.write(compressedData);
        }
        
        StringBuilder builder = new StringBuilder();
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            
            String line;
            
            while ((line = bufferedReader.readLine()) != null) builder.append(line);
            
        }
        
        if(logResponseStatusText) plugin.getLogger().info("Sent data to bStats and received response: " + builder);
        
    }
    
    private static byte[] compress(final String str) throws IOException {
        
        if(str == null) return null;
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try(GZIPOutputStream gzip = new GZIPOutputStream(outputStream)) {
            gzip.write(str.getBytes(StandardCharsets.UTF_8));
        }
        
        return outputStream.toByteArray();
        
    }
    
    public static abstract class CustomChart {
        
        final String chartId;
        
        CustomChart(String chartId) {
            
            if(chartId == null || chartId.isEmpty()) throw new IllegalArgumentException("ChartId cannot be null or empty!");
            
            this.chartId = chartId;
            
        }
        
        private JsonObject getRequestJsonObject() {
            
            JsonObject chart = new JsonObject();
            
            chart.addProperty("chartId", chartId);
            
            try {
                
                JsonObject data = getChartData();
                
                if (data == null) return null;
                
                chart.add("data", data);
                
            } catch (Throwable t) {
                
                if(logFailedRequests) Bukkit.getLogger().log(Level.WARNING, "Failed to get data for custom chart with id " + chartId, t);
                
                return null;
                
            }
            
            return chart;
            
        }
        
        protected abstract JsonObject getChartData() throws Exception;
        
    }
    
    public static class SimplePie extends CustomChart {
        
        private final Callable<String> callable;
        
        public SimplePie(String chartId, Callable<String> callable) {
            
            super(chartId);
            
            this.callable = callable;
            
        }
        
        @Override
        protected JsonObject getChartData() throws Exception {
            
            JsonObject data = new JsonObject();
            
            String value = callable.call();
            
            if(value == null || value.isEmpty()) return null;
            
            data.addProperty("value", value);
            
            return data;
            
        }
        
    }
    
    public static class AdvancedPie extends CustomChart {
        
        private final Callable<Map<String, Integer>> callable;
        
        public AdvancedPie(String chartId, Callable<Map<String, Integer>> callable) {
            
            super(chartId);
            
            this.callable = callable;
            
        }
        
        @Override
        protected JsonObject getChartData() throws Exception {
            
            JsonObject data = new JsonObject();
            JsonObject values = new JsonObject();
            
            Map<String, Integer> map = callable.call();
            
            if (map == null || map.isEmpty()) return null;
            
            boolean allSkipped = true;
            
            for(Map.Entry<String, Integer> entry : map.entrySet()) {
                
                if(entry.getValue() == 0) continue;
                
                allSkipped = false;
                values.addProperty(entry.getKey(), entry.getValue());
                
            }
            
            if(allSkipped) return null;
            
            data.add("values", values);
            
            return data;
            
        }
        
    }
    
    public static class DrilldownPie extends CustomChart {
        
        private final Callable<Map<String, Map<String, Integer>>> callable;
        
        public DrilldownPie(String chartId, Callable<Map<String, Map<String, Integer>>> callable) {
            
            super(chartId);
            
            this.callable = callable;
            
        }
        
        @Override
        public JsonObject getChartData() throws Exception {
            
            JsonObject data = new JsonObject();
            JsonObject values = new JsonObject();
            
            Map<String, Map<String, Integer>> map = callable.call();
            
            if (map == null || map.isEmpty()) return null;
            
            boolean reallyAllSkipped = true;
            
            for(Map.Entry<String, Map<String, Integer>> entryValues : map.entrySet()) {
                
                JsonObject value = new JsonObject();
                
                boolean allSkipped = true;
                
                for(Map.Entry<String, Integer> valueEntry : map.get(entryValues.getKey()).entrySet()) {
                    
                    value.addProperty(valueEntry.getKey(), valueEntry.getValue());
                    allSkipped = false;
                    
                }
                
                if(!allSkipped) {
                    
                    reallyAllSkipped = false;
                    values.add(entryValues.getKey(), value);
                    
                }
                
            }
            
            if(reallyAllSkipped) return null;
            
            data.add("values", values);
            
            return data;
            
        }
        
    }
    
    public static class SingleLineChart extends CustomChart {
        
        private final Callable<Integer> callable;
        
        public SingleLineChart(String chartId, Callable<Integer> callable) {
            
            super(chartId);
            
            this.callable = callable;
            
        }
        
        @Override
        protected JsonObject getChartData() throws Exception {
            
            JsonObject data = new JsonObject();
            
            int value = callable.call();
            
            if (value == 0) return null;
            
            data.addProperty("value", value);
            
            return data;
            
        }
        
    }
    
    public static class MultiLineChart extends CustomChart {
        
        private final Callable<Map<String, Integer>> callable;
        
        public MultiLineChart(String chartId, Callable<Map<String, Integer>> callable) {
            
            super(chartId);
            
            this.callable = callable;
            
        }
        
        @Override
        protected JsonObject getChartData() throws Exception {
            
            JsonObject data = new JsonObject();
            JsonObject values = new JsonObject();
            
            Map<String, Integer> map = callable.call();
            
            if(map == null || map.isEmpty()) return null;
            
            boolean allSkipped = true;
            
            for(Map.Entry<String, Integer> entry : map.entrySet()) {
                
                if(entry.getValue() == 0) continue;
                
                allSkipped = false;
                values.addProperty(entry.getKey(), entry.getValue());
                
            }
            
            if(allSkipped) return null;
            
            data.add("values", values);
            
            return data;
            
        }
        
    }
    
    public static class SimpleBarChart extends CustomChart {
        
        private final Callable<Map<String, Integer>> callable;
        
        public SimpleBarChart(String chartId, Callable<Map<String, Integer>> callable) {
            
            super(chartId);
            
            this.callable = callable;
            
        }
        
        @Override
        protected JsonObject getChartData() throws Exception {
            
            JsonObject data = new JsonObject();
            JsonObject values = new JsonObject();
            
            Map<String, Integer> map = callable.call();
            
            if(map == null || map.isEmpty()) return null;
            
            for(Map.Entry<String, Integer> entry : map.entrySet()) {
                
                JsonArray categoryValues = new JsonArray();
                
                categoryValues.add(new JsonPrimitive(entry.getValue()));
                values.add(entry.getKey(), categoryValues);
                
            }
            
            data.add("values", values);
            
            return data;
            
        }
        
    }
    
    public static class AdvancedBarChart extends CustomChart {
        
        private final Callable<Map<String, int[]>> callable;
        
        public AdvancedBarChart(String chartId, Callable<Map<String, int[]>> callable) {
            
            super(chartId);
            
            this.callable = callable;
            
        }
        
        @Override
        protected JsonObject getChartData() throws Exception {
            
            JsonObject data = new JsonObject();
            JsonObject values = new JsonObject();
            
            Map<String, int[]> map = callable.call();
            
            if(map == null || map.isEmpty()) return null;
            
            boolean allSkipped = true;
            
            for(Map.Entry<String, int[]> entry : map.entrySet()) {
                
                if (entry.getValue().length == 0) continue;
                
                allSkipped = false;
                
                JsonArray categoryValues = new JsonArray();
                
                for(int categoryValue : entry.getValue()) categoryValues.add(new JsonPrimitive(categoryValue));
                
                values.add(entry.getKey(), categoryValues);
                
            }
            
            if(allSkipped) return null;
            
            data.add("values", values);
            
            return data;
            
        }
        
    }
    
}