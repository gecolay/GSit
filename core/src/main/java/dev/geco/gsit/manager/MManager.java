package dev.geco.gsit.manager;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.*;

import org.bukkit.command.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;

import net.md_5.bungee.api.*;

import net.kyori.adventure.text.*;
import net.kyori.adventure.text.minimessage.*;

import dev.geco.gsit.GSitMain;

public class MManager {

    private final GSitMain GPM;

    private boolean allowBungeeMessages = true;
    private boolean allowComponentMessages = NMSManager.isNewerOrVersion(18, 2);

    private final char PRE_FORMAT_COLOR_CHAR = '&';

    private final HashMap<String, String> COLOR_TAGS = new HashMap<>(); {

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

    private final HashMap<String, String> TAGS = new HashMap<>(); {

        TAGS.putAll(COLOR_TAGS);
        TAGS.put("k", "<obfuscated>");
        TAGS.put("l", "<bold>");
        TAGS.put("m", "<strikethrough>");
        TAGS.put("n", "<underlined>");
        TAGS.put("o", "<italic>");
        TAGS.put("r", "<reset>");
    }

    private final List<String> RESET_ON_TAGS = new ArrayList<>(); {

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

    private final List<String> LEVEL_TAGS = new ArrayList<>(); {

        LEVEL_TAGS.add("hover");
        LEVEL_TAGS.add("click");
    }

    private final List<String> LANG_FILES = new ArrayList<>(); {

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

    private final HashMap<String, FileConfiguration> messages = new HashMap<>();
    private final HashMap<UUID, String> languages = new HashMap<>();

    public MManager(GSitMain GPluginMain) {
        GPM = GPluginMain;
        try { Class.forName("net.md_5.bungee.api.ChatMessageType"); } catch (Throwable e) { allowBungeeMessages = false; }
        try {
            Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            Class.forName("net.kyori.adventure.text.Component");
            Class.forName("net.kyori.adventure.audience.Audience");
        } catch (Throwable e) { allowComponentMessages = false; }
        loadMessages();
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

    public String toFormattedMessage(String Text) {
        String colorText = org.bukkit.ChatColor.translateAlternateColorCodes(PRE_FORMAT_COLOR_CHAR, Text);
        try {
            Matcher matcher = Pattern.compile("(#[a-fA-F0-9]{6})").matcher(colorText);
            while(matcher.find()) colorText = colorText.replaceFirst(matcher.group(), ChatColor.of(matcher.group()).toString());
        } catch (Throwable ignored) { }
        return colorText.replace("<lang:key.sneak>", "Sneak");
    }

    public Object toFormattedComponent(String Text) {
        String text = Text;
        if(!allowComponentMessages) return text;
        for(Map.Entry<String, String> tag : TAGS.entrySet()) text = text.replace(PRE_FORMAT_COLOR_CHAR + tag.getKey(), tag.getValue()).replace(PRE_FORMAT_COLOR_CHAR + tag.getKey().toUpperCase(), tag.getValue()).replace(org.bukkit.ChatColor.COLOR_CHAR + tag.getKey(), tag.getValue()).replace(org.bukkit.ChatColor.COLOR_CHAR + tag.getKey().toUpperCase(), tag.getValue());
        text = text.replaceAll("(?<!<color:)#[a-fA-F0-9]{6}(?<!>)", "<color:$0>");
        text = fixMiniMessageFormat(text);
        try { return MiniMessage.miniMessage().deserialize(text); } catch (Throwable e) { return Component.text(toFormattedMessage(Text)); }
    }

    private String fixMiniMessageFormat(String Text) {
        HashMap<Integer, List<String>> reset_map = new HashMap<>();
        TreeMap<Integer, String> insert_map = new TreeMap<>();
        int pos = 0;
        int level = 0;
        for(char ch : Text.toCharArray()) {
            if(ch == '<') {
                int end = Text.indexOf(">", pos);
                if(end == -1) continue;
                String tag = Text.substring(pos + 1, end);
                if(RESET_ON_TAGS.contains(tag)) {
                    List<String> reset_list = reset_map.getOrDefault(level, new ArrayList<>());
                    reset_list.add(tag);
                    reset_map.put(level, reset_list);
                }
                else if(RESET_ON_TAGS.contains("/" + tag.substring(1))) {
                    List<String> reset_list = reset_map.getOrDefault(level, new ArrayList<>());
                    reset_list.remove(tag);
                    reset_map.put(level, reset_list);
                }
                else {
                    if(LEVEL_TAGS.stream().anyMatch(tag::startsWith)) {
                        level++;
                    } else if(LEVEL_TAGS.contains(tag.substring(1))) {
                        level--;
                    }
                    if(COLOR_TAGS.containsValue("<" + tag.toLowerCase() + ">") || tag.startsWith("color")) {
                        List<String> reset_list = reset_map.getOrDefault(level, new ArrayList<>());
                        if(reset_list.size() > 0) {
                            StringBuilder resetTags = new StringBuilder();
                            for(String a : reset_list) resetTags.append("</").append(a).append(">");
                            insert_map.put(pos, resetTags.toString());
                            reset_list.clear();
                            reset_map.put(level, reset_list);
                        }
                    }
                }
            }
            pos++;
        }
        return insertStringValues(Text, insert_map);
    }

    private String insertStringValues(String Text, TreeMap<Integer, String> InsertMap) {
        StringBuilder text = new StringBuilder(Text);
        int offset = 0;
        for (Map.Entry<Integer, String> entry : InsertMap.entrySet()) {
            int pos = entry.getKey();
            if(pos >= 0 && pos + offset < text.length()) text.insert(pos + offset, entry.getValue());
            offset += entry.getValue().length();
        }
        return text.toString();
    }

    public void sendMessage(CommandSender Target, String Message, Object... ReplaceList) {
        try {
            if(allowComponentMessages) {
                Target.sendMessage((Component) getLanguageComponent(Message, getLanguage(Target), ReplaceList));
                return;
            }
        } catch (Throwable ignored) { }
        Target.sendMessage(getLanguageMessage(Message, getLanguage(Target), ReplaceList));
    }

    public void sendActionBarMessage(Player Target, String Message, Object... ReplaceList) {
        try {
            if(allowComponentMessages) {
                Target.sendActionBar((Component) getLanguageComponent(Message, getLanguage(Target), ReplaceList));
                return;
            }
        } catch (Throwable ignored) { }
        if(allowBungeeMessages) Target.spigot().sendMessage(ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(getLanguageMessage(Message, getLanguage(Target), ReplaceList)));
    }

    public String getMessage(String Message, Object... ReplaceList) { return getLanguageMessage(Message, GPM.getCManager().L_LANG, ReplaceList); }

    public String getLanguageMessage(String Message, CommandSender Target, Object... ReplaceList) { return getLanguageMessage(Message, getLanguage(Target), ReplaceList); }

    public String getLanguageMessage(String Message, String LanguageCode, Object... ReplaceList) { return toFormattedMessage(getRawLanguageMessage(Message, LanguageCode, ReplaceList)); }

    public Object getComponent(String Message, Object... ReplaceList) { return getLanguageComponent(Message, GPM.getCManager().L_LANG, ReplaceList); }

    public Object getLanguageComponent(String Message, CommandSender Target, Object... ReplaceList) { return getLanguageComponent(Message, getLanguage(Target), ReplaceList); }

    public Object getLanguageComponent(String Message, String LanguageCode, Object... ReplaceList) { return toFormattedComponent(getRawLanguageMessage(Message, LanguageCode, ReplaceList)); }

    public String getRawMessage(String Message, Object... ReplaceList) { return getRawLanguageMessage(Message, GPM.getCManager().L_LANG, ReplaceList); }

    public String getRawLanguageMessage(String Message, CommandSender Target, Object... ReplaceList) { return getRawLanguageMessage(Message, getLanguage(Target), ReplaceList); }

    public String getRawLanguageMessage(String Message, String LanguageCode, Object... ReplaceList) { return replace(Message == null || Message.isEmpty() ? "" : getMessages(LanguageCode).getString(Message, Message), LanguageCode, ReplaceList); }

    private String replace(String Message, String LanguageCode, Object... ReplaceList) {
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