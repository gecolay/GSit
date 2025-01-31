package dev.geco.gsit.event;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.PrePlayerStopPlayerSitEvent;
import dev.geco.gsit.object.GetUpReason;
import dev.geco.gsit.service.PlayerSitService;
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
        if(event.isCancelled() && (mount.getScoreboardTags().contains(GSitMain.NAME + "_SeatEntity") || mount.getScoreboardTags().contains(PlayerSitService.PLAYERSIT_ENTITY_TAG))) event.setCancelled(false);
    }

    public void handleEntityDismountEvent(Cancellable event, Entity entity, Entity dismounted) {
        if(!(entity instanceof Player player)) return;

        if(gSitMain.getSitService().isEntitySitting(player) && (!gSitMain.getConfigService().GET_UP_SNEAK || (!gSitMain.getSitService().removeSeat(player, GetUpReason.GET_UP, true)))) {
            event.setCancelled(true);
            return;
        }
        if(gSitMain.getPoseService().isPlayerPosing(player) && (!gSitMain.getConfigService().GET_UP_SNEAK || !gSitMain.getPoseService().removePose(player, GetUpReason.GET_UP, true))) {
            event.setCancelled(true);
            return;
        }

        if(!dismounted.getScoreboardTags().contains(PlayerSitService.PLAYERSIT_ENTITY_TAG)) return;

        PrePlayerStopPlayerSitEvent preEvent = new PrePlayerStopPlayerSitEvent(player, GetUpReason.GET_UP, false);
        Bukkit.getPluginManager().callEvent(preEvent);
        if(preEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        gSitMain.getPlayerSitService().getPreventDismountStackPlayers().add(player);

        gSitMain.getTaskService().runDelayed(() -> {
            gSitMain.getPlayerSitService().getPreventDismountStackPlayers().remove(player);
        }, 2);

        Entity bottom = gSitMain.getPassengerUtil().getBottomEntityVehicle(dismounted);

        if(gSitMain.getConfigService().PS_BOTTOM_RETURN && player.isValid()) gSitMain.getEntityUtil().setEntityLocation(player, bottom.getLocation());

        gSitMain.getPlayerSitService().stopPlayerSit(player, GetUpReason.GET_UP, false, false);
    }

}