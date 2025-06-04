package dev.geco.gsit.event;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.PrePlayerStopPlayerSitEvent;
import dev.geco.gsit.object.GSeat;
import dev.geco.gsit.object.GStopReason;
import dev.geco.gsit.object.IGPose;
import dev.geco.gsit.service.PlayerSitService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class EntityEventHandler {

    private final GSitMain gSitMain;
    private final PlayerSitService playerSitService;
    private final boolean getUpSneakEnabled;
    private final boolean bottomReturnEnabled;

    public EntityEventHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        this.playerSitService = gSitMain.getPlayerSitService();
        this.getUpSneakEnabled = gSitMain.getConfigService().GET_UP_SNEAK;
        this.bottomReturnEnabled = gSitMain.getConfigService().PS_BOTTOM_RETURN;
    }

    public void handleEntityDismountEvent(Cancellable event, Entity entity, Entity dismounted) {
        if (!(entity instanceof Player player)) return;
        
        // Cache frequently accessed services
        final var sitService = gSitMain.getSitService();
        final var poseService = gSitMain.getPoseService();
        final var taskService = gSitMain.getTaskService();
        final var entityUtil = gSitMain.getEntityUtil();
        final var passengerUtil = gSitMain.getPassengerUtil();

        // Handle regular seats
        GSeat seat = sitService.getSeatByEntity(player);
        if (seat != null) {
            if (!getUpSneakEnabled || !sitService.removeSeat(seat, GStopReason.GET_UP, true)) {
                event.setCancelled(true);
                return;
            }
        }

        // Handle poses
        IGPose pose = poseService.getPoseByPlayer(player);
        if (pose != null) {
            if (!getUpSneakEnabled || !poseService.removePose(pose, GStopReason.GET_UP, true)) {
                event.setCancelled(true);
                return;
            }
        }

        // Handle player sit entities
        if (!dismounted.getScoreboardTags().contains(PlayerSitService.PLAYERSIT_ENTITY_TAG) || 
            !playerSitService.isPlayerInPlayerSitStack(player)) {
            return;
        }

        // Fire pre-cancel event
        PrePlayerStopPlayerSitEvent preEvent = new PrePlayerStopPlayerSitEvent(player, GStopReason.GET_UP);
        Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        // Prevent immediate re-dismount
        playerSitService.getPreventDismountStackPlayers().add(player);
        
        // Schedule delayed cleanup
        taskService.runDelayed(() -> 
            playerSitService.getPreventDismountStackPlayers().remove(player), 
            2
        );

        // Handle bottom entity return
        if (bottomReturnEnabled) {
            Entity bottomEntity = passengerUtil.getBottomEntityVehicle(dismounted);
            taskService.runDelayed(() -> {
                if (player.isValid()) {
                    entityUtil.setEntityLocation(player, bottomEntity.getLocation());
                }
            }, player, 1);
        }

        // Stop player sit
        playerSitService.stopPlayerSit(player, GStopReason.GET_UP, false, true, false);
    }
}
