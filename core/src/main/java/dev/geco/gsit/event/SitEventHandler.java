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

import java.util.Map;
import java.util.Set;

public class SitEventHandler implements Listener {

    private final GSitMain gSitMain;
    private final Attribute blockInteractionRangeAttribute;
    private final boolean emptyHandOnly;
    private final Set<Material> materialBlacklist;
    private final Map<Material, ?> sitMaterials;
    private final double maxDistance;
    private final boolean allowUnsafe;
    private final boolean centerBlock;
    private final boolean bottomPartOnly;
    private final boolean sameBlockRest;

    public SitEventHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        this.blockInteractionRangeAttribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("block_interaction_range"));
        
        var config = gSitMain.getConfigService();
        this.emptyHandOnly = config.S_EMPTY_HAND_ONLY;
        this.materialBlacklist = config.MATERIALBLACKLIST;
        this.sitMaterials = config.S_SITMATERIALS;
        this.maxDistance = config.S_MAX_DISTANCE;
        this.allowUnsafe = config.ALLOW_UNSAFE;
        this.centerBlock = config.CENTER_BLOCK;
        this.bottomPartOnly = config.S_BOTTOM_PART_ONLY;
        this.sameBlockRest = config.SAME_BLOCK_REST;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerInteractEvent(PlayerInteractEvent event) {
        // Early exit for irrelevant events
        if (event.getHand() != EquipmentSlot.HAND || 
            event.getAction() != Action.RIGHT_CLICK_BLOCK || 
            event.getBlockFace() != BlockFace.UP) {
            return;
        }
        
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        
        // Validate player state and permissions
        if (!isPlayerValidForSitting(event, player)) return;
        
        // Validate block material
        if (!isValidSitMaterial(clickedBlock)) return;
        
        // Check world and location permissions
        if (!gSitMain.getEnvironmentUtil().isEntityInAllowedWorld(player) || 
            !gSitMain.getEnvironmentUtil().canUseInLocation(clickedBlock.getLocation(), player, "sit")) {
            return;
        }
        
        // Distance check
        if (maxDistance > 0d && calculateDistance(clickedBlock, player) > maxDistance) return;
        
        // Block safety check
        if (!allowUnsafe && !clickedBlock.getRelative(BlockFace.UP).isPassable()) return;
        
        // Ray trace validation
        if (!isValidRayTrace(player, clickedBlock)) return;
        
        // Toggle permission
        if (!gSitMain.getToggleService().canEntityUseSit(player.getUniqueId())) return;
        
        // Handle same block restriction
        if (!sameBlockRest && !gSitMain.getSitService().kickSeatEntitiesFromBlock(clickedBlock, player)) return;
        
        // Handle special block types (stairs/slabs)
        if (handleSpecialBlockTypes(event, clickedBlock, player)) return;
        
        // Calculate offsets
        double[] offsets = calculateOffsets(event, clickedBlock);
        
        // Create seat
        if (gSitMain.getSitService().createSeat(
            clickedBlock, 
            player, 
            true, 
            offsets[0], 
            0d, 
            offsets[1], 
            player.getLocation().getYaw(), 
            true
        ) != null) {
            event.setCancelled(true);
        }
    }
    
    private boolean isPlayerValidForSitting(PlayerInteractEvent event, Player player) {
        return !emptyHandOnly || event.getItem() == null &&
               gSitMain.getPermissionService().hasPermission(player, "SitClick", "Sit.*") &&
               player.isValid() &&
               !player.isSneaking() &&
               !gSitMain.getSitService().isEntityBlocked(player) &&
               !gSitMain.getSitService().isEntitySitting(player) &&
               !gSitMain.getPoseService().isPlayerPosing(player) &&
               !gSitMain.getCrawlService().isPlayerCrawling(player);
    }
    
    private boolean isValidSitMaterial(Block block) {
        Material type = block.getType();
        return !materialBlacklist.contains(type) && 
               (sitMaterials.containsKey(type) || sitMaterials.containsKey(Material.AIR));
    }
    
    private double calculateDistance(Block clickedBlock, Player player) {
        Location blockCenter = clickedBlock.getLocation().add(0.5, 0.5, 0.5);
        return blockCenter.distance(player.getLocation());
    }
    
    private boolean isValidRayTrace(Player player, Block clickedBlock) {
        if (blockInteractionRangeAttribute == null) return true;
        
        double range = player.getAttribute(blockInteractionRangeAttribute).getValue();
        RayTraceResult targetRayTrack = player.rayTraceBlocks(range);
        
        if (targetRayTrack == null) return true;
        
        BlockFace hitFace = targetRayTrack.getHitBlockFace();
        Block hitBlock = targetRayTrack.getHitBlock();
        
        return hitFace == BlockFace.UP && clickedBlock.equals(hitBlock);
    }
    
    private boolean handleSpecialBlockTypes(PlayerInteractEvent event, Block clickedBlock, Player player) {
        Material type = clickedBlock.getType();
        
        if (Tag.STAIRS.isTagged(type)) {
            Stairs stairs = (Stairs) clickedBlock.getBlockData();
            if (stairs.getHalf() == Bisected.Half.BOTTOM) {
                if (gSitMain.getSitService().createStairSeatForEntity(clickedBlock, player) != null) {
                    event.setCancelled(true);
                    return true;
                }
            } else if (bottomPartOnly) {
                return true;
            }
        } 
        else if (Tag.SLABS.isTagged(type)) {
            Slab slab = (Slab) clickedBlock.getBlockData();
            if (slab.getType() != Slab.Type.BOTTOM && bottomPartOnly) {
                return true;
            }
        }
        return false;
    }
    
    private double[] calculateOffsets(PlayerInteractEvent event, Block clickedBlock) {
        double xoffset = centerBlock ? 0 : -0.5d;
        double zoffset = centerBlock ? 0 : -0.5d;
        
        if (!centerBlock) {
            Vector interactionPointVector = event.getClickedPosition();
            if (interactionPointVector != null) {
                xoffset += interactionPointVector.getX() - clickedBlock.getX();
                zoffset += interactionPointVector.getZ() - clickedBlock.getZ();
            } else {
                Location interactionPoint = event.getInteractionPoint();
                if (interactionPoint != null) {
                    xoffset += interactionPoint.getX() - clickedBlock.getX();
                    zoffset += interactionPoint.getZ() - clickedBlock.getZ();
                }
            }
        }
        
        return new double[]{xoffset, zoffset};
    }
}
