package dev.geco.gsit.events;

import java.util.*;

import org.bukkit.block.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class BlockEvents implements Listener {

    private final GSitMain GPM;

    public BlockEvents(GSitMain GPluginMain) { GPM = GPluginMain; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void BPisEE(BlockPistonExtendEvent Event) { handleBlockPistonEvent(Event, Event.getBlocks()); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void BPisRE(BlockPistonRetractEvent Event) { handleBlockPistonEvent(Event, Event.getBlocks()); }

    private void handleBlockPistonEvent(BlockPistonEvent Event, List<Block> Blocks) {
        List<GSeat> moveList = new ArrayList<>();
        for(Block block : Blocks) {
            if(GPM.getSitManager().isSeatBlock(block)) {
                for(GSeat seat : GPM.getSitManager().getSeats(block)) {
                    if(moveList.contains(seat)) continue;
                    GPM.getSitManager().moveSeat(seat.getEntity(), Event.getDirection());
                    moveList.add(seat);
                }
            }
            if(!GPM.getCManager().GET_UP_BREAK || !GPM.getPoseManager().isPoseBlock(block)) continue;
            for(IGPoseSeat poseSeat : GPM.getPoseManager().getPoses(block)) GPM.getPoseManager().removePose(poseSeat.getPlayer(), GetUpReason.BREAK);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void BExpE(BlockExplodeEvent Event) { handleExplodeEvent(Event.blockList()); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void EExpE(EntityExplodeEvent Event) { handleExplodeEvent(Event.blockList()); }

    private void handleExplodeEvent(List<Block> Blocks) {
        if(!GPM.getCManager().GET_UP_BREAK) return;
        for(Block block : new ArrayList<>(Blocks)) {
            if(GPM.getSitManager().isSeatBlock(block)) for(GSeat seat : GPM.getSitManager().getSeats(block)) if(!GPM.getSitManager().removeSeat(seat.getEntity(), GetUpReason.BREAK)) Blocks.remove(block);
            if(GPM.getPoseManager().isPoseBlock(block)) for(IGPoseSeat poseSeat : GPM.getPoseManager().getPoses(block)) if(!GPM.getPoseManager().removePose(poseSeat.getPlayer(), GetUpReason.BREAK)) Blocks.remove(block);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void BFadE(BlockFadeEvent Event) { handleBlockEvent(Event, Event.getBlock()); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void LDecE(LeavesDecayEvent Event) { handleBlockEvent(Event, Event.getBlock()); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void BBurE(BlockBurnEvent Event) { handleBlockEvent(Event, Event.getBlock()); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void EChaBE(EntityChangeBlockEvent Event) { handleBlockEvent(Event, Event.getBlock()); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void BBreE(BlockBreakEvent Event) { handleBlockEvent(Event, Event.getBlock()); }

    private void handleBlockEvent(Cancellable Event, Block Block) {
        if(!GPM.getCManager().GET_UP_BREAK) return;
        if(GPM.getSitManager().isSeatBlock(Block)) for(GSeat seat : GPM.getSitManager().getSeats(Block)) if(!GPM.getSitManager().removeSeat(seat.getEntity(), GetUpReason.BREAK)) Event.setCancelled(true);
        if(GPM.getPoseManager().isPoseBlock(Block) && !Event.isCancelled()) for(IGPoseSeat poseSeat : GPM.getPoseManager().getPoses(Block)) if(!GPM.getPoseManager().removePose(poseSeat.getPlayer(), GetUpReason.BREAK)) Event.setCancelled(true);
    }

}