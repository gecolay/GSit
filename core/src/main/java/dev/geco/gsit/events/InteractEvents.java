package dev.geco.gsit.events;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.*;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.*;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.block.data.type.Stairs.Shape;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.BoundingBox;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.manager.*;

public class InteractEvents implements Listener {
    
    private final GSitMain GPM;
    
    public InteractEvents(GSitMain GPluginMain) { GPM = GPluginMain; }
    
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PIntE(PlayerInteractEvent e) {
        
        Block b = e.getClickedBlock();
        Action a = e.getAction();
        Player p = e.getPlayer();
        
        if(e.getHand() != EquipmentSlot.HAND || a != Action.RIGHT_CLICK_BLOCK) return;
        
        if(e.getBlockFace() != BlockFace.UP) return;

        if(GPM.getCManager().S_EMPTY_HAND_ONLY && e.getItem() != null) return;
        
        if(!GPM.getCManager().S_SITMATERIALS.containsKey(b.getType())) return;
        
        if(GPM.getCManager().MATERIALBLACKLIST.contains(b.getType())) return;
        
        if(GPM.getCManager().WORLDBLACKLIST.contains(p.getWorld().getName())) return;
        
        if(!GPM.getPManager().hasNormalPermission(p, "SitClick")) return;
        
        if(!p.isValid() || !p.isOnGround() || p.isSneaking() || p.isInsideVehicle()) return;
        
        if(!GPM.getToggleManager().canSit(p.getUniqueId())) return;
        
        double d = GPM.getCManager().S_MAX_DISTANCE;
        
        BoundingBox bb = b.getBoundingBox();
        
        if(d > 0d && b.getLocation().add(0.5, bb.getHeight(), 0.5).distance(p.getLocation()) > d) return;
        
        Material u = b.getRelative(BlockFace.UP).getType();
        
        if(!GPM.getCManager().S_ALLOW_UNSAFE && !u.isTransparent() && u != Material.WATER && !u.name().equalsIgnoreCase("LIGHT")) return;

        if(GPM.getPlotSquared() != null && !GPM.getPlotSquared().canCreateSeat(b.getLocation(), p)) return;

        if(GPM.getWorldGuard() != null && !GPM.getWorldGuard().checkFlag(b.getLocation(), GPM.getWorldGuard().SIT_FLAG)) return;
        
        if(!GPM.getCManager().REST_SAME_BLOCK && !GPM.getSitManager().kickSeat(b, p)) return;
        
        if(Tag.STAIRS.isTagged(b.getType())) {
            
            Stairs bd = (Stairs) b.getBlockData();
            
            if(bd.getHalf() != Half.BOTTOM) return;
            
            BlockFace f = bd.getFacing().getOppositeFace();
            
            Shape s = bd.getShape();
            
            switch(bd.getShape()) {
            case STRAIGHT:
                switch(f) {
                case EAST:
                    GPM.getSitManager().createSeat(b, p, false, SitManager.STAIR_OFFSET, (bb.getMaxY() / 2d), 0d, -90f, true);
                    break;
                case SOUTH:
                    GPM.getSitManager().createSeat(b, p, false, 0d, (bb.getMaxY() / 2d), SitManager.STAIR_OFFSET, 0f, true);
                    break;
                case WEST:
                    GPM.getSitManager().createSeat(b, p, false, -SitManager.STAIR_OFFSET, (bb.getMaxY() / 2d), 0d, 90f, true);
                    break;
                case NORTH:
                    GPM.getSitManager().createSeat(b, p, false, 0d, (bb.getMaxY() / 2d), -SitManager.STAIR_OFFSET, 180f, true);
                    break;
                default:
                    break;
                }
                break;
            default:
                if(f == BlockFace.NORTH && s == Shape.OUTER_RIGHT || f == BlockFace.EAST && s == Shape.OUTER_LEFT || f == BlockFace.NORTH && s == Shape.INNER_RIGHT || f == BlockFace.EAST && s == Shape.INNER_LEFT) {
                    
                    GPM.getSitManager().createSeat(b, p, false, SitManager.STAIR_OFFSET, ((b.getBoundingBox().getMinY() + b.getBoundingBox().getHeight() - b.getY())/ 2d), -SitManager.STAIR_OFFSET, -135f, true);
                    
                } else if(f == BlockFace.NORTH && s == Shape.OUTER_LEFT || f == BlockFace.WEST && s == Shape.OUTER_RIGHT || f == BlockFace.NORTH && s == Shape.INNER_LEFT || f == BlockFace.WEST && s == Shape.INNER_RIGHT) {
                    
                    GPM.getSitManager().createSeat(b, p, false, -SitManager.STAIR_OFFSET, ((b.getBoundingBox().getMinY() + b.getBoundingBox().getHeight() - b.getY()) / 2d), -SitManager.STAIR_OFFSET, 135f, true);
                    
                } else if(f == BlockFace.SOUTH && s == Shape.OUTER_RIGHT || f == BlockFace.WEST && s == Shape.OUTER_LEFT || f == BlockFace.SOUTH && s == Shape.INNER_RIGHT || f == BlockFace.WEST && s == Shape.INNER_LEFT) {
                    
                    GPM.getSitManager().createSeat(b, p, false, -SitManager.STAIR_OFFSET, ((b.getBoundingBox().getMinY() + b.getBoundingBox().getHeight() - b.getY()) / 2d), SitManager.STAIR_OFFSET, 45f, true);
                    
                } else if(f == BlockFace.SOUTH && s == Shape.OUTER_LEFT || f == BlockFace.EAST && s == Shape.OUTER_RIGHT || f == BlockFace.SOUTH && s == Shape.INNER_LEFT || f == BlockFace.EAST && s == Shape.INNER_RIGHT) {
                    
                    GPM.getSitManager().createSeat(b, p, false, SitManager.STAIR_OFFSET, ((b.getBoundingBox().getMinY() + b.getBoundingBox().getHeight() - b.getY()) / 2d), SitManager.STAIR_OFFSET, -45f, true);
                    
                }
                break;
            }
            
            e.setCancelled(true);
            
        } else if(Tag.SLABS.isTagged(b.getType())) {
            
            Slab bd = (Slab) b.getBlockData();
            
            if(bd.getType() != Type.BOTTOM) return;
            
            GPM.getSitManager().createSeat(b, p, true, 0d, b.getBoundingBox().getMinY() + b.getBoundingBox().getHeight() - b.getY(), 0d, p.getLocation().getYaw(), true);
            
            e.setCancelled(true);
            
        } else {

            GPM.getSitManager().createSeat(b, p, true, 0d, b.getBoundingBox().getMinY() + b.getBoundingBox().getHeight() - b.getY(), 0d, p.getLocation().getYaw(), true);
            
            e.setCancelled(true);
            
        }
        
    }
    
}