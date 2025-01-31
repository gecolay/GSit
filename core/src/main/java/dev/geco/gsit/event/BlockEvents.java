package dev.geco.gsit.event;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.GSeat;
import dev.geco.gsit.objects.GetUpReason;
import dev.geco.gsit.objects.IGPose;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.List;

public class BlockEvents implements Listener {

    private final GSitMain gSitMain;

    public BlockEvents(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockPistonExtendEvent(BlockPistonExtendEvent event) { handleBlockPistonEvent(event, event.getBlocks()); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockPistonRetractEvent(BlockPistonRetractEvent event) { handleBlockPistonEvent(event, event.getBlocks()); }

    private void handleBlockPistonEvent(BlockPistonEvent event, List<Block> blocks) {
        List<GSeat> moveList = new ArrayList<>();
        for(Block block : blocks) {
            if(gSitMain.getSitService().isBlockWithSeat(block)) {
                for(GSeat seat : gSitMain.getSitService().getSeatsByBlock(block)) {
                    if(moveList.contains(seat)) continue;
                    gSitMain.getSitService().moveSeat(seat.getEntity(), event.getDirection());
                    moveList.add(seat);
                }
            }
            if(!gSitMain.getConfigService().GET_UP_BREAK || !gSitMain.getPoseService().isBlockWithPose(block)) continue;
            for(IGPose poseSeat : gSitMain.getPoseService().getPosesByBlock(block)) gSitMain.getPoseService().removePose(poseSeat.getPlayer(), GetUpReason.BREAK);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockExplodeEvent(BlockExplodeEvent event) { handleExplodeEvent(event.blockList()); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityExplodeEvent(EntityExplodeEvent event) { handleExplodeEvent(event.blockList()); }

    private void handleExplodeEvent(List<Block> blocks) {
        if(!gSitMain.getConfigService().GET_UP_BREAK) return;
        for(Block block : new ArrayList<>(blocks)) {
            if(gSitMain.getSitService().isBlockWithSeat(block)) for(GSeat seat : gSitMain.getSitService().getSeatsByBlock(block)) if(!gSitMain.getSitService().removeSeat(seat.getEntity(), GetUpReason.BREAK)) blocks.remove(block);
            if(gSitMain.getPoseService().isBlockWithPose(block)) for(IGPose poseSeat : gSitMain.getPoseService().getPosesByBlock(block)) if(!gSitMain.getPoseService().removePose(poseSeat.getPlayer(), GetUpReason.BREAK)) blocks.remove(block);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockFadeEvent(BlockFadeEvent event) { handleBlockEvent(event, event.getBlock()); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void leavesDecayEvent(LeavesDecayEvent event) { handleBlockEvent(event, event.getBlock()); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockBurnEvent(BlockBurnEvent event) { handleBlockEvent(event, event.getBlock()); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityChangeBlockEvent(EntityChangeBlockEvent event) { handleBlockEvent(event, event.getBlock()); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockBreakEvent(BlockBreakEvent event) { handleBlockEvent(event, event.getBlock()); }

    private void handleBlockEvent(Cancellable event, Block block) {
        if(!gSitMain.getConfigService().GET_UP_BREAK) return;
        if(gSitMain.getSitService().isBlockWithSeat(block)) for(GSeat seat : gSitMain.getSitService().getSeatsByBlock(block)) if(!gSitMain.getSitService().removeSeat(seat.getEntity(), GetUpReason.BREAK)) event.setCancelled(true);
        if(gSitMain.getPoseService().isBlockWithPose(block) && !event.isCancelled()) for(IGPose poseSeat : gSitMain.getPoseService().getPosesByBlock(block)) if(!gSitMain.getPoseService().removePose(poseSeat.getPlayer(), GetUpReason.BREAK)) event.setCancelled(true);
    }

}