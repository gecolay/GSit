package dev.geco.gsit.manager;

import java.io.*;
import java.util.*;

import org.bukkit.configuration.file.*;
import org.bukkit.scheduler.BukkitRunnable;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.values.*;

public class ToggleManager {
    
    private final GSitMain GPM;
    
    public ToggleManager(GSitMain GPluginMain) { GPM = GPluginMain; }
    
    private File TData;
    
    private FileConfiguration TD;
    
    private final List<UUID> t = new ArrayList<>();

    private final List<UUID> pt = new ArrayList<>();
    
    private BukkitRunnable r;
    
    
    public boolean canSit(UUID U) { return GPM.getCManager().S_DEFAULT_SIT_MODE != t.contains(U); }

    public boolean canPlayerSit(UUID U) { return GPM.getCManager().PS_DEFAULT_SIT_MODE != pt.contains(U); }
    
    public void setCanSit(UUID U, boolean T) {
        if((T && GPM.getCManager().S_DEFAULT_SIT_MODE) || (!T && !GPM.getCManager().S_DEFAULT_SIT_MODE)) {
            t.remove(U);
        } else {
            t.add(U);
        }
    }

    public void setCanPlayerSit(UUID U, boolean P) {
        if((P && GPM.getCManager().PS_DEFAULT_SIT_MODE) || (!P && !GPM.getCManager().PS_DEFAULT_SIT_MODE)) {
            pt.remove(U);
        } else {
            pt.add(U);
        }
    }
    
    
    public void loadToggleData() {
        t.clear();
        pt.clear();
        TData = new File("plugins/" + GPM.NAME, Values.DATA_PATH + "/" + Values.TOGGLE_FILE + Values.DATA_FILETYP);
        TD = YamlConfiguration.loadConfiguration(TData);
        for(String z : TD.getStringList("T")) t.add(UUID.fromString(z));
        for(String z : TD.getStringList("P")) pt.add(UUID.fromString(z));
        startAutoSave();
    }
    
    public void saveToggleData() {
        stopAutoSave();
        quickSaveToggleData();
    }
    
    private void quickSaveToggleData() {
        TD.set("T", null);
        TD.set("P", null);
        List<String> tc = new ArrayList<>();
        for(UUID z : t) tc.add(z.toString());
        TD.set("T", tc);
        List<String> pc = new ArrayList<>();
        for(UUID z : pt) pc.add(z.toString());
        TD.set("P", pc);
        saveFile(TData, TD);
    }
    
    private void startAutoSave() {
        stopAutoSave();
        r = new BukkitRunnable() {
            @Override
            public void run() {
                quickSaveToggleData();
            }
        };
        long t = 20 * 180;
        r.runTaskTimerAsynchronously(GPM, t, t);
    }
    
    private void stopAutoSave() { if(r != null) r.cancel(); }
    
    private void saveFile(File f, FileConfiguration fc) { try { fc.save(f); } catch(IOException e) { } }
    
}