package dev.geco.gsit.manager;

import java.io.*;
import java.util.*;

import org.bukkit.scheduler.*;
import org.bukkit.configuration.file.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.values.*;

public class ToggleManager {

    private final GSitMain GPM;

    public ToggleManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    private File toggleFile;

    private FileConfiguration toggleData;

    private final List<UUID> toggleList = new ArrayList<>();

    private final List<UUID> playerToggleList = new ArrayList<>();

    private final List<UUID> crawlToggleList = new ArrayList<>();

    private BukkitRunnable task;

    public boolean canSit(UUID UUID) { return GPM.getCManager().S_DEFAULT_SIT_MODE != toggleList.contains(UUID); }

    public boolean canPlayerSit(UUID UUID) { return GPM.getCManager().PS_DEFAULT_SIT_MODE != playerToggleList.contains(UUID); }

    public boolean canCrawl(UUID UUID) { return !crawlToggleList.contains(UUID); }

    public void setCanSit(UUID UUID, boolean Toggle) {

        if((Toggle && GPM.getCManager().S_DEFAULT_SIT_MODE) || (!Toggle && !GPM.getCManager().S_DEFAULT_SIT_MODE)) {

            toggleList.remove(UUID);
        } else {

            toggleList.add(UUID);
        }
    }

    public void setCanPlayerSit(UUID UUID, boolean PlayerToggle) {

        if((PlayerToggle && GPM.getCManager().PS_DEFAULT_SIT_MODE) || (!PlayerToggle && !GPM.getCManager().PS_DEFAULT_SIT_MODE)) {

            playerToggleList.remove(UUID);
        } else {

            playerToggleList.add(UUID);
        }
    }

    public void setCanCrawl(UUID UUID, boolean Toggle) {

        if(Toggle) {

            crawlToggleList.remove(UUID);
        } else {

            crawlToggleList.add(UUID);
        }
    }

    public void loadToggleData() {

        toggleList.clear();

        playerToggleList.clear();

        crawlToggleList.clear();

        toggleFile = new File("plugins/" + GPM.NAME, PluginValues.DATA_PATH + "/" + PluginValues.TOGGLE_FILE + PluginValues.DATA_FILETYP);

        toggleData = YamlConfiguration.loadConfiguration(toggleFile);

        for(String uuid : toggleData.getStringList("T")) toggleList.add(UUID.fromString(uuid));

        for(String uuid : toggleData.getStringList("P")) playerToggleList.add(UUID.fromString(uuid));

        for(String uuid : toggleData.getStringList("C")) crawlToggleList.add(UUID.fromString(uuid));

        startAutoSave();
    }

    public void saveToggleData() {

        stopAutoSave();

        quickSaveToggleData();
    }

    private void quickSaveToggleData() {

        toggleData.set("T", null);
        toggleData.set("P", null);
        toggleData.set("C", null);

        List<String> toggles = new ArrayList<>();

        for(UUID uuid : toggleList) toggles.add(uuid.toString());

        toggleData.set("T", toggles);

        List<String> playerToggles = new ArrayList<>();

        for(UUID uuid : playerToggleList) playerToggles.add(uuid.toString());

        toggleData.set("P", playerToggles);

        List<String> crawlToggles = new ArrayList<>();

        for(UUID uuid : crawlToggleList) crawlToggles.add(uuid.toString());

        toggleData.set("C", crawlToggles);

        saveFile(toggleFile, toggleData);
    }

    private void startAutoSave() {

        stopAutoSave();

        task = new BukkitRunnable() {

            @Override
            public void run() {

                quickSaveToggleData();
            }
        };

        long time = 20 * 180;

        task.runTaskTimerAsynchronously(GPM, time, time);
    }

    private void stopAutoSave() { if(task != null) task.cancel(); }

    private void saveFile(File File, FileConfiguration FileConfiguration) { try { FileConfiguration.save(File); } catch (Exception ignored) { } }

}