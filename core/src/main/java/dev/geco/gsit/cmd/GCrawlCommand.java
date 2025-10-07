package dev.geco.gsit.cmd;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.model.StopReason;
import dev.geco.gsit.model.Crawl;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GCrawlCommand implements CommandExecutor {

    private final GSitMain gSitMain;

    public GCrawlCommand(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(!(sender instanceof Player player)) {
            gSitMain.getMessageService().sendMessage(sender, "Messages.command-sender-error");
            return true;
        }

        if(args.length == 0) {
            if(!gSitMain.getPermissionService().hasPermission(sender, "Crawl", "Crawl.*")) {
                gSitMain.getMessageService().sendMessage(sender, "Messages.command-permission-error");
                return true;
            }

            if(!gSitMain.getCrawlService().isAvailable()) {
                gSitMain.getMessageService().sendMessage(sender, "Messages.command-version-error", "%Version%", gSitMain.getVersionManager().getServerVersion());
                return true;
            }

            Crawl crawl = gSitMain.getCrawlService().getCrawlByPlayer(player);
            if(crawl != null) {
                gSitMain.getCrawlService().stopCrawl(crawl, StopReason.GET_UP);
                return true;
            }

            if(!player.isValid() || !player.isOnGround() || player.getVehicle() != null || player.isSleeping()) {
                gSitMain.getMessageService().sendMessage(sender, "Messages.action-crawl-now-error");
                return true;
            }

            if(!gSitMain.getEnvironmentUtil().isEntityInAllowedWorld(player)) {
                gSitMain.getMessageService().sendMessage(sender, "Messages.action-crawl-world-error");
                return true;
            }

            if(!gSitMain.getEnvironmentUtil().canUseInLocation(player.getLocation(), player, "crawl")) {
                gSitMain.getMessageService().sendMessage(sender, "Messages.action-crawl-region-error");
                return true;
            }

            if(gSitMain.getCrawlService().startCrawl(player) == null) gSitMain.getMessageService().sendMessage(sender, "Messages.action-crawl-region-error");
            return true;
        }

        if(args[0].equalsIgnoreCase("toggle") && gSitMain.getConfigService().C_DOUBLE_SNEAK) {
            if(gSitMain.getPermissionService().hasPermission(sender, "CrawlToggle", "Crawl.*")) {
                boolean toggle = gSitMain.getToggleService().canPlayerUseCrawl(player.getUniqueId());
                if(args.length > 1 && args[1].equalsIgnoreCase("off")) toggle = true;
                if(args.length > 1 && args[1].equalsIgnoreCase("on")) toggle = false;

                if(toggle) {
                    gSitMain.getToggleService().setPlayerCanUseCrawl(player.getUniqueId(), false);
                    gSitMain.getMessageService().sendMessage(sender, "Messages.command-gcrawl-toggle-off");
                } else {
                    gSitMain.getToggleService().setPlayerCanUseCrawl(player.getUniqueId(), true);
                    gSitMain.getMessageService().sendMessage(sender, "Messages.command-gcrawl-toggle-on");
                }
            } else Bukkit.dispatchCommand(sender, label);
        } else Bukkit.dispatchCommand(sender, label);

        return true;
    }

}