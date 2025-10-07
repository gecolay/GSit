package dev.geco.gsit.event;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.PrePlayerStopPlayerSitEvent;
import dev.geco.gsit.model.Seat;
import dev.geco.gsit.model.StopReason;
import dev.geco.gsit.model.Pose;
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

    public void handleEntityDismountEvent(Cancellable event, Entity entity, Entity dismounted) {
        if(!(entity instanceof Player player)) return;

        Seat seat = gSitMain.getSitService().getSeatByEntity(player);
        if(seat != null && (!gSitMain.getConfigService().GET_UP_SNEAK || (!gSitMain.getSitService().removeSeat(seat, StopReason.GET_UP, true)))) {
            event.setCancelled(true);
            return;
        }

        Pose pose = gSitMain.getPoseService().getPoseByPlayer(player);
        if(pose != null && (!gSitMain.getConfigService().GET_UP_SNEAK || !gSitMain.getPoseService().removePose(pose, StopReason.GET_UP, true))) {
            event.setCancelled(true);
            return;
        }

        if(!dismounted.getScoreboardTags().contains(PlayerSitService.PLAYERSIT_ENTITY_TAG) || !gSitMain.getPlayerSitService().isPlayerInPlayerSitStack(player)) return;

        PrePlayerStopPlayerSitEvent preEvent = new PrePlayerStopPlayerSitEvent(player, StopReason.GET_UP);
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

        gSitMain.getPlayerSitService().stopPlayerSit(player, StopReason.GET_UP, false, true, false);
    }

}