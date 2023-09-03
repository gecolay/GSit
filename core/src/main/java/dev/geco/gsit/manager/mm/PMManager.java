package dev.geco.gsit.manager.mm;

import java.util.*;

import org.jetbrains.annotations.*;

import org.bukkit.command.*;
import org.bukkit.entity.*;

import net.md_5.bungee.api.*;

import net.kyori.adventure.text.*;
import net.kyori.adventure.text.minimessage.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.manager.*;

public class PMManager extends MManager {

    private boolean allowComponentMessages = NMSManager.isNewerOrVersion(18, 2);

    public PMManager(GSitMain GPluginMain) {
        super(GPluginMain);
        try {
            Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            Class.forName("net.kyori.adventure.text.Component");
            Class.forName("net.kyori.adventure.audience.Audience");
        } catch (Throwable e) { allowComponentMessages = false; }
        loadMessages();
    }

    private @Nullable Component toFormattedComponent(String Text, Object... RawReplaceList) {
        String text = Text;
        if(!allowComponentMessages) return null;
        for(Map.Entry<String, String> tag : TAGS.entrySet()) text = text.replace(PRE_FORMAT_COLOR_CHAR + tag.getKey(), tag.getValue()).replace(PRE_FORMAT_COLOR_CHAR + tag.getKey().toUpperCase(), tag.getValue()).replace(org.bukkit.ChatColor.COLOR_CHAR + tag.getKey(), tag.getValue()).replace(org.bukkit.ChatColor.COLOR_CHAR + tag.getKey().toUpperCase(), tag.getValue());
        text = text.replaceAll("(?<!<color:)#[a-fA-F0-9]{6}(?<!>)", "<color:$0>");
        text = fixMiniMessageFormat(text);
        Component component;
        try { component = MiniMessage.miniMessage().deserialize(text); } catch (Throwable e) { component = Component.text(toFormattedMessage(Text)); }
        if(RawReplaceList.length > 0 && RawReplaceList.length % 2 == 0) for(int count = 0; count < RawReplaceList.length; count += 2) if(RawReplaceList[count] != null && RawReplaceList[count + 1] != null) {
            int finalCount = count;
            component = component.replaceText((b) -> {
                b.matchLiteral(RawReplaceList[finalCount].toString()).replacement(Component.text(RawReplaceList[finalCount + 1].toString()));
            });
        }
        return component;
    }

    private @NotNull String fixMiniMessageFormat(@NotNull String Text) {
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
                    } else if(LEVEL_TAGS.contains(tag.substring(1)) && level > 0) {
                        List<String> reset_list = reset_map.getOrDefault(level, new ArrayList<>());
                        reset_list.clear();
                        reset_map.put(level, reset_list);
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

    private @NotNull String insertStringValues(String Text, @NotNull TreeMap<Integer, String> InsertMap) {
        StringBuilder text = new StringBuilder(Text);
        int offset = 0;
        for (Map.Entry<Integer, String> entry : InsertMap.entrySet()) {
            int pos = entry.getKey();
            if(pos >= 0 && pos + offset < text.length()) text.insert(pos + offset, entry.getValue());
            offset += entry.getValue().length();
        }
        return text.toString();
    }

    private Component getLanguageComponent(String Message, String LanguageCode, Object... ReplaceList) { return toFormattedComponent(getRawLanguageMessage(Message, LanguageCode, ReplaceList)); }

    public void sendMessage(CommandSender Target, String Message, Object... ReplaceList) {
        try {
            if(allowComponentMessages) {
                Target.sendMessage(getLanguageComponent(Message, getLanguage(Target), ReplaceList));
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

}