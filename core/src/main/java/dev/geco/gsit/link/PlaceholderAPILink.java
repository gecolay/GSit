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
    public @NotNull String getIdentifier() { return GPM.NAME.toLowerCase(); }

    @Override
    public @NotNull String getAuthor() { return GPM.getDescription().getAuthors().toString(); }

    @Override
    public @NotNull String getVersion() { return GPM.getDescription().getVersion(); }

    @Override
    public @NotNull List<String> getPlaceholders() { return Arrays.asList("playertoggle", "posing", "sitting", "toggle"); }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {

        if(player == null) return null;

        if(params.equalsIgnoreCase("playertoggle")) return "" + GPM.getToggleManager().canPlayerSit(player.getUniqueId());

        if(params.equalsIgnoreCase("posing")) return player.isOnline() ? "" + GPM.getPoseManager().isPosing((Player) player) : "" + false;

        if(params.equalsIgnoreCase("sitting")) return player.isOnline() ? "" + GPM.getSitManager().isSitting((Player) player) : "" + false;

        if(params.equalsIgnoreCase("toggle")) return "" + GPM.getToggleManager().canSit(player.getUniqueId());

        return null;
    }

}