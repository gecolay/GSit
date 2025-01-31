package dev.geco.gsit.cmd.tab;

import dev.geco.gsit.GSitMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GCrawlTabComplete implements TabCompleter {

    private final GSitMain gSitMain;

    public GCrawlTabComplete(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(!gSitMain.getConfigService().C_DOUBLE_SNEAK || !(sender instanceof Player)) return new ArrayList<>();
        List<String> complete = new ArrayList<>(), completeStarted = new ArrayList<>();
        if(args.length == 1) {
            if(gSitMain.getPermissionService().hasPermission(sender, "CrawlToggle", "Crawl.*")) complete.add("toggle");
            if(!args[args.length - 1].isEmpty()) {
                for(String entry : complete) if(entry.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) completeStarted.add(entry);
                complete.clear();
            }
        } else if(args.length == 2) {
            if(gSitMain.getPermissionService().hasPermission(sender, "CrawlToggle", "Crawl.*") && args[0].equalsIgnoreCase("toggle")) {
                complete.add("on");
                complete.add("off");
            }
            if(!args[args.length - 1].isEmpty()) {
                for(String entry : complete) if(entry.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) completeStarted.add(entry);
                complete.clear();
            }
        }
        return complete.isEmpty() ? completeStarted : complete;
    }

}