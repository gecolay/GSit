package dev.geco.gsit.manager;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import net.md_5.bungee.api.*;

import net.kyori.adventure.audience.*;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.minimessage.*;

import org.bukkit.command.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.values.*;

public class MManager {

    private final GSitMain GPM;

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

    private FileConfiguration messages;

    public MManager(GSitMain GPluginMain) {
        GPM = GPluginMain;
        loadMessages();
    }

    public FileConfiguration getMessages() { return messages; }

    public void loadMessages() {
        for(String lang : LANG_FILES) {
            File langFile = new File("plugins/" + GPM.NAME + "/" + PluginValues.LANG_PATH + "/" + lang + PluginValues.YML_FILETYP);
            if(!langFile.exists()) {
                GPM.saveResource(PluginValues.LANG_PATH + "/" + lang + PluginValues.YML_FILETYP, false);
            }
        }
        messages = YamlConfiguration.loadConfiguration(new File("plugins/" + GPM.NAME + "/" + PluginValues.LANG_PATH, GPM.getConfig().getString("Lang.lang", "en_en") + PluginValues.YML_FILETYP));
    }

    public String toFormattedMessage(String Text) {

        String colorText = org.bukkit.ChatColor.translateAlternateColorCodes('&', Text);

        Matcher matcher = Pattern.compile("(#[\\da-fA-F]{6})").matcher(colorText);

        while(matcher.find()) colorText = colorText.replaceFirst(matcher.group(), ChatColor.of(matcher.group()).toString());

        return colorText.replace("<lang:key.sneak>", "Sneak");
    }

    public Object toFormattedComponent(String Text) {

        String text = Text;

        for(Map.Entry<String, String> tag : TAGS.entrySet()) text = text.replace(tag.getKey(), tag.getValue());

        Matcher matcher = Pattern.compile("(#[\\da-fA-F]{6})").matcher(text);

        while(matcher.find()) if(text.indexOf(matcher.group()) == 0 || text.charAt(text.indexOf(matcher.group()) - 1) != ':') text = text.replaceFirst(matcher.group(), "<reset><color:" + matcher.group() + ">");

        try { return MiniMessage.miniMessage().deserialize(text); } catch (Exception e) { return Component.text(toFormattedMessage(Text)); }
    }

    public void sendMessage(CommandSender Sender, String Message, Object... ReplaceList) {

        if(GPM.SERVER > 1 && NMSManager.isNewerOrVersion(18, 2)) {

            ((Audience) Sender).sendMessage((Component) getComponent(Message, ReplaceList));
        } else Sender.sendMessage(getMessage(Message, ReplaceList));
    }

    public void sendActionBarMessage(Player Player, String Message, Object... ReplaceList) {

        if(GPM.SERVER > 1 && NMSManager.isNewerOrVersion(18, 2)) {

            ((Audience) Player).sendActionBar((Component) getComponent(Message, ReplaceList));
        } else Player.spigot().sendMessage(ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(getMessage(Message, ReplaceList)));
    }

    public String getMessage(String Message, Object... ReplaceList) { return toFormattedMessage(getRawMessage(Message, ReplaceList)); }

    public Object getComponent(String Message, Object... ReplaceList) { return toFormattedComponent(getRawMessage(Message, ReplaceList)); }

    public String getRawMessage(String Message, Object... ReplaceList) { return replace(Message == null || Message.isEmpty() ? "" : messages.getString(Message, Message), ReplaceList); }

    private String replace(String Message, Object... ReplaceList) {

        String message = Message;

        if(ReplaceList != null && ReplaceList.length > 1) for(int count = 0; count < ReplaceList.length; count += 2) if(ReplaceList[count] != null && ReplaceList[count + 1] != null) message = message.replace(ReplaceList[count].toString(), ReplaceList[count + 1].toString());

        return message.replace("[P]", GPM.getPrefix());
    }

}