package dev.geco.gsit.manager.mm;

import java.util.*;
import java.util.regex.*;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.*;

import org.bukkit.command.*;

import net.kyori.adventure.text.*;
import net.kyori.adventure.text.minimessage.*;
import net.kyori.adventure.text.serializer.legacy.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.manager.*;

public class MPaperManager extends MManager {

    protected final Pattern PARSED_HEX_PATTERN = Pattern.compile("§x(§[0-9a-fA-F]){6}");
    protected final LegacyComponentSerializer legacyComponentSerializer;
    protected final MiniMessage miniMessage;
    protected final Map<String, String> TAGS;

    public MPaperManager(GSitMain GPluginMain) {
        super(GPluginMain);
        legacyComponentSerializer = LegacyComponentSerializer.builder().character(AMPERSAND_CHAR).hexColors().build();
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

    public String toFormattedMessage(String Text, Object... RawReplaceList) { return org.bukkit.ChatColor.translateAlternateColorCodes(AMPERSAND_CHAR, replaceHexColorsDirect(formatText(Text, RawReplaceList))); }

    public void sendMessage(@NotNull CommandSender Target, String Message, Object... ReplaceList) { Target.sendMessage(getLanguageComponent(Message, getLanguage(Target), ReplaceList)); }

    public void sendActionBarMessage(@NotNull Player Target, String Message, Object... ReplaceList) { Target.sendActionBar(getLanguageComponent(Message, getLanguage(Target), ReplaceList)); }

    private @NotNull Component getLanguageComponent(String Message, String LanguageCode, Object... ReplaceList) { return toFormattedComponent(getRawMessageByLanguage(Message, LanguageCode, ReplaceList)); }

    private @NotNull Component toFormattedComponent(String Text, Object... RawReplaceList) { return miniMessage.deserialize(replaceLegacyColors(replaceText(Text, RawReplaceList))); }

    private String replaceLegacyColors(String Text) {
        Matcher matcher = HEX_PATTERN.matcher(Text);
        StringBuilder result = new StringBuilder(Text.length());
        int lastIndex = 0;
        while(matcher.find()) {
            result.append(Text, lastIndex, matcher.start()).append("<color:").append(matcher.group()).append(">");
            lastIndex = matcher.end();
        }
        result.append(Text, lastIndex, Text.length());
        for(Map.Entry<String, String> tag : TAGS.entrySet()) {
            String key = tag.getKey();
            String value = tag.getValue();
            String upperKey = key.toUpperCase();
            result = new StringBuilder(result.toString().replace(AMPERSAND_CHAR + key, value).replace(AMPERSAND_CHAR + upperKey, value).replace(org.bukkit.ChatColor.COLOR_CHAR + key, value).replace(org.bukkit.ChatColor.COLOR_CHAR + upperKey, value));
        }
        return result.toString();
    }

    private String formatText(String Text, Object... RawReplaceList) { return legacyComponentSerializer.serialize(miniMessage.deserialize(replaceParsedLegacyColors(replaceText(Text, RawReplaceList)))); }

    private String replaceParsedLegacyColors(String Text) {
        Matcher matcher = PARSED_HEX_PATTERN.matcher(Text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) matcher.appendReplacement(result, "#" + matcher.group().replaceAll("§x|§", ""));
        matcher.appendTail(result);
        return result.toString().replace(String.valueOf(org.bukkit.ChatColor.COLOR_CHAR), String.valueOf(AMPERSAND_CHAR));
    }

}