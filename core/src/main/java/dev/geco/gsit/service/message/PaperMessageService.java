package dev.geco.gsit.service.message;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.service.MessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaperMessageService extends MessageService {

    protected final Pattern PARSED_HEX_PATTERN = Pattern.compile("§x(§[0-9a-fA-F]){6}");
    protected final String RESET_TAG = "<reset>";
    protected final String COLOR_CODES = "0123456789abcdef";
    protected final String[] COLOR_TAGS = {"<black>", "<dark_blue>", "<dark_green>", "<dark_aqua>", "<dark_red>", "<dark_purple>", "<gold>", "<gray>", "<dark_gray>", "<blue>", "<green>", "<aqua>", "<red>", "<light_purple>", "<yellow>", "<white>"};
    protected final String FORMAT_CODES = "klmno";
    protected final String[] FORMAT_TAGS = {"<obfuscated>", "<bold>", "<strikethrough>", "<underlined>", "<italic>"};
    protected final LegacyComponentSerializer legacyComponentSerializer;
    protected final MiniMessage miniMessage;

    public PaperMessageService(GSitMain gSitMain) {
        super(gSitMain);
        legacyComponentSerializer = LegacyComponentSerializer.builder().character(AMPERSAND_CHAR).hexColors().build();
        miniMessage = MiniMessage.miniMessage();
    }

    @Override
    protected String formatReplaceValue(String value) { return miniMessage.escapeTags(value); }

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

    private String replaceLegacyColors(String text) {
        StringBuilder result = new StringBuilder(text.length() + 16);
        boolean formatted = false;
        for(int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);
            if(current == '<') {
                int tagEnd = findTagEnd(text, index);
                if(tagEnd != -1) {
                    result.append(text, index, tagEnd + 1);
                    index = tagEnd;
                    continue;
                }
            } else if(current == '#' || current == '&' || current == COLOR_CHAR) {
                char next = index + 1 < text.length() ? Character.toLowerCase(text.charAt(index + 1)) : 0;
                String hex = null;
                int length = 0;
                if(current == '#') { hex = hexAt(text, index + 1); length = 7; }
                else if(next == '#') { hex = hexAt(text, index + 2); length = 8; }
                else if(next == 'x') { hex = spacedHexAt(text, index + 2, current); length = 14; }
                if(hex != null) {
                    if(formatted) result.append(RESET_TAG);
                    result.append("<color:#").append(hex.toLowerCase()).append('>');
                    formatted = false;
                    index += length - 1;
                    continue;
                }
                int color = current == '#' ? -1 : COLOR_CODES.indexOf(next);
                int format = current == '#' ? -1 : FORMAT_CODES.indexOf(next);
                if(color != -1 || format != -1 || (next == 'r' && current != '#')) {
                    if(color != -1 && formatted) result.append(RESET_TAG);
                    result.append(color != -1 ? COLOR_TAGS[color] : format != -1 ? FORMAT_TAGS[format] : RESET_TAG);
                    formatted = format != -1;
                    index++;
                    continue;
                }
            }
            result.append(current);
        }
        return result.toString();
    }

    private int findTagEnd(String text, int start) {
        char quote = 0;
        for(int index = start + 1; index < text.length(); index++) {
            char current = text.charAt(index);
            if(quote != 0) {
                if(current == '\\') index++;
                else if(current == quote) quote = 0;
            } else if(current == '\'' || current == '"') quote = current;
            else if(current == '>') return index;
            else if(current == '<') return -1;
        }
        return -1;
    }

    private String hexAt(String text, int start) {
        if(start + 6 > text.length()) return null;
        for(int index = start; index < start + 6; index++) if(Character.digit(text.charAt(index), 16) == -1) return null;
        return text.substring(start, start + 6);
    }

    private String spacedHexAt(String text, int start, char prefix) {
        if(start + 12 > text.length()) return null;
        StringBuilder hex = new StringBuilder(6);
        for(int index = start; index < start + 12; index += 2) {
            if(text.charAt(index) != prefix || Character.digit(text.charAt(index + 1), 16) == -1) return null;
            hex.append(text.charAt(index + 1));
        }
        return hex.toString();
    }

}
