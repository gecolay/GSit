package dev.geco.gsit.manager;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

import org.bukkit.command.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;

abstract public class MManager {

    protected final GSitMain GPM;

    protected String PREFIX_PLACEHOLDER = "[P]";
    protected String PREFIX_REPLACE = "&7[&6" + GSitMain.getInstance().NAME + "&7]";
    protected String DEFAULT_LANG;
    protected final HashMap<String, FileConfiguration> messages = new HashMap<>();
    protected final HashMap<UUID, String> languages = new HashMap<>();

    public MManager(GSitMain GPluginMain) {
        GPM = GPluginMain;
        loadMessages();
    }

    public FileConfiguration getMessages() { return getMessages(DEFAULT_LANG); }

    public FileConfiguration getMessages(String LanguageCode) { return messages.getOrDefault(LanguageCode, new YamlConfiguration()); }

    public void loadMessages() {
        messages.clear();
        boolean betterSave = GPM.getSVManager().isNewerOrVersion(18, 2);
        try(JarFile jarFile = new JarFile(Paths.get(GPM.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toString())) {
            Enumeration<JarEntry> jarFiles = jarFile.entries();
            while(jarFiles.hasMoreElements()) {
                JarEntry jarEntry = jarFiles.nextElement();
                if(!jarEntry.getName().startsWith("lang") || jarEntry.isDirectory()) continue;
                File langFile = new File(GPM.getDataFolder(), jarEntry.getName());
                if(!betterSave) {
                    if(!langFile.exists()) GPM.saveResource(jarEntry.getName(), false);
                    continue;
                }
                FileConfiguration lang = YamlConfiguration.loadConfiguration(langFile);
                InputStream langSteam = GPM.getResource(jarEntry.getName());
                if(langSteam != null) {
                    FileConfiguration langSteamConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(langSteam, StandardCharsets.UTF_8));
                    lang.setDefaults(langSteamConfig);
                    YamlConfigurationOptions options = (YamlConfigurationOptions) lang.options();
                    options.parseComments(true).copyDefaults(true).width(500);
                    lang.loadFromString(lang.saveToString());
                    for(String comments : lang.getKeys(true)) lang.setComments(comments, langSteamConfig.getComments(comments));
                }
                lang.save(langFile);
            }
        } catch (Throwable e) { e.printStackTrace(); }
        File langFolder = new File(GPM.getDataFolder(), "lang");
        for(File langFile : Objects.requireNonNull(langFolder.listFiles())) messages.put(langFile.getName().replaceFirst("lang/", "").replaceFirst(".yml", ""), YamlConfiguration.loadConfiguration(langFile));
        DEFAULT_LANG = messages.containsKey(GPM.getCManager().L_LANG) ? GPM.getCManager().L_LANG : "en_us";
    }

    abstract public String getAsJSON(String Text, Object... RawReplaceList);

    abstract public String toFormattedMessage(String Text, Object... RawReplaceList);

    abstract public void sendMessage(CommandSender Target, String Message, Object... ReplaceList);

    abstract public void sendActionBarMessage(Player Target, String Message, Object... ReplaceList);

    public String getMessage(String Message, Object... ReplaceList) { return getLanguageMessage(Message, DEFAULT_LANG, ReplaceList); }

    public String getLanguageMessage(String Message, CommandSender Target, Object... ReplaceList) { return getLanguageMessage(Message, getLanguage(Target), ReplaceList); }

    public String getLanguageMessage(String Message, String LanguageCode, Object... ReplaceList) { return toFormattedMessage(getRawLanguageMessage(Message, LanguageCode, ReplaceList)); }

    public String getRawMessage(String Message, Object... ReplaceList) { return getRawLanguageMessage(Message, DEFAULT_LANG, ReplaceList); }

    public String getRawLanguageMessage(String Message, CommandSender Target, Object... ReplaceList) { return getRawLanguageMessage(Message, getLanguage(Target), ReplaceList); }

    public String getRawLanguageMessage(String Message, String LanguageCode, Object... ReplaceList) { return replaceWithLanguageCode(Message == null || Message.isEmpty() ? "" : getMessages(LanguageCode).getString(Message, Message), LanguageCode, ReplaceList); }

    public String getLanguage(CommandSender Target) {
        if(!(Target instanceof Entity)) return DEFAULT_LANG;
        String language = languages.get(((Entity) Target).getUniqueId());
        if(language != null) return language;
        if(GPM.getCManager().L_CLIENT_LANG && Target instanceof Player) {
            language = ((Player) Target).getLocale();
            if(messages.containsKey(language)) return language;
        }
        return DEFAULT_LANG;
    }

    public void setLanguage(CommandSender Target, String LanguageCode) {
        if(!(Target instanceof Entity)) return;
        languages.put(((Entity) Target).getUniqueId(), LanguageCode);
    }

    protected String replaceText(String Text, Object ... ReplaceList) {
        if(ReplaceList.length == 0 || ReplaceList.length % 2 != 0) return Text;
        for(int count = 0; count < ReplaceList.length; count += 2) {
            if(ReplaceList[count] != null && ReplaceList[count + 1] != null) {
                String key = ReplaceList[count].toString();
                String value = ReplaceList[count + 1].toString();
                Text = Text.replace(key, value);
            }
        }
        return Text;
    }

    private String replaceWithLanguageCode(String Message, String LanguageCode, Object ... ReplaceList) {
        Message = replaceText(Message, ReplaceList);
        return Message.replace(PREFIX_PLACEHOLDER, getMessages(LanguageCode).getString("Plugin.plugin-prefix", PREFIX_REPLACE));
    }

}