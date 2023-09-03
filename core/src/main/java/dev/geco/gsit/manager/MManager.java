package dev.geco.gsit.manager;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.*;

import org.jetbrains.annotations.*;

import org.bukkit.command.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;

import net.md_5.bungee.api.*;

import dev.geco.gsit.GSitMain;

abstract public class MManager {

    protected final GSitMain GPM;

    protected boolean allowBungeeMessages = true;

    protected final HashMap<String, FileConfiguration> messages = new HashMap<>();
    protected final HashMap<UUID, String> languages = new HashMap<>();

    protected final char PRE_FORMAT_COLOR_CHAR = '&';

    protected final HashMap<String, String> COLOR_TAGS = new HashMap<>(); {

        COLOR_TAGS.put("0", "<black>");
        COLOR_TAGS.put("1", "<dark_blue>");
        COLOR_TAGS.put("2", "<dark_green>");
        COLOR_TAGS.put("3", "<dark_aqua>");
        COLOR_TAGS.put("4", "<dark_red>");
        COLOR_TAGS.put("5", "<dark_purple>");
        COLOR_TAGS.put("6", "<gold>");
        COLOR_TAGS.put("7", "<gray>");
        COLOR_TAGS.put("8", "<dark_gray>");
        COLOR_TAGS.put("9", "<blue>");
        COLOR_TAGS.put("a", "<green>");
        COLOR_TAGS.put("b", "<aqua>");
        COLOR_TAGS.put("c", "<red>");
        COLOR_TAGS.put("d", "<light_purple>");
        COLOR_TAGS.put("e", "<yellow>");
        COLOR_TAGS.put("f", "<white>");
    }

    protected final HashMap<String, String> TAGS = new HashMap<>(); {

        TAGS.putAll(COLOR_TAGS);
        TAGS.put("k", "<obfuscated>");
        TAGS.put("l", "<bold>");
        TAGS.put("m", "<strikethrough>");
        TAGS.put("n", "<underlined>");
        TAGS.put("o", "<italic>");
        TAGS.put("r", "<reset>");
    }

    protected final List<String> RESET_ON_TAGS = new ArrayList<>(); {

        RESET_ON_TAGS.add("bold");
        RESET_ON_TAGS.add("b");
        RESET_ON_TAGS.add("italic");
        RESET_ON_TAGS.add("em");
        RESET_ON_TAGS.add("i");
        RESET_ON_TAGS.add("underlined");
        RESET_ON_TAGS.add("u");
        RESET_ON_TAGS.add("strikethrough");
        RESET_ON_TAGS.add("st");
        RESET_ON_TAGS.add("obfuscated");
        RESET_ON_TAGS.add("obf");
    }

    protected final List<String> LEVEL_TAGS = new ArrayList<>(); {

        LEVEL_TAGS.add("hover");
        LEVEL_TAGS.add("click");
    }

    protected final List<String> LANG_FILES = new ArrayList<>(); {

        LANG_FILES.add("cs_cz");
        LANG_FILES.add("de_de");
        LANG_FILES.add("en_en");
        LANG_FILES.add("es_es");
        LANG_FILES.add("fi_fi");
        LANG_FILES.add("fr_fr");
        LANG_FILES.add("id_id");
        LANG_FILES.add("it_it");
        LANG_FILES.add("ja_jp");
        LANG_FILES.add("pl_pl");
        LANG_FILES.add("pt_br");
        LANG_FILES.add("ru_ru");
        LANG_FILES.add("sk_sk");
        LANG_FILES.add("uk_ua");
        LANG_FILES.add("zh_cn");
        LANG_FILES.add("zh_tw");
    }

    public MManager(GSitMain GPluginMain) {
        GPM = GPluginMain;
        try { Class.forName("net.md_5.bungee.api.ChatMessageType"); } catch (Throwable e) { allowBungeeMessages = false; }
    }

    public FileConfiguration getMessages() { return getMessages(GPM.getCManager().L_LANG); }

    public FileConfiguration getMessages(String LanguageCode) { return messages.getOrDefault(LanguageCode, new YamlConfiguration()); }

