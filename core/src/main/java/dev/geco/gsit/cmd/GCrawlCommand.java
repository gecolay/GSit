package dev.geco.gsit.cmd;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

import dev.geco.gsit.GSitMain;

public class GCrawlCommand implements CommandExecutor {

    private final GSitMain GPM;

    public GCrawlCommand(GSitMain GPluginMain) { GPM = GPluginMain; }
    
    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if(s instanceof Player) {
            Player p = (Player) s;

        } else GPM.getMManager().sendMessage(s, "Messages.command-sender-error");
        return true;
    }
    
}