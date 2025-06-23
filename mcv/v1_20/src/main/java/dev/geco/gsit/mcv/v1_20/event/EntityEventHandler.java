package dev.geco.gsit.mcv.v1_20.event;

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

    @EventHandler(priority = EventPriority.HIGH)
    public void entityMountEvent(EntityMountEvent event) { gSitMain.getEntityEventHandler().handleEntityMountEvent(event, event.getEntity(), event.getMount()); }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void entityDismountEvent(EntityDismountEvent event) { gSitMain.getEntityEventHandler().handleEntityDismountEvent(event, event.getEntity(), event.getDismounted()); }

}