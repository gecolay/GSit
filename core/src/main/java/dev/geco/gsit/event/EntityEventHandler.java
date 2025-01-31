package dev.geco.gsit.event;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.PlayerGetUpPlayerSitEvent;
import dev.geco.gsit.api.event.PrePlayerGetUpPlayerSitEvent;
import dev.geco.gsit.object.GetUpReason;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class EntityEventHandler {

    private final GSitMain gSitMain;

    public EntityEventHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    public void handleEntityMountEvent(Cancellable event, Entity mount) {
        if(event.isCancelled() && (mount.getScoreboardTags().contains(GSitMain.NAME + "_SeatEntity") || mount.getScoreboardTags().contains(GSitMain.NAME + "_PlayerSeatEntity"))) event.setCancelled(false);
    }

    public void handleEntityDismountEvent(Cancellable event, Entity entity, Entity dismounted) {
        if(entity instanceof Player player) {
            if(gSitMain.getSitService().isEntitySitting(player) && (!gSitMain.getConfigService().GET_UP_SNEAK || (!gSitMain.getSitService().removeSeat(player, GetUpReason.GET_UP, true)))) {
                event.setCancelled(true);
                return;
            }
            if(gSitMain.getPoseService().isPlayerPosing(player) && (!gSitMain.getConfigService().GET_UP_SNEAK || !gSitMain.getPoseService().removePose(player, GetUpReason.GET_UP, true))) {
                event.setCancelled(true);
                return;
            }
        }

        if(!dismounted.getScoreboardTags().contains(GSitMain.NAME + "_PlayerSeatEntity") && !(dismounted instanceof Player)) return;

        if(entity instanceof Player player) {

            PrePlayerGetUpPlayerSitEvent preEvent = new PrePlayerGetUpPlayerSitEvent(player, GetUpReason.GET_UP);
            Bukkit.getPluginManager().callEvent(preEvent);
            if(preEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }

            gSitMain.getPlayerSitService().getWaitEjectPlayers().add(player);

            gSitMain.getTaskService().runDelayed(() -> {
                gSitMain.getPlayerSitService().getWaitEjectPlayers().remove(player);
            }, 2);
        }

        Entity bottom = gSitMain.getPassengerUtil().getBottomEntityVehicle(dismounted);

        if(gSitMain.getConfigService().PS_BOTTOM_RETURN && entity.isValid() && entity instanceof Player) gSitMain.getEntityUtil().setEntityLocation(entity, bottom.getLocation());

        gSitMain.getPlayerSitService().stopPlayerSit(dismounted, GetUpReason.GET_UP);

        if(entity instanceof Player) Bukkit.getPluginManager().callEvent(new PlayerGetUpPlayerSitEvent((Player) entity, GetUpReason.GET_UP));
    }

}