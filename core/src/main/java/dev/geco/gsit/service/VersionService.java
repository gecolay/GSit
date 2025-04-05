package dev.geco.gsit.service;

import dev.geco.gsit.GSitMain;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;

public class VersionService {

    private final String LATEST_VERSION = "v1_21_5";
    private final HashMap<String, String> VERSION_MAPPING = new HashMap<>(); {
        VERSION_MAPPING.put("v1_18_1", "v1_18");
        VERSION_MAPPING.put("v1_19_2", "v1_19_1");
        VERSION_MAPPING.put("v1_20_1", "v1_20");
        VERSION_MAPPING.put("v1_20_4", "v1_20_3");
        VERSION_MAPPING.put("v1_20_6", "v1_20_5");
        VERSION_MAPPING.put("v1_21_1", "v1_21");
        VERSION_MAPPING.put("v1_21_3", "v1_21_2");
    }
    private final GSitMain gSitMain;
    private final String serverVersion;
    private String packagePath;
    private boolean available;

    public VersionService(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        String rawServerVersion = Bukkit.getServer().getBukkitVersion();
        serverVersion = rawServerVersion.substring(0, rawServerVersion.indexOf('-'));
        if(!isNewerOrVersion(18, 0)) return;
        packagePath = gSitMain.getClass().getPackage().getName() + ".mcv." + getPackageVersion();
        available = hasPackageClass("object.SeatEntity");
        if(available) return;
        packagePath = gSitMain.getClass().getPackage().getName() + ".mcv." + LATEST_VERSION;
        available = hasPackageClass("object.SeatEntity");
    }

    public String getServerVersion() { return serverVersion; }

    public boolean isAvailable() { return available; }

    public boolean isNewerOrVersion(int version, int subVersion) {
        String[] serverVersionSplit = serverVersion.split("\\.");
        if(Integer.parseInt(serverVersionSplit[1]) > version) return true;
        return Integer.parseInt(serverVersionSplit[1]) == version && (serverVersionSplit.length > 2 ? Integer.parseInt(serverVersionSplit[2]) >= subVersion : subVersion == 0);
    }

    public Object getLegacyPackageObjectInstance(String className, Object... parameters) {
        try {
            Class<?> mcvPackageClass = Class.forName(gSitMain.getClass().getPackage().getName() + ".mcv.v1_17_1." + className);
            if(parameters.length == 0) return mcvPackageClass.getConstructor().newInstance();
            Class<?>[] parameterTypes = Arrays.stream(parameters).map(Object::getClass).toArray(Class<?>[]::new);
            return mcvPackageClass.getConstructor(parameterTypes).newInstance(parameters);
        } catch(Throwable e) { gSitMain.getLogger().log(Level.SEVERE, "Could not get legacy package object with class name '" + className + "'!", e); }
        return null;
    }

    public Object getPackageObjectInstance(String className, Object... parameters) {
        try {
            Class<?> mcvPackageClass = Class.forName(packagePath + "." + className);
            if(parameters.length == 0) return mcvPackageClass.getConstructor().newInstance();
            Class<?>[] parameterTypes = Arrays.stream(parameters).map(Object::getClass).toArray(Class<?>[]::new);
            return mcvPackageClass.getConstructor(parameterTypes).newInstance(parameters);
        } catch(Throwable e) { gSitMain.getLogger().log(Level.SEVERE, "Could not get package object with class name '" + className + "'!", e); }
        return null;
    }

    public boolean hasPackageClass(String className) {
        try {
            Class.forName(packagePath + "." + className);
            return true;
        } catch(Throwable ignored) { }
        return false;
    }

    private String getPackageVersion() {
        String packageVersion = "v" + serverVersion.replace(".", "_");
        return VERSION_MAPPING.getOrDefault(packageVersion, packageVersion);
    }

}