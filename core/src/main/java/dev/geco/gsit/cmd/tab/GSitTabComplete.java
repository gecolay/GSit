package dev.geco.gsit.cmd.tab;

import dev.geco.gsit.GSitMain;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class GSitTabComplete implements TabCompleter {

    private final GSitMain gSitMain;

    public GSitTabComplete(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> complete = new ArrayList<>(), completeStarted = new ArrayList<>();
        if(args.length == 1) {
            if(gSitMain.getPermissionService().hasPermission(sender, "SitToggle", "Sit.*")) complete.add("toggle");
            if(gSitMain.getPermissionService().hasPermission(sender, "PlayerSitToggle", "PlayerSit.*") && gSitMain.getConfigService().PS_ALLOW_SIT) complete.add("playertoggle");
            if(!args[args.length - 1].isEmpty()) {
                for(String entry : complete) if(entry.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) completeStarted.add(entry);
                complete.clear();
            }
        } else if(args.length == 2) {
            if(gSitMain.getPermissionService().hasPermission(sender, "SitToggle", "Sit.*") && args[0].equalsIgnoreCase("toggle")) {
                if(sender instanceof Player) {
                    complete.add("on");
                    complete.add("off");
                } else {
                    complete.addAll(Stream.of(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toList());
                }
            }
            if(gSitMain.getPermissionService().hasPermission(sender, "PlayerSitToggle", "PlayerSit.*") && args[0].equalsIgnoreCase("playertoggle") && gSitMain.getConfigService().PS_ALLOW_SIT) {
                if(sender instanceof Player) {
                    complete.add("on");
                    complete.add("off");
                } else {
                    complete.addAll(Stream.of(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toList());
                }
            }
            if(!args[args.length - 1].isEmpty()) {
                for(String entry : complete) if(entry.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) completeStarted.add(entry);
                complete.clear();
            }
        } else if(args.length == 3) {
            if(gSitMain.getPermissionService().hasPermission(sender, "SitToggle", "Sit.*") && args[0].equalsIgnoreCase("toggle") && !(sender instanceof Player)) {
                complete.add("on");
                complete.add("off");
            }
            if(gSitMain.getPermissionService().hasPermission(sender, "PlayerSitToggle", "PlayerSit.*") && args[0].equalsIgnoreCase("playertoggle") && gSitMain.getConfigService().PS_ALLOW_SIT && !(sender instanceof Player)) {
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