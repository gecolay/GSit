package dev.geco.gsit.mcv.v1_20_3.event;

import dev.geco.gsit.GSitMain;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

public class EntityEventHandler implements Listener {

    private final GSitMain gSitMain;

    public EntityEventHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void entityMountEventLow(org.bukkit.event.entity.EntityMountEvent event) { gSitMain.getEntityEventHandler().handleEntityMountEventLow(event, event.getMount()); }

    @EventHandler(priority = EventPriority.HIGH)
    @SuppressWarnings("deprecation")
    public void entityMountEventHigh(EntityMountEvent event) { gSitMain.getEntityEventHandler().handleEntityMountEventHigh(event, event.getMount()); }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("deprecation")
    public void entityDismountEvent(EntityDismountEvent event) { gSitMain.getEntityEventHandler().handleEntityDismountEvent(event, event.getEntity(), event.getDismounted()); }

}