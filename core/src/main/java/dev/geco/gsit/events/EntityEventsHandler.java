package dev.geco.gsit.events;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.*;
import dev.geco.gsit.objects.*;

public class EntityEventsHandler {

    private final GSitMain GPM;

    public EntityEventsHandler(GSitMain GPluginMain) { GPM = GPluginMain; }

    public void handleEntityMountEvent(Cancellable Event, Entity Mount) {
        if(Event.isCancelled() && (Mount.getScoreboardTags().contains(GSitMain.NAME + "_SeatEntity") || Mount.getScoreboardTags().contains(GSitMain.NAME + "_PlayerSeatEntity"))) Event.setCancelled(false);
    }

    public void handleEntityDismountEvent(Cancellable Event, Entity Entity, Entity Dismounted) {

        if(Entity instanceof Player player) {

            if(GPM.getSitManager().isSitting(player) && (!GPM.getCManager().GET_UP_SNEAK || (!GPM.getSitManager().removeSeat(player, GetUpReason.GET_UP, true)))) {
                Event.setCancelled(true);
                return;
            }

            if(GPM.getPoseManager().isPosing(player) && (!GPM.getCManager().GET_UP_SNEAK || !GPM.getPoseManager().removePose(player, GetUpReason.GET_UP, true))) {
                Event.setCancelled(true);
                return;
            }
        }

        if(!Dismounted.getScoreboardTags().contains(GSitMain.NAME + "_PlayerSeatEntity") && !(Dismounted instanceof Player)) return;

        if(Entity instanceof Player player) {

            PrePlayerGetUpPlayerSitEvent preEvent = new PrePlayerGetUpPlayerSitEvent(player, GetUpReason.GET_UP);
            Bukkit.getPluginManager().callEvent(preEvent);
            if(preEvent.isCancelled()) {
                Event.setCancelled(true);
                return;
            }

            GPM.getPlayerSitManager().WAIT_EJECT.add(player);

            GPM.getTManager().runDelayed(() -> {
                GPM.getPlayerSitManager().WAIT_EJECT.remove(player);
            }, 2);
        }

        Entity bottom = GPM.getPassengerUtil().getBottomEntity(Dismounted);

        if(GPM.getCManager().PS_BOTTOM_RETURN && Entity.isValid() && Entity instanceof Player) GPM.getEntityUtil().setEntityLocation(Entity, bottom.getLocation());

        GPM.getPlayerSitManager().stopPlayerSit(Dismounted, GetUpReason.GET_UP);

        if(Entity instanceof Player) Bukkit.getPluginManager().callEvent(new PlayerGetUpPlayerSitEvent((Player) Entity, GetUpReason.GET_UP));
    }

}