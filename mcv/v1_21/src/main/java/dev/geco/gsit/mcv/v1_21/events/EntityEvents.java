package dev.geco.gsit.mcv.v1_21.events;

import dev.geco.gsit.GSitMain;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;

public class EntityEvents implements Listener {

    private final GSitMain gSitMain;

    public EntityEvents(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void entityMountEvent(EntityMountEvent event) { gSitMain.getEntityEventsHandler().handleEntityMountEvent(event, event.getMount()); }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void entityDismountEvent(EntityDismountEvent event) { gSitMain.getEntityEventsHandler().handleEntityDismountEvent(event, event.getEntity(), event.getDismounted()); }

}