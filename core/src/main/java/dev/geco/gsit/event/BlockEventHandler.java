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
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.*;

public class BlockEventHandler implements Listener {

    private final GSitMain gSitMain;
    private final boolean getUpBreakEnabled;

    public BlockEventHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        this.getUpBreakEnabled = gSitMain.getConfigService().GET_UP_BREAK;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockPistonExtendEvent(BlockPistonExtendEvent event) {
        handleBlockPistonEvent(event, event.getBlocks());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockPistonRetractEvent(BlockPistonRetractEvent event) {
        handleBlockPistonEvent(event, event.getBlocks());
    }

    private void handleBlockPistonEvent(BlockPistonEvent event, List<Block> blocks) {
        if (!getUpBreakEnabled) return;
        
        final var sitService = gSitMain.getSitService();
        final var poseService = gSitMain.getPoseService();
        final Set<GSeat> movedSeats = new HashSet<>();
        
        for (Block block : blocks) {
            // Process seats
            Collection<GSeat> seats = sitService.getSeatsByBlock(block);
            if (seats != null) {
                for (GSeat seat : seats) {
                    if (movedSeats.contains(seat)) continue;
                    sitService.moveSeat(seat, event.getDirection());
                    movedSeats.add(seat);
                }
            }
            
            // Process poses
            Collection<IGPose> poses = poseService.getPosesByBlock(block);
            if (poses != null) {
                for (IGPose pose : poses) {
                    poseService.removePose(pose, GStopReason.BLOCK_BREAK);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockExplodeEvent(BlockExplodeEvent event) {
        handleExplodeEvent(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityExplodeEvent(EntityExplodeEvent event) {
        handleExplodeEvent(event.blockList());
    }

    private void handleExplodeEvent(List<Block> blocks) {
        if (!getUpBreakEnabled || blocks == null || blocks.isEmpty()) return;
        
        final var sitService = gSitMain.getSitService();
        final var poseService = gSitMain.getPoseService();
        final Iterator<Block> iterator = blocks.iterator();
        
        while (iterator.hasNext()) {
            Block block = iterator.next();
            boolean shouldRemoveBlock = false;
            
            // Check seats
            Collection<GSeat> seats = sitService.getSeatsByBlock(block);
            if (seats != null && !seats.isEmpty()) {
                for (GSeat seat : seats) {
                    if (!sitService.removeSeat(seat, GStopReason.BLOCK_BREAK)) {
                        shouldRemoveBlock = true;
                        break;
                    }
                }
            }
            
            // Check poses (only if block not already marked for removal)
            if (!shouldRemoveBlock) {
                Collection<IGPose> poses = poseService.getPosesByBlock(block);
                if (poses != null && !poses.isEmpty()) {
                    for (IGPose pose : poses) {
                        if (!poseService.removePose(pose, GStopReason.BLOCK_BREAK)) {
                            shouldRemoveBlock = true;
                            break;
                        }
                    }
                }
            }
            
            if (shouldRemoveBlock) {
                iterator.remove();
            }
        }
    }

    // Block event handlers
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockFadeEvent(BlockFadeEvent event) {
        handleBlockEvent(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void leavesDecayEvent(LeavesDecayEvent event) {
        handleBlockEvent(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockBurnEvent(BlockBurnEvent event) {
        handleBlockEvent(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityChangeBlockEvent(EntityChangeBlockEvent event) {
        handleBlockEvent(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockBreakEvent(BlockBreakEvent event) {
        handleBlockEvent(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockFormEvent(BlockFormEvent event) {
        handleBlockEvent(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockFromToEvent(BlockFromToEvent event) {
        handleBlockEvent(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockGrowEvent(BlockGrowEvent event) {
        handleBlockEvent(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockSpreadEvent(BlockSpreadEvent event) {
        handleBlockEvent(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void moistureChangeEvent(MoistureChangeEvent event) {
        handleBlockEvent(event, event.getBlock());
    }

    private void handleBlockEvent(Cancellable event, Block block) {
        if (!getUpBreakEnabled || event.isCancelled()) return;
        
        final var sitService = gSitMain.getSitService();
        final var poseService = gSitMain.getPoseService();
        
        // Process seats
        Collection<GSeat> seats = sitService.getSeatsByBlock(block);
        if (seats != null) {
            for (GSeat seat : seats) {
                if (!sitService.removeSeat(seat, GStopReason.BLOCK_BREAK)) {
                    event.setCancelled(true);
                    return; // Immediate exit on cancellation
                }
            }
        }
        
        // Process poses
        Collection<IGPose> poses = poseService.getPosesByBlock(block);
        if (poses != null) {
            for (IGPose pose : poses) {
                if (!poseService.removePose(pose, GStopReason.BLOCK_BREAK)) {
                    event.setCancelled(true);
                    return; // Immediate exit on cancellation
                }
            }
        }
    }
}
