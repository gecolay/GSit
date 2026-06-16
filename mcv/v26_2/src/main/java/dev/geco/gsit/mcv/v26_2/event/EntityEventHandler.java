package dev.geco.gsit.mcv.v26_2.event;

import dev.geco.gsit.GSitMain;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;

public class EntityEventHandler implements Listener {

    private final GSitMain gSitMain;

    public EntityEventHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void entityMountEventLow(EntityMountEvent event) { gSitMain.getEntityEventHandler().handleEntityMountEventLow(event, event.getMount()); }

    @EventHandler(priority = EventPriority.HIGH)
    public void entityMountEventHigh(EntityMountEvent event) { gSitMain.getEntityEventHandler().handleEntityMountEventHigh(event, event.getMount()); }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void entityDismountEvent(EntityDismountEvent event) { gSitMain.getEntityEventHandler().handleEntityDismountEvent(event, event.getEntity(), event.getDismounted()); }

}