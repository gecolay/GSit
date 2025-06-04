package dev.geco.gsit.event.feature;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.PlayerStopPoseEvent;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpinConfusionEventHandler implements Listener {

    private final GSitMain gSitMain;
    private final boolean spinConfusionEnabled;
    private final PotionEffect confusionEffect;

    public SpinConfusionEventHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        this.spinConfusionEnabled = gSitMain.getConfigService().FEATUREFLAGS.contains("SPIN_CONFUSION");
        
        PotionEffectType effectType = null;
        if (spinConfusionEnabled) {
            String effectName = gSitMain.getVersionManager().isNewerOrVersion(20, 5) ? "NAUSEA" : "CONFUSION";
            effectType = PotionEffectType.getByName(effectName);
            
            if (effectType == null) {
                gSitMain.getLogger().log(Level.SEVERE, "Effect type not found: " + effectName);
            }
        }
        this.confusionEffect = effectType != null ? 
            new PotionEffect(effectType, 120, 2) : null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerStopPoseEvent(PlayerStopPoseEvent event) {
        if (event.getPose().getPose() != Pose.SPIN_ATTACK || 
            !spinConfusionEnabled || 
            confusionEffect == null) return;
        
        event.getPlayer().addPotionEffect(confusionEffect);
    }
}
