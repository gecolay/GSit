package dev.geco.gsit.cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class GCrawlCommand implements CommandExecutor {

    private final GSitMain GPM;

    public GCrawlCommand(GSitMain GPluginMain) { GPM = GPluginMain; }
    
    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if(s instanceof Player) {
            Player p = (Player) s;
            if(GPM.getPManager().hasNormalPermission(s, "Crawl")) {
                if(GPM.getCrawlManager() != null) {
                    if(GPM.getCrawlManager().isCrawling(p)) {
                        GPM.getCrawlManager().stopCrawl(GPM.getCrawlManager().getCrawl(p), GetUpReason.GET_UP);
                    } else {
                        if(p.isValid() && !p.isSneaking() && p.isOnGround() && !p.isInsideVehicle() && !p.isSleeping()) {
                            if(!GPM.getCManager().WORLDBLACKLIST.contains(p.getWorld().getName())) {
                                if(GPM.getWorldGuard() == null || GPM.getWorldGuard().checkFlag(p.getLocation(), GPM.getWorldGuard().CRAWL_FLAG)) {
                                    GPM.getCrawlManager().startCrawl(p);
                                } else GPM.getMManager().sendMessage(s, "Messages.action-crawl-region-error");
                            } else GPM.getMManager().sendMessage(s, "Messages.action-crawl-world-error");
                        } else GPM.getMManager().sendMessage(s, "Messages.action-crawl-now-error");
                    }
                } else {
                    String v = Bukkit.getServer().getClass().getPackage().getName();
                    v = v.substring(v.lastIndexOf('.') + 1);
                    GPM.getMManager().sendMessage(s, "Messages.command-version-error", "%Version%", v);
                }
            } else GPM.getMManager().sendMessage(s, "Messages.command-permission-error");
        } else GPM.getMManager().sendMessage(s, "Messages.command-sender-error");
        return true;
    }
    
}