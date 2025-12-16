package dev.geco.gsit.service;

import dev.geco.gsit.GSitMain;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract public class MessageService {

    protected final String PREFIX_PLACEHOLDER = "[P]";
    protected final String PREFIX_REPLACE = "&7[&6" + GSitMain.NAME + "&7]";
    protected final char AMPERSAND_CHAR = '&';
    protected final char COLOR_CHAR = org.bukkit.ChatColor.COLOR_CHAR;
    protected final Pattern HEX_PATTERN = Pattern.compile("#([a-fA-F0-9]{6})");
    protected final GSitMain gSitMain;
    protected final HashMap<String, FileConfiguration> messages = new HashMap<>();
    protected String defaultLanguage;

    public MessageService(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        loadMessages();
    }

    public FileConfiguration getMessages() { return getMessages(defaultLanguage); }

    public FileConfiguration getMessages(String languageCode) { return messages.getOrDefault(languageCode, new YamlConfiguration()); }

    public void loadMessages() {
        messages.clear();
        boolean betterSave = gSitMain.getVersionManager().isNewerOrVersion(new int[]{1, 18, 2});
        try(JarFile jarFile = new JarFile(Paths.get(gSitMain.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toString())) {
            Enumeration<JarEntry> jarFiles = jarFile.entries();
            while(jarFiles.hasMoreElements()) {
                JarEntry jarEntry = jarFiles.nextElement();
                if(!jarEntry.getName().startsWith("lang") || jarEntry.isDirectory()) continue;
                File langFile = new File(gSitMain.getDataFolder(), jarEntry.getName());
                if(!betterSave) {
                    if(!langFile.exists()) gSitMain.saveResource(jarEntry.getName(), false);
                    continue;
                }
                FileConfiguration lang = YamlConfiguration.loadConfiguration(langFile);
                InputStream langSteam = gSitMain.getResource(jarEntry.getName());
                if(langSteam != null) {
                    FileConfiguration langSteamConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(langSteam, StandardCharsets.UTF_8));
                    if(lang.getKeys(true).equals(langSteamConfig.getKeys(true))) continue;
                    lang.setDefaults(langSteamConfig);
                    YamlConfigurationOptions options = (YamlConfigurationOptions) lang.options();
                    options.parseComments(true).copyDefaults(true).width(500);
                    lang.loadFromString(lang.saveToString());
                    for(String comments : lang.getKeys(true)) lang.setComments(comments, langSteamConfig.getComments(comments));
                    lang.save(langFile);
                } else if(!langFile.exists()) gSitMain.saveResource(jarEntry.getName(), false);
            }
        } catch(Throwable e) { gSitMain.getLogger().log(Level.SEVERE, "Could not load messages!", e); }
        File langFolder = new File(gSitMain.getDataFolder(), "lang");
        for(File langFile : Objects.requireNonNull(langFolder.listFiles())) messages.put(langFile.getName().replaceFirst("lang/", "").replaceFirst(".yml", ""), YamlConfiguration.loadConfiguration(langFile));
        defaultLanguage = messages.containsKey(gSitMain.getConfigService().L_LANG) ? gSitMain.getConfigService().L_LANG : "en_us";
    }

    abstract public String toFormattedMessage(String text, Object... rawReplaceList);

    abstract public void sendMessage(@NotNull CommandSender target, String message, Object... replaceList);

    abstract public void sendActionBarMessage(@NotNull Player target, String message, Object... replaceList);

    public String getMessage(String message, Object... replaceList) { return getMessage(message, null, replaceList); }

    public String getMessage(String message, Entity entity, Object... replaceList) { return getTranslatedMessage(message, getLanguageForTarget(entity), replaceList); }

    public String getTranslatedMessage(String message, String languageCode, Object... replaceList) { return toFormattedMessage(getRawTranslatedMessage(message, languageCode, replaceList)); }

    public String getRawTranslatedMessage(String message, String languageCode, Object... replaceList) { return replaceWithLanguageCode(message == null || message.isEmpty() ? "" : getMessages(languageCode).getString(message, message), languageCode, replaceList); }

    public String getLanguageForTarget(CommandSender target) {
        if(!gSitMain.getConfigService().L_CLIENT_LANG || !(target instanceof Player)) return defaultLanguage;
        String language = ((Player) target).getLocale();
        return messages.containsKey(language) ? language : defaultLanguage;
    }

    protected String replaceText(String text, Object @NotNull ... replaceList) {
        if(replaceList.length == 0 || replaceList.length % 2 != 0) return text;
        for(int count = 0; count < replaceList.length; count += 2) {
            if(replaceList[count] != null && replaceList[count + 1] != null) {
                String key = replaceList[count].toString();
                String value = replaceList[count + 1].toString();
                text = text.replace(key, value);
            }
        }
        return text;
    }

    protected String replaceHexColorsDirectly(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder(text.length());
        int lastIndex = 0;
        while(matcher.find()) {
            result.append(text, lastIndex, matcher.start()).append(COLOR_CHAR).append('x');
            char[] chars = matcher.group().substring(1).toCharArray();
            for(char c : chars) result.append(COLOR_CHAR).append(c);
            lastIndex = matcher.end();
        }
        result.append(text.substring(lastIndex));
        return result.toString();
    }

    private String replaceWithLanguageCode(String message, String languageCode, Object ... replaceList) {
        message = replaceText(message, replaceList);
        return message.replace(PREFIX_PLACEHOLDER, getMessages(languageCode).getString("Plugin.plugin-prefix", PREFIX_REPLACE));
    }

}