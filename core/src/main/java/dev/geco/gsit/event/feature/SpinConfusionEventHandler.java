package dev.geco.gsit.event.feature;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.PlayerStopPoseEvent;
import dev.geco.gsit.model.PoseType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpinConfusionEventHandler implements Listener {

    private final GSitMain gSitMain;

    public SpinConfusionEventHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerStopPoseEvent(PlayerStopPoseEvent event) {
        if(event.getPose().getPoseType() != PoseType.SPIN) return;
        if(!gSitMain.getConfigService().FEATUREFLAGS.contains("SPIN_CONFUSION")) return;
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.getByName(gSitMain.getVersionManager().isNewerOrVersion(20, 5) ? "NAUSEA" : "CONFUSION"), 120, 2));
    }

}