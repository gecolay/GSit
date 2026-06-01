package dev.geco.gsit.cmd;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.model.Crawl;
import dev.geco.gsit.model.StopReason;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GCrawlCommand implements CommandExecutor {

    private final GSitMain gSitMain;

    public GCrawlCommand(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(args.length == 0) {
            if(!(sender instanceof Player player)) {
                gSitMain.getMessageService().sendMessage(sender, "Messages.command-sender-error");
                return true;
            }

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

        if(!args[0].equalsIgnoreCase("toggle") || !gSitMain.getConfigService().C_DOUBLE_SNEAK) {
            Bukkit.dispatchCommand(sender, label);
            return true;
        }

        if(gSitMain.getPermissionService().hasPermission(sender, "CrawlToggle", "Crawl.*")) {
            Bukkit.dispatchCommand(sender, label);
            return true;
        }

        if(args.length == 1 && !(sender instanceof Player)) {
            Bukkit.dispatchCommand(sender, label);
            return true;
        }

        UUID uuid = sender instanceof Player player ? player.getUniqueId() : getTargetUuid(args[1]);

        boolean toggle = gSitMain.getToggleService().canPlayerUseCrawl(uuid);
        int toggleIndex = sender instanceof Player ? 1 : 2;
        if(args.length > toggleIndex && args[toggleIndex].equalsIgnoreCase("off")) toggle = true;
        if(args.length > toggleIndex && args[toggleIndex].equalsIgnoreCase("on")) toggle = false;

        if(toggle) {
            gSitMain.getToggleService().setPlayerCanUseCrawl(uuid, false);
            gSitMain.getMessageService().sendMessage(sender, "Messages.command-gcrawl-toggle-off");
        } else {
            gSitMain.getToggleService().setPlayerCanUseCrawl(uuid, true);
            gSitMain.getMessageService().sendMessage(sender, "Messages.command-gcrawl-toggle-on");
        }

        return true;
    }

    private UUID getTargetUuid(String name) {
        try {
            return UUID.fromString(name);
        } catch(IllegalArgumentException e) {
            return Bukkit.getOfflinePlayer(name).getUniqueId();
        }
    }

}