package dev.geco.gsit.cmd.tab;

import java.util.*;

import org.jetbrains.annotations.*;

import org.bukkit.command.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;

public class GSitTabComplete implements TabCompleter {

    private final GSitMain GPM;

    public GSitTabComplete(GSitMain GPluginMain) { GPM = GPluginMain; }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender Sender, @NotNull Command Command, @NotNull String Label, String[] Args) {

        List<String> complete = new ArrayList<>(), completeStarted = new ArrayList<>();

        if(Sender instanceof Player) {

            if(Args.length == 1) {

                if(GPM.getPManager().hasPermission(Sender, "SitToggle") && !GPM.getCManager().S_SITMATERIALS.isEmpty()) complete.add("toggle");

                if(GPM.getPManager().hasPermission(Sender, "PlayerSitToggle") && GPM.getCManager().PS_ALLOW_SIT) complete.add("playertoggle");

                if(!Args[Args.length - 1].isEmpty()) for(String entry : complete) if(entry.toLowerCase().startsWith(Args[Args.length - 1].toLowerCase())) completeStarted.add(entry);
            }
        }

        return !completeStarted.isEmpty() ? completeStarted : complete;
    }

}