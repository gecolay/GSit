package dev.geco.gsit.event;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.object.GSeat;
import dev.geco.gsit.object.GStopReason;
import dev.geco.gsit.object.IGPose;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockEventHandler implements Listener {

    private final GSitMain gSitMain;

    public BlockEventHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockPistonExtendEvent(BlockPistonExtendEvent event) { handleBlockPistonEvent(event, event.getBlocks()); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockPistonRetractEvent(BlockPistonRetractEvent event) { handleBlockPistonEvent(event, event.getBlocks()); }

    private void handleBlockPistonEvent(BlockPistonEvent event, List<Block> blocks) {
        Set<GSeat> moveList = new HashSet<>();
        Set<IGPose> breakList = new HashSet<>();
        for(Block block : blocks) {
            moveList.addAll(gSitMain.getSitService().getSeatsByBlock(block));
            if(!gSitMain.getConfigService().GET_UP_BREAK) continue;
            breakList.addAll(gSitMain.getPoseService().getPosesByBlock(block));
        }
        for(GSeat seat : moveList) gSitMain.getSitService().moveSeat(seat, event.getDirection());
        for(IGPose poseSeat : breakList) gSitMain.getPoseService().removePose(poseSeat, GStopReason.BLOCK_BREAK);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockExplodeEvent(BlockExplodeEvent event) { handleExplodeEvent(event.blockList()); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityExplodeEvent(EntityExplodeEvent event) { handleExplodeEvent(event.blockList()); }

    private void handleExplodeEvent(List<Block> blocks) {
        if(!gSitMain.getConfigService().GET_UP_BREAK) return;
        blocks: for(Block block : new ArrayList<>(blocks)) {
            for(GSeat seat : gSitMain.getSitService().getSeatsByBlock(block)) if(!gSitMain.getSitService().removeSeat(seat, GStopReason.BLOCK_BREAK)) {
                blocks.remove(block);
                continue blocks;
            }
            for(IGPose poseSeat : gSitMain.getPoseService().getPosesByBlock(block)) if(!gSitMain.getPoseService().removePose(poseSeat, GStopReason.BLOCK_BREAK)) blocks.remove(block);
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
        for(GSeat seat : gSitMain.getSitService().getSeatsByBlock(block)) if(!gSitMain.getSitService().removeSeat(seat, GStopReason.BLOCK_BREAK)) event.setCancelled(true);
        if(!event.isCancelled()) for(IGPose poseSeat : gSitMain.getPoseService().getPosesByBlock(block)) if(!gSitMain.getPoseService().removePose(poseSeat, GStopReason.BLOCK_BREAK)) event.setCancelled(true);
    }

}