package dev.geco.gsit.service.message;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.service.MessageService;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpigotMessageService extends MessageService {

    protected boolean allowBungeeMessages = true;

    public SpigotMessageService(GSitMain gSitMain) {
        super(gSitMain);
        try { Class.forName("net.md_5.bungee.api.ChatMessageType"); } catch(Throwable e) { allowBungeeMessages = false; }
    }

    public String toFormattedMessage(String text, Object... rawReplaceList) { return org.bukkit.ChatColor.translateAlternateColorCodes(AMPERSAND_CHAR, replaceHexColorsDirectly(replaceText(text, rawReplaceList).replace("<lang:key.sneak>", "Sneak"))); }

    public void sendMessage(@NotNull CommandSender target, String message, Object... replaceList) {
        String translatedMessage = getTranslatedMessage(message, getLanguageForTarget(target), replaceList);
        if(translatedMessage.isEmpty()) return;
        target.sendMessage(translatedMessage);
    }

    public void sendActionBarMessage(@NotNull Player target, String message, Object... replaceList) {
        if(!allowBungeeMessages) return;
        String translatedMessage = getTranslatedMessage(message, getLanguageForTarget(target), replaceList);
        if(translatedMessage.isEmpty()) return;
        target.spigot().sendMessage(ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(translatedMessage));
    }

}