    public void loadMessages() {
        messages.clear();
        if(NMSManager.isNewerOrVersion(18, 2)) {
            for(String langFileName : LANG_FILES) {
                File langFile = new File(GPM.getDataFolder(), "lang/" + langFileName + ".yml");
                try {
                    FileConfiguration lang = YamlConfiguration.loadConfiguration(langFile);
                    InputStream langSteam = GPM.getResource("lang/" + langFileName + ".yml");
                    if(langSteam != null) {
                        FileConfiguration langSteamConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(langSteam, StandardCharsets.UTF_8));
                        lang.setDefaults(langSteamConfig);
                        YamlConfigurationOptions options = (YamlConfigurationOptions) lang.options();
                        options.parseComments(true).copyDefaults(true).width(500);
                        lang.loadFromString(lang.saveToString());
                        for(String comments : lang.getKeys(true)) lang.setComments(comments, langSteamConfig.getComments(comments));
                    }
                    lang.save(langFile);
                    messages.put(langFileName, lang);
                } catch (Throwable e) {
                    e.printStackTrace();
                    if(!langFile.exists()) GPM.saveResource("lang/" + langFileName + ".yml", false);
                    messages.put(langFileName, YamlConfiguration.loadConfiguration(langFile));
                }
            }
        } else {
            for(String langFileName : LANG_FILES) {
                File langFile = new File(GPM.getDataFolder(), "lang/" + langFileName + ".yml");
                if(!langFile.exists()) GPM.saveResource("lang/" + langFileName + ".yml", false);
                messages.put(langFileName, YamlConfiguration.loadConfiguration(langFile));
            }
        }
    }

    public String toFormattedMessage(String Text, Object... RawReplaceList) {
        String text = org.bukkit.ChatColor.translateAlternateColorCodes(PRE_FORMAT_COLOR_CHAR, Text);
        try {
            Matcher matcher = Pattern.compile("(#[a-fA-F0-9]{6})").matcher(text);
            while(matcher.find()) text = text.replaceFirst(matcher.group(), ChatColor.of(matcher.group()).toString());
        } catch (Throwable ignored) { }
        text = text.replace("<lang:key.sneak>", "Sneak");
        if(RawReplaceList.length > 0 && RawReplaceList.length % 2 == 0) for(int count = 0; count < RawReplaceList.length; count += 2) if(RawReplaceList[count] != null && RawReplaceList[count + 1] != null) text = text.replace(RawReplaceList[count].toString(), RawReplaceList[count + 1].toString());
        return text;
    }

    abstract public void sendMessage(CommandSender Target, String Message, Object... ReplaceList);

    abstract public void sendActionBarMessage(Player Target, String Message, Object... ReplaceList);

    public String getMessage(String Message, Object... ReplaceList) { return getLanguageMessage(Message, GPM.getCManager().L_LANG, ReplaceList); }

    public String getLanguageMessage(String Message, CommandSender Target, Object... ReplaceList) { return getLanguageMessage(Message, getLanguage(Target), ReplaceList); }

    public String getLanguageMessage(String Message, String LanguageCode, Object... ReplaceList) { return toFormattedMessage(getRawLanguageMessage(Message, LanguageCode, ReplaceList)); }

    public String getRawMessage(String Message, Object... ReplaceList) { return getRawLanguageMessage(Message, GPM.getCManager().L_LANG, ReplaceList); }

    public String getRawLanguageMessage(String Message, CommandSender Target, Object... ReplaceList) { return getRawLanguageMessage(Message, getLanguage(Target), ReplaceList); }

    public String getRawLanguageMessage(String Message, String LanguageCode, Object... ReplaceList) { return replace(Message == null || Message.isEmpty() ? "" : getMessages(LanguageCode).getString(Message, Message), LanguageCode, ReplaceList); }

    private @NotNull String replace(String Message, String LanguageCode, Object @NotNull ... ReplaceList) {
        if(ReplaceList.length > 0 && ReplaceList.length % 2 == 0) for(int count = 0; count < ReplaceList.length; count += 2) if(ReplaceList[count] != null && ReplaceList[count + 1] != null) Message = Message.replace(ReplaceList[count].toString(), ReplaceList[count + 1].toString());
        return Message.replace("[P]", getMessages(LanguageCode).getString("Plugin.plugin-prefix", "&7[&6" + GPM.NAME + "&7]"));
    }

    public String getLanguage(CommandSender Target) {
        if(!(Target instanceof Entity)) return GPM.getCManager().L_LANG;
        return languages.getOrDefault(((Entity) Target).getUniqueId(), GPM.getCManager().L_LANG);
    }

    public void setLanguage(CommandSender Target, String LanguageCode) {
        if(!(Target instanceof Entity)) return;
        languages.put(((Entity) Target).getUniqueId(), LanguageCode);
    }

}