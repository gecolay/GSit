package dev.geco.gsit.manager;

import org.jetbrains.annotations.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.*;

import org.bukkit.command.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;

abstract public class MManager {

    protected final GSitMain GPM;
    protected final String PREFIX_PLACEHOLDER = "[P]";
    protected final String PREFIX_REPLACE = "&7[&6" + GSitMain.NAME + "&7]";
    protected final char AMPERSAND_CHAR = '&';
    protected final char COLOR_CHAR = org.bukkit.ChatColor.COLOR_CHAR;
    protected final Pattern HEX_PATTERN = Pattern.compile("#([a-fA-F0-9]{6})");
    protected final HashMap<String, FileConfiguration> messages = new HashMap<>();
    protected String DEFAULT_LANG;

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
                    if(lang.getKeys(true).equals(langSteamConfig.getKeys(true))) continue;
                    lang.setDefaults(langSteamConfig);
                    YamlConfigurationOptions options = (YamlConfigurationOptions) lang.options();
                    options.parseComments(true).copyDefaults(true).width(500);
                    lang.loadFromString(lang.saveToString());
                    for(String comments : lang.getKeys(true)) lang.setComments(comments, langSteamConfig.getComments(comments));
                    lang.save(langFile);
                } else if(!langFile.exists()) GPM.saveResource(jarEntry.getName(), false);
            }
        } catch (Throwable e) { e.printStackTrace(); }
        File langFolder = new File(GPM.getDataFolder(), "lang");
        for(File langFile : Objects.requireNonNull(langFolder.listFiles())) messages.put(langFile.getName().replaceFirst("lang/", "").replaceFirst(".yml", ""), YamlConfiguration.loadConfiguration(langFile));
        DEFAULT_LANG = messages.containsKey(GPM.getCManager().L_LANG) ? GPM.getCManager().L_LANG : "en_us";
    }

    abstract public String toFormattedMessage(String Text, Object... RawReplaceList);

    abstract public void sendMessage(@NotNull CommandSender Target, String Message, Object... ReplaceList);

    abstract public void sendActionBarMessage(@NotNull Player Target, String Message, Object... ReplaceList);

    public String getMessage(String Message, Object... ReplaceList) { return getMessage(Message, null, ReplaceList); }

    public String getMessage(String Message, Entity Entity, Object... ReplaceList) { return getMessageByLanguage(Message, getLanguage(Entity), ReplaceList); }

    public String getMessageByLanguage(String Message, String LanguageCode, Object... ReplaceList) { return toFormattedMessage(getRawMessageByLanguage(Message, LanguageCode, ReplaceList)); }

    public String getRawMessageByLanguage(String Message, String LanguageCode, Object... ReplaceList) { return replaceWithLanguageCode(Message == null || Message.isEmpty() ? "" : getMessages(LanguageCode).getString(Message, Message), LanguageCode, ReplaceList); }

    public String getLanguage(CommandSender Target) {
        if(!GPM.getCManager().L_CLIENT_LANG || !(Target instanceof Player)) return DEFAULT_LANG;
        String language = ((Player) Target).getLocale();
        return messages.containsKey(language) ? language : DEFAULT_LANG;
    }

    protected String replaceText(String Text, Object @NotNull ... ReplaceList) {
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

    protected String replaceHexColorsDirect(String Text) {
        Matcher matcher = HEX_PATTERN.matcher(Text);
        StringBuilder result = new StringBuilder(Text.length());
        int lastIndex = 0;
        while(matcher.find()) {
            result.append(Text, lastIndex, matcher.start()).append(COLOR_CHAR).append('x');
            char[] chars = matcher.group().substring(1).toCharArray();
            for(char c : chars) result.append(COLOR_CHAR).append(c);
            lastIndex = matcher.end();
        }
        result.append(Text.substring(lastIndex));
        return result.toString();
    }

    private String replaceWithLanguageCode(String Message, String LanguageCode, Object ... ReplaceList) {
        Message = replaceText(Message, ReplaceList);
        return Message.replace(PREFIX_PLACEHOLDER, getMessages(LanguageCode).getString("Plugin.plugin-prefix", PREFIX_REPLACE));
    }

}