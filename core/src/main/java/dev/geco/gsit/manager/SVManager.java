package dev.geco.gsit.manager;

import java.util.*;

import org.bukkit.*;

import dev.geco.gsit.GSitMain;

public class SVManager {

    private final GSitMain GPM;
    private final String LATEST_VERSION = "v1_21_4";
    private final String SERVER_VERSION;
    private String PACKAGE_PATH;
    private boolean AVAILABLE;
    private final HashMap<String, String> VERSION_MAPPING = new HashMap<>(); {
        VERSION_MAPPING.put("v1_18_1", "v1_18");
        VERSION_MAPPING.put("v1_19_2", "v1_19_1");
        VERSION_MAPPING.put("v1_20_1", "v1_20");
        VERSION_MAPPING.put("v1_20_4", "v1_20_3");
        VERSION_MAPPING.put("v1_20_6", "v1_20_5");
        VERSION_MAPPING.put("v1_21_1", "v1_21");
        VERSION_MAPPING.put("v1_21_3", "v1_21_2");
    }

    public SVManager(GSitMain GPluginMain) {
        GPM = GPluginMain;
        String version = Bukkit.getServer().getBukkitVersion();
        SERVER_VERSION = version.substring(0, version.indexOf('-'));
        PACKAGE_PATH = GPM.getClass().getPackage().getName() + ".mcv." + getPackageVersion();
        AVAILABLE = hasPackageClass("objects.SeatEntity");
        if(AVAILABLE) return;
        PACKAGE_PATH = GPM.getClass().getPackage().getName() + ".mcv." + LATEST_VERSION;
        AVAILABLE = hasPackageClass("objects.SeatEntity");
    }

    public String getServerVersion() { return SERVER_VERSION; }

    public boolean isAvailable() { return AVAILABLE; }

    public boolean isNewerOrVersion(int Version, int SubVersion) {
        String[] version = SERVER_VERSION.split("\\.");
        return Integer.parseInt(version[1]) > Version || (Integer.parseInt(version[1]) == Version && (version.length > 2 ? Integer.parseInt(version[2]) >= SubVersion : SubVersion == 0));
    }

    public Object getLegacyPackageObject(String ClassName, Object... Objects) {
        try {
            Class<?> mcvClass = Class.forName(GPM.getClass().getPackage().getName() + ".mcv.v1_17_1." + ClassName);
            if(Objects.length == 0) return mcvClass.getConstructor().newInstance();
            Class<?>[] classes = Arrays.stream(Objects).map(Object::getClass).toArray(Class<?>[]::new);
            return mcvClass.getConstructor(classes).newInstance(Objects);
        } catch (Throwable e) { e.printStackTrace(); }
        return null;
    }

    public Object getPackageObject(String ClassName, Object... Objects) {
        try {
            Class<?> mcvClass = Class.forName(PACKAGE_PATH + "." + ClassName);
            if(Objects.length == 0) return mcvClass.getConstructor().newInstance();
            Class<?>[] classes = Arrays.stream(Objects).map(Object::getClass).toArray(Class<?>[]::new);
            return mcvClass.getConstructor(classes).newInstance(Objects);
        } catch (Throwable e) { e.printStackTrace(); }
        return null;
    }

    public boolean hasPackageClass(String ClassName) {
        try {
            Class.forName(PACKAGE_PATH + "." + ClassName);
            return true;
        } catch (Throwable ignored) { }
        return false;
    }

    private String getPackageVersion() {
        String package_version = "v" + SERVER_VERSION.replace(".", "_");
        return VERSION_MAPPING.getOrDefault(package_version, package_version);
    }

}