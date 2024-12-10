package dev.geco.gsit.cmd;

import org.jetbrains.annotations.*;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class GCrawlCommand implements CommandExecutor {

    private final GSitMain GPM;

    public GCrawlCommand(GSitMain GPluginMain) { GPM = GPluginMain; }

    @Override
    public boolean onCommand(@NotNull CommandSender Sender, @NotNull Command Command, @NotNull String Label, String[] Args) {

        if(!(Sender instanceof Player player)) {

            GPM.getMManager().sendMessage(Sender, "Messages.command-sender-error");
            return true;
        }

        if(Args.length == 0) {

            if(!GPM.getPManager().hasPermission(Sender, "Crawl", "Crawl.*")) {

                GPM.getMManager().sendMessage(Sender, "Messages.command-permission-error");
                return true;
            }

            if(!GPM.getCrawlManager().isAvailable()) {

                GPM.getMManager().sendMessage(Sender, "Messages.command-version-error", "%Version%", GPM.getSVManager().getServerVersion());
                return true;
            }

            if(GPM.getCrawlManager().isCrawling(player)) {

                GPM.getCrawlManager().stopCrawl(player, GetUpReason.GET_UP);
                return true;
            }

            if(!player.isValid() || !player.isOnGround() || player.getVehicle() != null || player.isSleeping()) {

                GPM.getMManager().sendMessage(Sender, "Messages.action-crawl-now-error");
                return true;
            }

            if(!GPM.getPManager().hasPermission(Sender, "ByPass.Region", "ByPass.*")) {

                if(!GPM.getEnvironmentUtil().isInAllowedWorld(player)) {

                    GPM.getMManager().sendMessage(Sender, "Messages.action-crawl-world-error");
                    return true;
                }
            }

            if(GPM.getWorldGuardLink() != null && !GPM.getWorldGuardLink().checkFlag(player.getLocation(), GPM.getWorldGuardLink().getFlag("crawl"))) {

                GPM.getMManager().sendMessage(Sender, "Messages.action-crawl-region-error");
                return true;
            }

            if(GPM.getCrawlManager().startCrawl(player) == null) GPM.getMManager().sendMessage(Sender, "Messages.action-crawl-region-error");
            return true;
        }

        if(Args[0].equalsIgnoreCase("toggle") && GPM.getCManager().C_DOUBLE_SNEAK) {

            if(GPM.getPManager().hasPermission(Sender, "CrawlToggle", "Crawl.*")) {

                boolean toggle = GPM.getToggleManager().canCrawl(player.getUniqueId());

                if(Args.length > 1 && Args[1].equalsIgnoreCase("off")) toggle = true;

                if(Args.length > 1 && Args[1].equalsIgnoreCase("on")) toggle = false;

                if(toggle) {

                    GPM.getToggleManager().setCanCrawl(player.getUniqueId(), false);

                    GPM.getMManager().sendMessage(Sender, "Messages.command-gcrawl-toggle-off");
                } else {

                    GPM.getToggleManager().setCanCrawl(player.getUniqueId(), true);

                    GPM.getMManager().sendMessage(Sender, "Messages.command-gcrawl-toggle-on");
                }
            } else Bukkit.dispatchCommand(Sender, Label);
        } else Bukkit.dispatchCommand(Sender, Label);

        return true;
    }

}