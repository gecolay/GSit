package dev.geco.gsit.manager.mm;

import org.jetbrains.annotations.*;

import org.bukkit.command.*;
import org.bukkit.entity.*;

import net.kyori.adventure.text.*;
import net.kyori.adventure.text.minimessage.*;
import net.kyori.adventure.text.serializer.json.*;
import net.kyori.adventure.text.serializer.legacy.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.manager.*;

public class MPaperManager extends MManager {

    protected final LegacyComponentSerializer legacyComponentSerializer;
    protected Object jsonComponentSerializer;
    protected final MiniMessage miniMessage;

    public MPaperManager(GSitMain GPluginMain) {
        super(GPluginMain);
        legacyComponentSerializer = LegacyComponentSerializer.builder().character(AMPERSAND_CHAR).hexColors().build();
        if(GPluginMain.getSVManager().isNewerOrVersion(20, 0)) jsonComponentSerializer = JSONComponentSerializer.json();
        miniMessage = MiniMessage.miniMessage();
    }

    public String getAsJSON(String Text, Object... RawReplaceList) { return jsonComponentSerializer != null ? ((JSONComponentSerializer) jsonComponentSerializer).serialize(toFormattedComponent(Text, RawReplaceList)) : super.getAsJSON(Text, RawReplaceList); }

    public String toFormattedMessage(String Text, Object... RawReplaceList) { return org.bukkit.ChatColor.translateAlternateColorCodes(AMPERSAND_CHAR, replaceHexColorsDirect(formatText(Text, RawReplaceList))); }

    public void sendMessage(@NotNull CommandSender Target, String Message, Object... ReplaceList) { Target.sendMessage(getLanguageComponent(Message, getLanguage(Target), ReplaceList)); }

    public void sendActionBarMessage(@NotNull Player Target, String Message, Object... ReplaceList) { Target.sendActionBar(getLanguageComponent(Message, getLanguage(Target), ReplaceList)); }

    private @NotNull Component getLanguageComponent(String Message, String LanguageCode, Object... ReplaceList) { return toFormattedComponent(getRawMessageByLanguage(Message, LanguageCode, ReplaceList)); }

    private @NotNull Component toFormattedComponent(String Text, Object... RawReplaceList) { return legacyComponentSerializer.deserialize(replaceHexColors(formatText(Text, RawReplaceList))); }

    private String formatText(String Text, Object... RawReplaceList) {
        Component component = miniMessage.deserialize(replaceText(Text, RawReplaceList));
        return legacyComponentSerializer.serialize(component);
    }

}