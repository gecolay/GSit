package dev.geco.gsit.manager.mm;

import org.jetbrains.annotations.*;

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

    public String toFormattedMessage(String Text, Object... RawReplaceList) { return org.bukkit.ChatColor.translateAlternateColorCodes(AMPERSAND_CHAR, replaceHexColorsDirect(replaceText(Text, RawReplaceList).replace("<lang:key.sneak>", "Sneak"))); }

    public void sendMessage(@NotNull CommandSender Target, String Message, Object... ReplaceList) {
        String message = getMessageByLanguage(Message, getLanguage(Target), ReplaceList);
        if(message.isEmpty()) return;
        Target.sendMessage(message);
    }

    public void sendActionBarMessage(@NotNull Player Target, String Message, Object... ReplaceList) {
        if(!allowBungeeMessages) return;
        String message = getMessageByLanguage(Message, getLanguage(Target), ReplaceList);
        if(message.isEmpty()) return;
        Target.spigot().sendMessage(ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
    }

}