package dev.geco.gsit.mcv.v1_21_9.event;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.service.SitService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class EntityEventHandler implements Listener {

    private final GSitMain gSitMain;

    public EntityEventHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void entityMountEventLow(EntityMountEvent event) {
        if(gSitMain.getWorldGuardLink() == null) return;
        if(!(event.getEntity() instanceof Player player)) return;
        if(!event.getMount().getScoreboardTags().contains(SitService.SIT_TAG)) return;
        player.setMetadata("NPC", new FixedMetadataValue(gSitMain, null));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void entityMountEventHigh(EntityMountEvent event) {
        if(gSitMain.getWorldGuardLink() == null) return;
        if(!(event.getEntity() instanceof Player player)) return;
        if(!event.getMount().getScoreboardTags().contains(SitService.SIT_TAG)) return;
        if(!player.hasMetadata("NPC")) return;
        player.removeMetadata("NPC", gSitMain);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void entityDismountEvent(EntityDismountEvent event) { gSitMain.getEntityEventHandler().handleEntityDismountEvent(event, event.getEntity(), event.getDismounted()); }

}