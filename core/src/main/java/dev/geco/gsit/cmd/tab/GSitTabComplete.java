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

        List<String> ta = new ArrayList<>(), ts = new ArrayList<>();

        if(Sender instanceof Player) {

            if(Args.length == 1) {

                if(GPM.getPManager().hasNormalPermission(Sender, "SitToggle")) ta.add("toggle");

                if(GPM.getPManager().hasNormalPermission(Sender, "PlayerSitToggle")) ta.add("playertoggle");

                if(!Args[Args.length - 1].isEmpty()) {

                    for(String r : ta) if(r.toLowerCase().startsWith(Args[Args.length - 1].toLowerCase())) ts.add(r);

                    ta.clear();
                }
            }
        }

        return ta.size() == 0 ? ts : ta;
    }

}