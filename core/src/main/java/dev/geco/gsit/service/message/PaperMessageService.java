package dev.geco.gsit.service.message;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.service.MessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaperMessageService extends MessageService {

    public final Pattern PARSED_HEX_PATTERN = Pattern.compile("§x(§[0-9a-fA-F]){6}");

    protected final LegacyComponentSerializer legacyComponentSerializer;
    protected final MiniMessage miniMessage;
    protected final Map<String, String> formatTags = new HashMap<>(); {
        formatTags.put("0", "<black>");
        formatTags.put("1", "<dark_blue>");
        formatTags.put("2", "<dark_green>");
        formatTags.put("3", "<dark_aqua>");
        formatTags.put("4", "<dark_red>");
        formatTags.put("5", "<dark_purple>");
        formatTags.put("6", "<gold>");
        formatTags.put("7", "<gray>");
        formatTags.put("8", "<dark_gray>");
        formatTags.put("9", "<blue>");
        formatTags.put("a", "<green>");
        formatTags.put("b", "<aqua>");
        formatTags.put("c", "<red>");
        formatTags.put("d", "<light_purple>");
        formatTags.put("e", "<yellow>");
        formatTags.put("f", "<white>");
        formatTags.put("k", "<obfuscated>");
        formatTags.put("l", "<bold>");
        formatTags.put("m", "<strikethrough>");
        formatTags.put("n", "<underlined>");
        formatTags.put("o", "<italic>");
        formatTags.put("r", "<reset>");
    };

    public PaperMessageService(GSitMain gSitMain) {
        super(gSitMain);
        legacyComponentSerializer = LegacyComponentSerializer.builder().character(AMPERSAND_CHAR).hexColors().build();
        miniMessage = MiniMessage.miniMessage();
    }

    public String toFormattedMessage(String text, Object... rawReplaceList) { return org.bukkit.ChatColor.translateAlternateColorCodes(AMPERSAND_CHAR, replaceHexColorsDirectly(formatText(text, rawReplaceList))); }

    public void sendMessage(@NotNull CommandSender target, String message, Object... replaceList) {
        Component translatedMessage = getTranslatedComponent(message, getLanguageForTarget(target), replaceList);
        if(translatedMessage.equals(Component.empty())) return;
        target.sendMessage(translatedMessage);
    }

    public void sendActionBarMessage(@NotNull Player target, String message, Object... replaceList) {
        Component translatedMessage = getTranslatedComponent(message, getLanguageForTarget(target), replaceList);
        if(translatedMessage.equals(Component.empty())) return;
        target.sendActionBar(translatedMessage);
    }

    private @NotNull Component getTranslatedComponent(String message, String languageCode, Object... replaceList) { return toFormattedComponent(getRawTranslatedMessage(message, languageCode, replaceList)); }

    private @NotNull Component toFormattedComponent(String text, Object... rawReplaceList) { return miniMessage.deserialize(replaceLegacyColors(replaceText(text, rawReplaceList))); }

    private String replaceLegacyColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder(text.length());
        int lastIndex = 0;
        while(matcher.find()) {
            result.append(text, lastIndex, matcher.start()).append("<color:").append(matcher.group()).append(">");
            lastIndex = matcher.end();
        }
        result.append(text, lastIndex, text.length());
        for(Map.Entry<String, String> tag : formatTags.entrySet()) {
            String key = tag.getKey();
            String value = tag.getValue();
            String upperKey = key.toUpperCase();
            result = new StringBuilder(result.toString().replace(AMPERSAND_CHAR + key, value).replace(AMPERSAND_CHAR + upperKey, value).replace(COLOR_CHAR + key, value).replace(COLOR_CHAR + upperKey, value));
        }
        return result.toString();
    }

    private String formatText(String text, Object... rawReplaceList) { return legacyComponentSerializer.serialize(miniMessage.deserialize(replaceParsedLegacyColors(replaceText(text, rawReplaceList)))); }

    private String replaceParsedLegacyColors(String text) {
        if(text.indexOf(COLOR_CHAR) == -1) return text;
        Matcher matcher = PARSED_HEX_PATTERN.matcher(text);
        int lastMatchEnd = 0;
        StringBuilder result = new StringBuilder(text.length());
        while(matcher.find()) {
            result.append(text, lastMatchEnd, matcher.start());
            String hex = text.substring(matcher.start() + 3, matcher.end()).replace("§", "");
            result.append('#').append(hex);
            lastMatchEnd = matcher.end();
        }
        result.append(text, lastMatchEnd, text.length());
        int length = result.length();
        for(int i = 0; i < length; i++) if(result.charAt(i) == COLOR_CHAR) result.setCharAt(i, AMPERSAND_CHAR);
        return result.toString();
    }

}