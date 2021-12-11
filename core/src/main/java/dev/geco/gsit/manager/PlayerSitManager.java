package dev.geco.gsit.manager;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;

import net.md_5.bungee.api.ChatMessageType;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class PlayerSitManager implements IPlayerSitManager {

    private final GSitMain GPM;

    public PlayerSitManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    private int feature_used = 0;

    public int getFeatureUsedCount() { return feature_used; }

    public void resetFeatureUsedCount() { feature_used = 0; }

    public boolean sitOnPlayer(Player Player, Player Target) {

        AreaEffectCloud sa = Target.getWorld().spawn(Target.getLocation(), AreaEffectCloud.class, b -> {
            b.setDuration(Integer.MAX_VALUE);
            b.setParticle(Particle.BLOCK_CRACK, Material.AIR.createBlockData());
            b.setRadius(0);
            b.setWaitTime(0);
            b.setGravity(false);
            b.setInvulnerable(true);
        });

        if(sa.isValid()) {

            sa.addPassenger(Player);

        } else return false;

        Target.addPassenger(sa);

        if(GPM.getCManager().PS_SHOW_SIT_MESSAGE) Player.spigot().sendMessage(ChatMessageType.ACTION_BAR, GPM.getMManager().getComplexMessage(GPM.getMManager().getRawMessage("Messages.action-playersit-info")));

        sa.setMetadata(GPM.NAME + "A", new FixedMetadataValue(GPM, Player));

        feature_used++;

        return true;
    }

    public void stopSit(Entity Entity, GetUpReason Reason) {

        if(Entity.hasMetadata(GPM.NAME + "A")) {
            Entity.eject();
            Entity.remove();
        }

        for(Entity e : Entity.getPassengers()) {
            if(e.hasMetadata(GPM.NAME + "A")) {
                e.eject();
                e.remove();
            }
        }

        if(Entity.isInsideVehicle()) {
            Entity e = Entity.getVehicle();
            if(e.hasMetadata(GPM.NAME + "A")) {
                e.eject();
                e.remove();
            }
        }

    }

}