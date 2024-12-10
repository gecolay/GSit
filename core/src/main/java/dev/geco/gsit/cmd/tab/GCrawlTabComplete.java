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

        if(!GPM.getCManager().C_DOUBLE_SNEAK || !(Sender instanceof Player)) return new ArrayList<>();

        List<String> complete = new ArrayList<>(), completeStarted = new ArrayList<>();

        if(Args.length == 1) {

            if(GPM.getPManager().hasPermission(Sender, "CrawlToggle", "Crawl.*")) complete.add("toggle");

            if(!Args[Args.length - 1].isEmpty()) {
                for(String entry : complete) if(entry.toLowerCase().startsWith(Args[Args.length - 1].toLowerCase())) completeStarted.add(entry);
                complete.clear();
            }
        } else if(Args.length == 2) {

            if(GPM.getPManager().hasPermission(Sender, "CrawlToggle", "Crawl.*") && Args[0].equalsIgnoreCase("toggle")) {
                complete.add("on");
                complete.add("off");
            }

            if(!Args[Args.length - 1].isEmpty()) {
                for(String entry : complete) if(entry.toLowerCase().startsWith(Args[Args.length - 1].toLowerCase())) completeStarted.add(entry);
                complete.clear();
            }
        }

        return complete.isEmpty() ? completeStarted : complete;
    }

}