package dev.geco.gsit.cmd.tab;

import java.util.*;

import org.jetbrains.annotations.*;

import org.bukkit.command.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;

public class GCrawlTabComplete implements TabCompleter {

    private final GSitMain GPM;

    public GCrawlTabComplete(GSitMain GPluginMain) { GPM = GPluginMain; }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender Sender, @NotNull Command Command, @NotNull String Label, String[] Args) {

        List<String> ta = new ArrayList<>(), ts = new ArrayList<>();

        if(Sender instanceof Player) {

            if(Args.length == 1) {

                if(GPM.getPManager().hasPermission(Sender, "CrawlToggle") && GPM.getCManager().C_DOUBLE_SNEAK) ta.add("toggle");

                if(!Args[Args.length - 1].isEmpty()) {

                    for(String r : ta) if(r.toLowerCase().startsWith(Args[Args.length - 1].toLowerCase())) ts.add(r);

                    ta.clear();
                }
            }
        }

        return ta.size() == 0 ? ts : ta;
    }

}