package dev.geco.gsit.service.message;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.service.MessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaperMessageService extends MessageService {

    protected final Pattern PARSED_HEX_PATTERN = Pattern.compile("§x(§[0-9a-fA-F]){6}");
    protected final LegacyComponentSerializer legacyComponentSerializer;
    protected final MiniMessage miniMessage;
    protected final Map<String, String> tags;

    public PaperMessageService(GSitMain gSitMain) {
        super(gSitMain);
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
        this.tags = Collections.unmodifiableMap(tags);
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
        for(Map.Entry<String, String> tag : tags.entrySet()) {
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