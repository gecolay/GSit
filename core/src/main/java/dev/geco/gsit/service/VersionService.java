package dev.geco.gsit.service;

import dev.geco.gsit.GSitMain;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;

public class VersionService {

    private final String LATEST_VERSION = "v1_21_11";
    private final HashMap<String, String> VERSION_MAPPING = new HashMap<>(); {
        VERSION_MAPPING.put("v1_18_1", "v1_18");
        VERSION_MAPPING.put("v1_19_2", "v1_19_1");
        VERSION_MAPPING.put("v1_20_1", "v1_20");
        VERSION_MAPPING.put("v1_20_4", "v1_20_3");
        VERSION_MAPPING.put("v1_20_6", "v1_20_5");
        VERSION_MAPPING.put("v1_21_1", "v1_21");
        VERSION_MAPPING.put("v1_21_3", "v1_21_2");
        VERSION_MAPPING.put("v1_21_7", "v1_21_6");
        VERSION_MAPPING.put("v1_21_8", "v1_21_6");
        VERSION_MAPPING.put("v1_21_10", "v1_21_9");
    }
    private final GSitMain gSitMain;
    private final String serverVersion;
    private String packagePath;
    private boolean available;

    public VersionService(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        serverVersion = getMinecraftVersion();
        if(!isNewerOrVersion(new int[] {1, 18})) return;
        packagePath = gSitMain.getClass().getPackage().getName() + ".mcv." + getPackageVersion();
        available = hasPackageClass("entity.SeatEntity");
        if(available) return;
        packagePath = gSitMain.getClass().getPackage().getName() + ".mcv." + LATEST_VERSION;
        available = hasPackageClass("entity.SeatEntity");
    }

    private String getMinecraftVersion() {
        String rawServerVersion = Bukkit.getServer().getVersion();
        int mcIndexStart = rawServerVersion.indexOf("MC:");
        if(mcIndexStart != -1) {
            mcIndexStart += 3;
            int mcIndexEnd = rawServerVersion.indexOf(')', mcIndexStart);
            if(mcIndexEnd != -1) rawServerVersion = rawServerVersion.substring(mcIndexStart, mcIndexEnd);
            int mcDashIndex = rawServerVersion.indexOf('-', mcIndexStart);
            if(mcDashIndex != -1) rawServerVersion = rawServerVersion.substring(mcIndexStart, mcDashIndex);
        }
        return rawServerVersion.trim();
    }

    public String getServerVersion() { return serverVersion; }

    public boolean isAvailable() { return available; }

    public boolean isNewerOrVersion(int[] version) {
        String[] parts = serverVersion.split("\\.");
        int max = Math.max(parts.length, version.length);
        for(int i = 0; i < max; i++) {
            int sv = (i < parts.length) ? Integer.parseInt(parts[i]) : 0;
            int tv = (i < version.length) ? version[i] : 0;
            if (sv > tv) return true;
            if (sv < tv) return false;
        }
        return true;
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