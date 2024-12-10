package dev.geco.gsit.events;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.Bisected.*;
import org.bukkit.block.data.type.*;
import org.bukkit.block.data.type.Slab.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.util.*;

import dev.geco.gsit.GSitMain;

public class InteractEvents implements Listener {

    private final GSitMain GPM;

    public InteractEvents(GSitMain GPluginMain) { GPM = GPluginMain; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PIntE(PlayerInteractEvent Event) {

        Action action = Event.getAction();
        if(Event.getHand() != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return;

        if(Event.getBlockFace() != BlockFace.UP) return;

        if(GPM.getCManager().S_EMPTY_HAND_ONLY && Event.getItem() != null) return;

        Player player = Event.getPlayer();
        Block clickedBlock = Event.getClickedBlock();
        if(clickedBlock == null || !GPM.getPManager().hasPermission(player, "SitClick", "Sit.*")) return;

        if(!GPM.getCManager().S_SITMATERIALS.containsKey(clickedBlock.getType()) && !GPM.getCManager().S_SITMATERIALS.containsKey(Material.AIR)) return;

        if(GPM.getCManager().MATERIALBLACKLIST.contains(clickedBlock.getType())) return;

        if(!GPM.getEnvironmentUtil().isInAllowedWorld(player)) return;

        if(!player.isValid() || player.isSneaking()) return;

        if(GPM.getSitManager().isSitting(player) || GPM.getPoseManager().isPosing(player) || GPM.getCrawlManager().isCrawling(player)) return;

        double distance = GPM.getCManager().S_MAX_DISTANCE;
        Location location = clickedBlock.getLocation();
        if(distance > 0d && location.clone().add(0.5, 0.5, 0.5).distance(player.getLocation()) > distance) return;

        if(!GPM.getCManager().ALLOW_UNSAFE && !(clickedBlock.getRelative(BlockFace.UP).isPassable())) return;

        if(GPM.getPlotSquaredLink() != null && !GPM.getPlotSquaredLink().canCreatePlayerSeat(location, player)) return;

        if(GPM.getWorldGuardLink() != null && !GPM.getWorldGuardLink().checkFlag(location, GPM.getWorldGuardLink().getFlag("sit"))) return;

        if(GPM.getGriefPreventionLink() != null && !GPM.getGriefPreventionLink().check(location, player)) return;

        if(!GPM.getCManager().SAME_BLOCK_REST && !GPM.getSitManager().kickSeat(clickedBlock, player)) return;

        if(!GPM.getToggleManager().canSit(player.getUniqueId())) return;

        if(Tag.STAIRS.isTagged(clickedBlock.getType())) {

            if(((Stairs) clickedBlock.getBlockData()).getHalf() == Half.BOTTOM) {

                if(GPM.getEnvironmentUtil().createSeatForStair(clickedBlock, player) != null) {

                    Event.setCancelled(true);
                    return;
                }
            } else if(GPM.getCManager().S_BOTTOM_PART_ONLY) return;
        } else if(Tag.SLABS.isTagged(clickedBlock.getType())) {

            if(((Slab) clickedBlock.getBlockData()).getType() != Type.BOTTOM && GPM.getCManager().S_BOTTOM_PART_ONLY) return;
        }

        boolean interactionPointAvailable = GPM.getCManager().CENTER_BLOCK;

        double xoffset = interactionPointAvailable ? 0 : -0.5d;
        double zoffset = interactionPointAvailable ? 0 : -0.5d;

        if(!interactionPointAvailable) {
            try {
                Vector interactionPointVector = Event.getClickedPosition();
                if(interactionPointVector != null) {
                    interactionPointAvailable = true;
                    xoffset += interactionPointVector.getX() - interactionPointVector.getBlockX();
                    zoffset += interactionPointVector.getZ() - interactionPointVector.getBlockZ();
                }
            } catch (Throwable ignored) { }
        }

        if(!interactionPointAvailable) {
            try {
                Location interactionPoint = Event.getInteractionPoint();
                if(interactionPoint != null) {
                    interactionPointAvailable = true;
                    xoffset += interactionPoint.getX() - interactionPoint.getBlockX();
                    zoffset += interactionPoint.getZ() - interactionPoint.getBlockZ();
                }
            } catch (Throwable ignored) { }
        }

        if(GPM.getSitManager().createSeat(clickedBlock, player, true, interactionPointAvailable ? xoffset : 0d, 0d, interactionPointAvailable ? zoffset : 0, player.getLocation().getYaw(), true) != null) Event.setCancelled(true);
    }

}