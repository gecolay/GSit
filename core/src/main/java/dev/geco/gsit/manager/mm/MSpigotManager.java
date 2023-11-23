package dev.geco.gsit.manager.mm;

import org.bukkit.command.*;
import org.bukkit.entity.*;

import net.md_5.bungee.api.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.manager.*;

public class MSpigotManager extends MManager {

    protected boolean allowBungeeMessages = true;

    public MSpigotManager(GSitMain GPluginMain) {
        super(GPluginMain);
        try { Class.forName("net.md_5.bungee.api.ChatMessageType"); } catch (Throwable e) { allowBungeeMessages = false; }
    }

    public String toFormattedMessage(String Text, Object... RawReplaceList) {
        String text = org.bukkit.ChatColor.translateAlternateColorCodes(AMPERSAND_CHAR, replaceText(Text, RawReplaceList).replace("<lang:key.sneak>", "Sneak"));
        return replaceHexColors(text);
    }

    public void sendMessage(CommandSender Target, String Message, Object... ReplaceList) { Target.sendMessage(getMessage(Message, getLanguage(Target), ReplaceList)); }

    public void sendActionBarMessage(Player Target, String Message, Object... ReplaceList) { if(allowBungeeMessages) Target.spigot().sendMessage(ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(getMessage(Message, getLanguage(Target), ReplaceList))); }

}