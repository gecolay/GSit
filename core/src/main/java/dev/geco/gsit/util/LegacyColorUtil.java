package dev.geco.gsit.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class LegacyColorUtil {

    private final char AMPERSAND_CHAR = '&';
    private final char COLOR_CHAR = '§';
    private final Map<Character, String> colorTags = new HashMap<>();
    private final Map<Character, String> formatTags = new HashMap<>();

    public LegacyColorUtil() {
        colorTags.put('0', "black");
        colorTags.put('1', "dark_blue");
        colorTags.put('2', "dark_green");
        colorTags.put('3', "dark_aqua");
        colorTags.put('4', "dark_red");
        colorTags.put('5', "dark_purple");
        colorTags.put('6', "gold");
        colorTags.put('7', "gray");
        colorTags.put('8', "dark_gray");
        colorTags.put('9', "blue");
        colorTags.put('a', "green");
        colorTags.put('b', "aqua");
        colorTags.put('c', "red");
        colorTags.put('d', "light_purple");
        colorTags.put('e', "yellow");
        colorTags.put('f', "white");
        formatTags.put('k', "obfuscated");
        formatTags.put('l', "bold");
        formatTags.put('m', "strikethrough");
        formatTags.put('n', "underlined");
        formatTags.put('o', "italic");
    }

    public String toMiniMessage(String text) {
        if(text == null || (text.indexOf(AMPERSAND_CHAR) == -1 && text.indexOf(COLOR_CHAR) == -1)) return text;
        StringBuilder result = new StringBuilder(text.length());
        Deque<String> openTags = new ArrayDeque<>();
        int length = text.length();
        for(int i = 0; i < length; i++) {
            char current = text.charAt(i);
            if(!isCodeChar(current) || i + 1 >= length) {
                result.append(current);
                continue;
            }
            char code = Character.toLowerCase(text.charAt(i + 1));
            String hexColor = getHexColor(text, i);
            if(hexColor != null) {
                closeTags(result, openTags);
                openTag(result, openTags, hexColor);
                i += code == '#' ? 7 : 13;
            } else if(colorTags.containsKey(code)) {
                closeTags(result, openTags);
                openTag(result, openTags, colorTags.get(code));
                i++;
            } else if(formatTags.containsKey(code)) {
                openTag(result, openTags, formatTags.get(code));
                i++;
            } else if(code == 'r') {
                closeTags(result, openTags);
                i++;
            } else result.append(current);
        }
        return result.toString();
    }

    private boolean isCodeChar(char c) { return c == AMPERSAND_CHAR || c == COLOR_CHAR; }

    private boolean isHexChar(char c) { return Character.digit(c, 16) != -1; }

    private String getHexColor(String text, int index) {
        int length = text.length();
        char code = Character.toLowerCase(text.charAt(index + 1));
        if(code == '#') {
            if(index + 8 > length) return null;
            String hex = text.substring(index + 2, index + 8);
            for(char c : hex.toCharArray()) if(!isHexChar(c)) return null;
            return "#" + hex.toLowerCase();
        }
        if(code != 'x' || index + 14 > length) return null;
        StringBuilder hex = new StringBuilder("#");
        for(int part = 0; part < 6; part++) {
            int partIndex = index + 2 + part * 2;
            if(!isCodeChar(text.charAt(partIndex)) || !isHexChar(text.charAt(partIndex + 1))) return null;
            hex.append(Character.toLowerCase(text.charAt(partIndex + 1)));
        }
        return hex.toString();
    }

    private void openTag(StringBuilder result, Deque<String> openTags, String tag) {
        result.append('<').append(tag).append('>');
        openTags.push(tag);
    }

    private void closeTags(StringBuilder result, Deque<String> openTags) {
        while(!openTags.isEmpty()) result.append("</").append(openTags.pop()).append('>');
    }

}
