package dev.geco.gsit.mcv.v1_19_4.events;

import org.bukkit.event.*;

import org.spigotmc.event.entity.*;

import dev.geco.gsit.GSitMain;

public class EntityEvents implements Listener {

    private final GSitMain GPM;

    public EntityEvents(GSitMain GPluginMain) { GPM = GPluginMain; }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void EMouE(EntityMountEvent Event) { GPM.getEntityEventsHandler().handleEntityMountEvent(Event, Event.getMount()); }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void EDisE(EntityDismountEvent Event) { GPM.getEntityEventsHandler().handleEntityDismountEvent(Event, Event.getEntity(), Event.getDismounted()); }

}