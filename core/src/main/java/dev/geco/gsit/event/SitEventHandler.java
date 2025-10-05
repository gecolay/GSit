package dev.geco.gsit.event;

import dev.geco.gsit.GSitMain;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class SitEventHandler implements Listener {

    private final GSitMain gSitMain;
    private final Attribute blockInteractionRangeAttribute;

    public SitEventHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        blockInteractionRangeAttribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("block_interaction_range"));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerInteractEvent(PlayerInteractEvent event) {
        Action action = event.getAction();
        if(event.getHand() != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return;

        if(event.getBlockFace() != BlockFace.UP) return;

        if(gSitMain.getConfigService().S_EMPTY_HAND_ONLY && event.getItem() != null) return;

        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        if(clickedBlock == null || !gSitMain.getPermissionService().hasPermission(player, "SitClick", "Sit.*")) return;

        if(!gSitMain.getConfigService().S_SITMATERIALS.containsKey(clickedBlock.getType()) && !gSitMain.getConfigService().S_SITMATERIALS.containsKey(Material.AIR)) return;

        if(gSitMain.getConfigService().MATERIALBLACKLIST.contains(clickedBlock.getType())) return;

        if(!gSitMain.getEnvironmentUtil().isEntityInAllowedWorld(player)) return;

        if(!player.isValid() || player.isSneaking()) return;

        if(gSitMain.getSitService().isEntityBlocked(player)) return;

        if(gSitMain.getSitService().isEntitySitting(player) || gSitMain.getPoseService().isPlayerPosing(player) || gSitMain.getCrawlService().isPlayerCrawling(player)) return;

        double distance = gSitMain.getConfigService().S_MAX_DISTANCE;
        Location location = clickedBlock.getLocation();
        if(distance > 0d && location.clone().add(0.5, 0.5, 0.5).distanceSquared(player.getLocation()) > distance * distance) return;

        if(!gSitMain.getConfigService().ALLOW_UNSAFE && !(clickedBlock.getRelative(BlockFace.UP).isPassable())) return;

        if(!gSitMain.getEnvironmentUtil().canUseInLocation(location, player, "sit")) return;

        RayTraceResult targetRayTrack = player.rayTraceBlocks(blockInteractionRangeAttribute != null ? player.getAttribute(blockInteractionRangeAttribute).getValue() : 5);
        BlockFace targetBlockFace = targetRayTrack != null ? targetRayTrack.getHitBlockFace() : null;
        if(targetBlockFace != null && targetBlockFace != BlockFace.UP) return;
        Block targetBlock = targetRayTrack != null ? targetRayTrack.getHitBlock() : null;
        if(targetBlock != null && !clickedBlock.equals(targetBlock)) return;

        if(!gSitMain.getToggleService().canEntityUseSit(player.getUniqueId())) return;

        if(!gSitMain.getConfigService().SAME_BLOCK_REST && !gSitMain.getSitService().kickSeatEntitiesFromBlock(clickedBlock, player)) return;

        if(Tag.STAIRS.isTagged(clickedBlock.getType())) {

            if(((Stairs) clickedBlock.getBlockData()).getHalf() == Bisected.Half.BOTTOM) {

                if(gSitMain.getSitService().createStairSeatForEntity(clickedBlock, player) != null) {

                    event.setCancelled(true);
                    return;
                }
            } else if(gSitMain.getConfigService().S_BOTTOM_PART_ONLY) return;
        } else if(Tag.SLABS.isTagged(clickedBlock.getType())) {

            if(((Slab) clickedBlock.getBlockData()).getType() != Slab.Type.BOTTOM && gSitMain.getConfigService().S_BOTTOM_PART_ONLY) return;
        }

        boolean useCenter = gSitMain.getConfigService().CENTER_BLOCK;

        double xoffset = useCenter ? 0 : -0.5d;
        double zoffset = xoffset;

        if(!useCenter) {
            try {
                Vector interactionPointVector = event.getClickedPosition();
                if(interactionPointVector != null) {
                    useCenter = true;
                    xoffset += interactionPointVector.getX() - interactionPointVector.getBlockX();
                    zoffset += interactionPointVector.getZ() - interactionPointVector.getBlockZ();
                }
            } catch(Throwable ignored) { }
        }

        if(!useCenter) {
            try {
                Location interactionPoint = event.getInteractionPoint();
                if(interactionPoint != null) {
                    useCenter = true;
                    xoffset += interactionPoint.getX() - interactionPoint.getBlockX();
                    zoffset += interactionPoint.getZ() - interactionPoint.getBlockZ();
                }
            } catch(Throwable ignored) { }
        }

        if(gSitMain.getSitService().createSeat(clickedBlock, player, true, useCenter ? xoffset : 0d, 0d, useCenter ? zoffset : 0, player.getLocation().getYaw(), true) != null) event.setCancelled(true);
    }

}