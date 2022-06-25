package dev.geco.gsit.cmd.tab;

import java.util.*;

import org.jetbrains.annotations.NotNull;

import org.bukkit.command.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class GEmoteTabComplete implements TabCompleter {

    private final GSitMain GPM;

    public GEmoteTabComplete(GSitMain GPluginMain) { GPM = GPluginMain; }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, String[] a) {
        List<String> ta = new ArrayList<>(), ts = new ArrayList<>();
        if(s instanceof Player) {
            if(a.length == 1) {
                if(GPM.getPManager().hasNormalPermission(s, "Emote")) for(GEmote emote : GPM.getEmoteManager().getAvailableEmotes()) ta.add(emote.getId());
                if(!a[a.length - 1].isEmpty()) {
                    for(String r : ta) if(r.toLowerCase().startsWith(a[a.length - 1].toLowerCase())) ts.add(r);
                    ta.clear();
                }
            }
        }
        return ta.size() == 0 ? ts : ta;
    }

}