package dev.geco.gsit.cmd;

import org.jetbrains.annotations.NotNull;

import org.bukkit.command.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class GEmoteCommand implements CommandExecutor {

    private final GSitMain GPM;

    public GEmoteCommand(GSitMain GPluginMain) { GPM = GPluginMain; }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, String[] a) {
        if(s instanceof Player) {
            Player p = (Player) s;
            if(GPM.getPManager().hasNormalPermission(s, "Emote")) {
                if(a.length == 0) {
                    GPM.getMManager().sendMessage(s, "Messages.action-emote-none-error");
                } else {
                    if(GPM.getEmoteManager().isEmoting(p)) {
                        GPM.getEmoteManager().stopEmote(p);
                        return true;
                    }
                    GEmote emote = GPM.getEmoteManager().getEmoteByName(a[0]);
                    if(emote != null) {
                        GPM.getEmoteManager().startEmote(p, emote);
                    } else GPM.getMManager().sendMessage(s, "Messages.action-emote-error", "%Emote%", a[0]);
                }
            } else GPM.getMManager().sendMessage(s, "Messages.command-permission-error");
        } else GPM.getMManager().sendMessage(s, "Messages.command-sender-error");
        return true;
    }

}