package dev.geco.gsit.manager;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.*;

import org.bukkit.command.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;

import net.md_5.bungee.api.*;

import net.kyori.adventure.audience.*;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.minimessage.*;

import dev.geco.gsit.GSitMain;

public class MManager {

    private final GSitMain GPM;

    private final boolean modern;

    private final HashMap<String, String> TAGS = new HashMap<>(); {

        TAGS.put("&0", "<reset><black>");
        TAGS.put("&1", "<reset><dark_blue>");
        TAGS.put("&2", "<reset><dark_green>");
        TAGS.put("&3", "<reset><dark_aqua>");
        TAGS.put("&4", "<reset><dark_red>");
        TAGS.put("&5", "<reset><dark_purple>");
        TAGS.put("&6", "<reset><gold>");
        TAGS.put("&7", "<reset><gray>");
        TAGS.put("&8", "<reset><dark_gray>");
        TAGS.put("&9", "<reset><blue>");
        TAGS.put("&a", "<reset><green>");
        TAGS.put("&b", "<reset><aqua>");
        TAGS.put("&c", "<reset><red>");
        TAGS.put("&d", "<reset><light_purple>");
        TAGS.put("&e", "<reset><yellow>");
        TAGS.put("&f", "<reset><white>");
        TAGS.put("&k", "<obfuscated>");
        TAGS.put("&l", "<bold>");
        TAGS.put("&m", "<strikethrough>");
        TAGS.put("&n", "<underlined>");
        TAGS.put("&o", "<italic>");
        TAGS.put("&r", "<reset>");
    }

    private final List<String> LANG_FILES = new ArrayList<>(); {

        LANG_FILES.add("de_de");
        LANG_FILES.add("en_en");
        LANG_FILES.add("es_es");
        LANG_FILES.add("fi_fi");
        LANG_FILES.add("fr_fr");
        LANG_FILES.add("it_it");
        LANG_FILES.add("ja_jp");
        LANG_FILES.add("pl_pl");
        LANG_FILES.add("pt_br");
        LANG_FILES.add("ru_ru");
        LANG_FILES.add("uk_ua");
        LANG_FILES.add("zh_cn");
        LANG_FILES.add("zh_tw");
    }

    private final HashMap<String, FileConfiguration> messages = new HashMap<>();

    private final HashMap<UUID, String> languages = new HashMap<>();

    public MManager(GSitMain GPluginMain) {
        GPM = GPluginMain;
        modern = NMSManager.isNewerOrVersion(18, 2);
        loadMessages();
    }

    public FileConfiguration getMessages() { return getMessages(GPM.getCManager().L_LANG); }

    public FileConfiguration getMessages(String LanguageCode) { return messages.getOrDefault(LanguageCode, new YamlConfiguration()); }

    public void loadMessages() {
        messages.clear();
        if(modern) {
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
                        for(String comments : lang.getKeys(true)) {
                            lang.setComments(comments, langSteamConfig.getComments(comments));
                        }
                    }
                    lang.save(langFile);
                    messages.put(langFileName, lang);
                } catch (Exception e) {
                    e.printStackTrace();
                    if(!langFile.exists()) {
                        GPM.saveResource("lang/" + langFileName + ".yml", false);
                    }
                    messages.put(langFileName, YamlConfiguration.loadConfiguration(langFile));
                }
            }
        } else {
            for(String langFileName : LANG_FILES) {
                File langFile = new File(GPM.getDataFolder(), "lang/" + langFileName + ".yml");
                if(!langFile.exists()) {
                    GPM.saveResource("lang/" + langFileName + ".yml", false);
                }
                messages.put(langFileName, YamlConfiguration.loadConfiguration(langFile));
            }
        }
    }

    public String toFormattedMessage(String Text) {
        String colorText = org.bukkit.ChatColor.translateAlternateColorCodes('&', Text);
        Matcher matcher = Pattern.compile("(#[a-fA-F0-9]{6})").matcher(colorText);
        while(matcher.find()) colorText = colorText.replaceFirst(matcher.group(), ChatColor.of(matcher.group()).toString());
        return colorText.replace("<lang:key.sneak>", "Sneak");
    }

    public Object toFormattedComponent(String Text) {
        String text = Text;
        for(Map.Entry<String, String> tag : TAGS.entrySet()) text = text.replace(tag.getKey(), tag.getValue()).replace(tag.getKey().toUpperCase(), tag.getValue());
        Matcher matcher = Pattern.compile("(#[a-fA-F0-9]{6})").matcher(text);
        while(matcher.find()) if(text.indexOf(matcher.group()) == 0 || text.charAt(text.indexOf(matcher.group()) - 1) != ':') text = text.replaceFirst(matcher.group(), "<reset><color:" + matcher.group() + ">");
        try { return MiniMessage.miniMessage().deserialize(text); } catch (Exception e) { return Component.text(toFormattedMessage(Text)); }
    }

    public void sendMessage(CommandSender Target, String Message, Object... ReplaceList) {
        if(GPM.SERVER > 1 && modern) {
            ((Audience) Target).sendMessage((Component) getLanguageComponent(Message, getLanguage(Target), ReplaceList));
        } else Target.sendMessage(getLanguageMessage(Message, getLanguage(Target), ReplaceList));
    }

    public void sendActionBarMessage(Player Target, String Message, Object... ReplaceList) {
        if(GPM.SERVER > 1 && modern) {
            ((Audience) Target).sendActionBar((Component) getLanguageComponent(Message, getLanguage(Target), ReplaceList));
        } else Target.spigot().sendMessage(ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(getLanguageMessage(Message, getLanguage(Target), ReplaceList)));
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
        String message = Message;
        if(ReplaceList.length > 0 && ReplaceList.length % 2 == 0) for(int count = 0; count < ReplaceList.length; count += 2) if(ReplaceList[count] != null && ReplaceList[count + 1] != null) message = message.replace(ReplaceList[count].toString(), ReplaceList[count + 1].toString());
        return message.replace("[P]", getMessages(LanguageCode).getString("Plugin.plugin-prefix", "&7[&6" + GPM.NAME + "&7]"));
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