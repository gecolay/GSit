package dev.geco.gsit.service.message;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.service.MessageService;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpigotMessageService extends MessageService {

    private static final Pattern CLICK_PATTERN = Pattern.compile("<click:open_url:'([^']+)'>((?:<hover:show_text:'([^']+)'>)?(.*?)(?:</hover>)?)</click>");
    private static final Pattern CLICK_STRIP_PATTERN = Pattern.compile("<click:open_url:'[^']+'>((?:<hover:show_text:'[^']+'>)?(.*?)(?:</hover>)?)</click>");

    protected boolean allowBungeeMessages = true;

    public SpigotMessageService(GSitMain gSitMain) {
        super(gSitMain);
        try { Class.forName("net.md_5.bungee.api.ChatMessageType"); } catch(Throwable e) { allowBungeeMessages = false; }
    }

    public String toFormattedMessage(String text, Object... rawReplaceList) { return org.bukkit.ChatColor.translateAlternateColorCodes(AMPERSAND_CHAR, replaceHexColorsDirectly(replaceText(text, rawReplaceList).replace("<lang:key.sneak>", "Sneak"))); }

    public void sendMessage(@NotNull CommandSender target, String message, Object... replaceList) {
        String translatedMessage = getTranslatedMessage(message, getLanguageForTarget(target), replaceList);
        if(translatedMessage.isEmpty()) return;
        if(allowBungeeMessages && target instanceof Player && translatedMessage.contains("<click:open_url:")) {
            ((Player) target).spigot().sendMessage(parseClickableComponents(translatedMessage));
            return;
        }
        if(translatedMessage.contains("<click:open_url:")) {
            translatedMessage = CLICK_STRIP_PATTERN.matcher(translatedMessage).replaceAll("$4");
        }
        target.sendMessage(translatedMessage);
    }

    public void sendActionBarMessage(@NotNull Player target, String message, Object... replaceList) {
        if(!allowBungeeMessages) return;
        String translatedMessage = getTranslatedMessage(message, getLanguageForTarget(target), replaceList);
        if(translatedMessage.isEmpty()) return;
        target.spigot().sendMessage(ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(translatedMessage));
    }

    private BaseComponent[] parseClickableComponents(String text) {
        List<BaseComponent> components = new ArrayList<>();
        Matcher matcher = CLICK_PATTERN.matcher(text);
        int lastIndex = 0;
        while(matcher.find()) {
            if(matcher.start() > lastIndex) {
                for(BaseComponent c : TextComponent.fromLegacyText(text.substring(lastIndex, matcher.start()))) {
                    components.add(c);
                }
            }
            String url = matcher.group(1);
            String hover = matcher.group(3);
            String linkText = matcher.group(4);

            BaseComponent[] linkComponents = TextComponent.fromLegacyText(linkText);
            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
            HoverEvent hoverEvent = hover != null && !hover.isEmpty() ? new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover)) : null;

            for(BaseComponent c : linkComponents) {
                c.setClickEvent(clickEvent);
                if(hoverEvent != null) c.setHoverEvent(hoverEvent);
                components.add(c);
            }
            lastIndex = matcher.end();
        }
        if(lastIndex < text.length()) {
            for(BaseComponent c : TextComponent.fromLegacyText(text.substring(lastIndex))) {
                components.add(c);
            }
        }
        return components.toArray(new BaseComponent[0]);
    }

}
