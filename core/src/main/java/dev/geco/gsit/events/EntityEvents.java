package dev.geco.gsit.events;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.*;
import dev.geco.gsit.objects.*;

public class EntityEvents implements Listener {

    private final GSitMain GPM;

    public EntityEvents(GSitMain GPluginMain) { GPM = GPluginMain; }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void EMouE(EntityMountEvent Event) { if(Event.isCancelled() && (Event.getMount().getScoreboardTags().contains(GPM.NAME + "_SeatEntity") || Event.getMount().getScoreboardTags().contains(GPM.NAME + "_PlayerSeatEntity"))) Event.setCancelled(false); }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void EDisE(EntityDismountEvent Event) {

        if(Event.getEntity() instanceof Player) {

            Player player = (Player) Event.getEntity();

            if (GPM.getSitManager().isSitting(player) && (!GPM.getCManager().GET_UP_SNEAK || (!GPM.getSitManager().removeSeat(player, GetUpReason.GET_UP, true)))) {

                Event.setCancelled(true);
                return;
            }

            if (GPM.getPoseManager().isPosing(player) && (!GPM.getCManager().GET_UP_SNEAK || !GPM.getPoseManager().removePose(player, GetUpReason.GET_UP, true))) {

                Event.setCancelled(true);
                return;
            }
        }

        if(!Event.getDismounted().getScoreboardTags().contains(GPM.NAME + "_PlayerSeatEntity") && !(Event.getDismounted() instanceof Player)) return;

        if(Event.getEntity() instanceof Player) {

            Player player = (Player) Event.getEntity();

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

        Entity bottom = GPM.getPassengerUtil().getBottomEntity(Event.getDismounted());

        if(GPM.getCManager().PS_BOTTOM_RETURN && Event.getEntity().isValid() && Event.getEntity() instanceof Player && GPM.getPackageUtil() != null) GPM.getEntityUtil().posEntity(Event.getEntity(), bottom.getLocation());

        if(Event.getDismounted().getScoreboardTags().contains(GPM.NAME + "_PlayerSeatEntity") && GPM.getPackageUtil() == null) GPM.getEntityUtil().posEntity(Event.getDismounted(), bottom.getLocation());

        GPM.getPlayerSitManager().stopPlayerSit(Event.getDismounted(), GetUpReason.GET_UP);

        if(Event.getEntity() instanceof Player) Bukkit.getPluginManager().callEvent(new PlayerGetUpPlayerSitEvent((Player) Event.getEntity(), GetUpReason.GET_UP));
    }

}