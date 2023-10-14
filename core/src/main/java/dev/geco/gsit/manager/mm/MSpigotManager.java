package dev.geco.gsit.manager.mm;

import java.util.regex.*;

import org.bukkit.command.*;
import org.bukkit.entity.*;

import net.md_5.bungee.api.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.manager.*;

public class MSpigotManager extends MManager {

    protected boolean allowBungeeMessages = true;
    protected final Pattern HEX_PATTERN = Pattern.compile("(#[a-fA-F0-9]{6})");
    protected final char PRE_FORMAT_COLOR_CHAR = '&';

    public MSpigotManager(GSitMain GPluginMain) {
        super(GPluginMain);
        try { Class.forName("net.md_5.bungee.api.ChatMessageType"); } catch (Throwable e) { allowBungeeMessages = false; }
    }

    public String getAsJSON(String Text, Object... RawReplaceList) { return null; }

    public String toFormattedMessage(String Text, Object... RawReplaceList) {
        String text = org.bukkit.ChatColor.translateAlternateColorCodes(PRE_FORMAT_COLOR_CHAR, Text);
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder(text.length());
        int lastIndex = 0;
        while(matcher.find()) {
            result.append(text, lastIndex, matcher.start());
            result.append(ChatColor.of(matcher.group()).toString());
            lastIndex = matcher.end();
        }
        result.append(text.substring(lastIndex));
        text = result.toString().replace("<lang:key.sneak>", "Sneak");
        if(RawReplaceList.length == 0 || RawReplaceList.length % 2 != 0) return text;
        for(int count = 0; count < RawReplaceList.length; count += 2) {
            if(RawReplaceList[count] != null && RawReplaceList[count + 1] != null) {
                String key = RawReplaceList[count].toString();
                String value = RawReplaceList[count + 1].toString();
                text = text.replace(key, value);
            }
        }
        return text;
    }

    public void sendMessage(CommandSender Target, String Message, Object... ReplaceList) { Target.sendMessage(getLanguageMessage(Message, getLanguage(Target), ReplaceList)); }

    public void sendActionBarMessage(Player Target, String Message, Object... ReplaceList) { if(allowBungeeMessages) Target.spigot().sendMessage(ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(getLanguageMessage(Message, getLanguage(Target), ReplaceList))); }

}