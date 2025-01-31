package dev.geco.gsit.link;

import dev.geco.gsit.GSitMain;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class PlaceholderAPILink extends PlaceholderExpansion {

    private final GSitMain gSitMain;

    public PlaceholderAPILink(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @Override
    public boolean canRegister() { return gSitMain.isEnabled(); }

    @Override
    public @NotNull String getName() { return gSitMain.getDescription().getName(); }

    @Override
    public @NotNull String getIdentifier() { return GSitMain.NAME.toLowerCase(); }

    @Override
    public @NotNull String getAuthor() { return gSitMain.getDescription().getAuthors().toString(); }

    @Override
    public @NotNull String getVersion() { return gSitMain.getDescription().getVersion(); }

    @Override
    public @NotNull List<String> getPlaceholders() { return Arrays.asList("crawling", "emoting", "playertoggle", "posing", "sitting", "toggle"); }

    @Override
    public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String placeholder) {
        if(offlinePlayer == null) return null;
        if(placeholder.equalsIgnoreCase("crawling")) return offlinePlayer.isOnline() ? "" + (gSitMain.getCrawlService().isPlayerCrawling(offlinePlayer.getPlayer())) : "" + false;
        else if(placeholder.equalsIgnoreCase("playertoggle")) return "" + gSitMain.getToggleService().canPlayerUsePlayerSit(offlinePlayer.getUniqueId());
        else if(placeholder.equalsIgnoreCase("posing")) return offlinePlayer.isOnline() ? "" + (gSitMain.getPoseService().isPlayerPosing(offlinePlayer.getPlayer())) : "" + false;
        else if(placeholder.equalsIgnoreCase("sitting")) return offlinePlayer.isOnline() ? "" + gSitMain.getSitService().isEntitySitting(offlinePlayer.getPlayer()) : "" + false;
        else if(placeholder.equalsIgnoreCase("toggle")) return "" + gSitMain.getToggleService().canEntityUseSit(offlinePlayer.getUniqueId());
        return null;
    }

}