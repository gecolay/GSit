package dev.geco.gsit.events.features;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.potion.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.*;

public class SpinConfusionEvent implements Listener {

    private final GSitMain GPM;

    public SpinConfusionEvent(GSitMain GPluginMain) { GPM = GPluginMain; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PGetUPE(PlayerGetUpPoseEvent Event) {

        if(Event.getPoseSeat().getPose() != Pose.SPIN_ATTACK) return;

        if(!GPM.getCManager().FEATUREFLAGS.contains("SPIN_CONFUSION")) return;

        Event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.getByName(GPM.getSVManager().isNewerOrVersion(20, 5) ? "NAUSEA" : "CONFUSION"), 120, 2));
    }

}