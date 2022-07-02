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

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class InteractEvents implements Listener {

    private final GSitMain GPM;

    public InteractEvents(GSitMain GPluginMain) { GPM = GPluginMain; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PIntE(PlayerInteractEvent e) {

        Block b = e.getClickedBlock();
        Action a = e.getAction();
        Player p = e.getPlayer();

        if(e.getHand() != EquipmentSlot.HAND || a != Action.RIGHT_CLICK_BLOCK) return;

        if(e.getBlockFace() != BlockFace.UP) return;

        if(GPM.getCManager().S_EMPTY_HAND_ONLY && e.getItem() != null) return;

        if(b == null || !GPM.getPManager().hasNormalPermission(p, "SitClick")) return;

        if(!GPM.getCManager().S_SITMATERIALS.containsKey(b.getType())) return;

        if(GPM.getCManager().MATERIALBLACKLIST.contains(b.getType())) return;

        if(GPM.getCManager().WORLDBLACKLIST.contains(p.getWorld().getName()) && !GPM.getPManager().hasPermission(p, "ByPass.World", "ByPass.*")) return;

        if(!p.isValid() || !p.isOnGround() || p.isSneaking() || p.isInsideVehicle()) return;

        if(!GPM.getToggleManager().canSit(p.getUniqueId())) return;

        if(GPM.getCrawlManager() != null && GPM.getCrawlManager().isCrawling(p)) return;

        double d = GPM.getCManager().S_MAX_DISTANCE;

        if(d > 0d && b.getLocation().add(0.5, 0.5, 0.5).distance(p.getLocation()) > d) return;

        if(!GPM.getCManager().ALLOW_UNSAFE && !(b.getRelative(BlockFace.UP).isPassable())) return;

        if(GPM.getWorldGuardLink() != null && !GPM.getWorldGuardLink().checkFlag(b.getLocation(), GPM.getWorldGuardLink().SIT_FLAG)) return;

        if(GPM.getGriefPreventionLink() != null && !GPM.getGriefPreventionLink().check(b.getLocation(), p)) return;

        if(!GPM.getCManager().SAME_BLOCK_REST && !GPM.getSitManager().kickSeat(b, p)) return;

        if(Tag.STAIRS.isTagged(b.getType())) {

            Stairs bd = (Stairs) b.getBlockData();

            if(bd.getHalf() != Half.BOTTOM) return;

            GSeat seat = GPM.getSitUtil().createSeatForStair(b, p);

            if(seat != null) {

                e.setCancelled(true);

                return;
            }

        } else if(Tag.SLABS.isTagged(b.getType())) {

            Slab bd = (Slab) b.getBlockData();

            if(bd.getType() != Type.BOTTOM) return;
        }

        GSeat seat = GPM.getSitManager().createSeat(b, p, true, 0d, 0d, 0d, p.getLocation().getYaw(), true, GPM.getCManager().GET_UP_SNEAK);

        if(seat != null) e.setCancelled(true);
    }

}