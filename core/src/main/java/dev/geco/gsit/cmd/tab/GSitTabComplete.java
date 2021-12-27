package dev.geco.gsit.cmd.tab;

import java.util.*;

import org.bukkit.entity.Player;
import org.bukkit.command.*;

import dev.geco.gsit.GSitMain;

public class GSitTabComplete implements TabCompleter {

    private final GSitMain GPM;

    public GSitTabComplete(GSitMain GPluginMain) { GPM = GPluginMain; }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String l, String[] a) {
        List<String> ta = new ArrayList<>(), ts = new ArrayList<>();
        if(s instanceof Player) {
            if(a.length == 1) {
                if(GPM.getPManager().hasNormalPermission(s, "SitToggle")) ta.add("toggle");
                if(GPM.getPManager().hasNormalPermission(s, "PlayerSitToggle")) ta.add("playertoggle");
                if(!a[a.length - 1].isEmpty()) {
                    for(String r : ta) if(r.toLowerCase().startsWith(a[a.length - 1].toLowerCase())) ts.add(r);
                    ta.clear();
                }
            }
        }
        return ta.size() == 0 ? ts : ta;
    }

}