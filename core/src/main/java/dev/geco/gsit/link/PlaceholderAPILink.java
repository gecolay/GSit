package dev.geco.gsit.link;

import java.util.*;

import org.jetbrains.annotations.*;

import org.bukkit.*;
import org.bukkit.entity.*;

import me.clip.placeholderapi.expansion.*;

import dev.geco.gsit.GSitMain;

public class PlaceholderAPILink extends PlaceholderExpansion {

    private final GSitMain GPM;

    public PlaceholderAPILink(GSitMain GPluginMain) { GPM = GPluginMain; }

    @Override
    public boolean canRegister() { return GPM.isEnabled(); }

    @Override
    public @NotNull String getName() { return GPM.getDescription().getName(); }

    @Override
    public @NotNull String getIdentifier() { return GSitMain.NAME.toLowerCase(); }

    @Override
    public @NotNull String getAuthor() { return GPM.getDescription().getAuthors().toString(); }

    @Override
    public @NotNull String getVersion() { return GPM.getDescription().getVersion(); }

    @Override
    public @NotNull List<String> getPlaceholders() { return Arrays.asList("crawling", "emoting", "playertoggle", "posing", "sitting", "toggle"); }

    @Override
    public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer Player, @NotNull String Params) {
        if(Player == null) return null;
        if(Params.equalsIgnoreCase("crawling")) return Player.isOnline() ? "" + (GPM.getCrawlManager().isCrawling((Player) Player)) : "" + false;
        if(Params.equalsIgnoreCase("playertoggle")) return "" + GPM.getToggleManager().canPlayerSit(Player.getUniqueId());
        if(Params.equalsIgnoreCase("posing")) return Player.isOnline() ? "" + (GPM.getPoseManager().isPosing((Player) Player)) : "" + false;
        if(Params.equalsIgnoreCase("sitting")) return Player.isOnline() ? "" + GPM.getSitManager().isSitting((Player) Player) : "" + false;
        if(Params.equalsIgnoreCase("toggle")) return "" + GPM.getToggleManager().canSit(Player.getUniqueId());
        return null;
    }

}