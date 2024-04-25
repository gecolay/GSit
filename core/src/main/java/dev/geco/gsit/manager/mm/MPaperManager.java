package dev.geco.gsit.manager.mm;

import java.util.*;
import java.util.regex.*;

import org.jetbrains.annotations.*;

import org.bukkit.command.*;
import org.bukkit.entity.*;

import net.kyori.adventure.text.*;
import net.kyori.adventure.text.minimessage.*;
import net.kyori.adventure.text.serializer.json.*;
import net.kyori.adventure.text.serializer.legacy.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.manager.*;

public class MPaperManager extends MManager {

    protected final LegacyComponentSerializer legacyComponentSerializer;
    protected Object jsonComponentSerializer;
    protected final MiniMessage miniMessage;
    protected final Map<String, String> TAGS;

    public MPaperManager(GSitMain GPluginMain) {
        super(GPluginMain);
        legacyComponentSerializer = LegacyComponentSerializer.builder().character(AMPERSAND_CHAR).hexColors().build();
        if(GPluginMain.getSVManager().isNewerOrVersion(20, 0)) jsonComponentSerializer = JSONComponentSerializer.json();
        miniMessage = MiniMessage.miniMessage();
        Map<String, String> tags = new HashMap<>();
        tags.put("0", "<black>");
        tags.put("1", "<dark_blue>");
        tags.put("2", "<dark_green>");
        tags.put("3", "<dark_aqua>");
        tags.put("4", "<dark_red>");
        tags.put("5", "<dark_purple>");
        tags.put("6", "<gold>");
        tags.put("7", "<gray>");
        tags.put("8", "<dark_gray>");
        tags.put("9", "<blue>");
        tags.put("a", "<green>");
        tags.put("b", "<aqua>");
        tags.put("c", "<red>");
        tags.put("d", "<light_purple>");
        tags.put("e", "<yellow>");
        tags.put("f", "<white>");
        tags.put("k", "<obfuscated>");
        tags.put("l", "<bold>");
        tags.put("m", "<strikethrough>");
        tags.put("n", "<underlined>");
        tags.put("o", "<italic>");
        tags.put("r", "<reset>");
        TAGS = Collections.unmodifiableMap(tags);
    }

    public String getAsJSON(String Text, Object... RawReplaceList) { return jsonComponentSerializer != null ? ((JSONComponentSerializer) jsonComponentSerializer).serialize(toFormattedComponent(Text, RawReplaceList)) : super.getAsJSON(Text, RawReplaceList); }

    public String toFormattedMessage(String Text, Object... RawReplaceList) { return org.bukkit.ChatColor.translateAlternateColorCodes(AMPERSAND_CHAR, replaceHexColorsDirect(formatText(Text, RawReplaceList))); }

    public void sendMessage(@NotNull CommandSender Target, String Message, Object... ReplaceList) { Target.sendMessage(getLanguageComponent(Message, getLanguage(Target), ReplaceList)); }

    public void sendActionBarMessage(@NotNull Player Target, String Message, Object... ReplaceList) { Target.sendActionBar(getLanguageComponent(Message, getLanguage(Target), ReplaceList)); }

    private @NotNull Component getLanguageComponent(String Message, String LanguageCode, Object... ReplaceList) { return toFormattedComponent(getRawMessageByLanguage(Message, LanguageCode, ReplaceList)); }

    private @NotNull Component toFormattedComponent(String Text, Object... RawReplaceList) { return miniMessage.deserialize(replaceLegacyColors(replaceText(Text, RawReplaceList))); }

    private String formatText(String Text, Object... RawReplaceList) { return legacyComponentSerializer.serialize( miniMessage.deserialize(replaceText(Text, RawReplaceList))); }

    private String replaceLegacyColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder(text.length());
        int lastIndex = 0;
        while(matcher.find()) {
            result.append(text, lastIndex, matcher.start()).append("<color:").append(matcher.group()).append(">");
            lastIndex = matcher.end();
        }
        result.append(text, lastIndex, text.length());
        for(Map.Entry<String, String> tag : TAGS.entrySet()) {
            String key = tag.getKey();
            String value = tag.getValue();
            String upperKey = key.toUpperCase();
            result = new StringBuilder(result.toString().replace(AMPERSAND_CHAR + key, value).replace(AMPERSAND_CHAR + upperKey, value).replace(org.bukkit.ChatColor.COLOR_CHAR + key, value).replace(org.bukkit.ChatColor.COLOR_CHAR + upperKey, value));
        }
        return result.toString();
    }

}