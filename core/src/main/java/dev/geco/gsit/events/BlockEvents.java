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
    public void BPisEE(BlockPistonExtendEvent Event) {

        List<GSeat> moveList = new ArrayList<>();

        for(Block block : Event.getBlocks()) {

            if(GPM.getSitUtil().isSeatBlock(block)) {

                for(GSeat seat : GPM.getSitUtil().getSeats(block)) {

                    if(moveList.contains(seat)) continue;

                    GPM.getSitManager().moveSeat(seat.getPlayer(), Event.getDirection());

                    moveList.add(seat);
                }
            }

            if(GPM.getCManager().GET_UP_BREAK && GPM.getPoseUtil().isPoseBlock(block)) for(IGPoseSeat poseSeat : GPM.getPoseUtil().getPoses(block)) GPM.getPoseManager().removePose(poseSeat.getSeat().getPlayer(), GetUpReason.BREAK);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void BPisRE(BlockPistonRetractEvent Event) {

        List<GSeat> moveList = new ArrayList<>();

        for(Block block : Event.getBlocks()) {

            if(GPM.getSitUtil().isSeatBlock(block)) {

                for(GSeat seat : GPM.getSitUtil().getSeats(block)) {

                    if(moveList.contains(seat)) continue;

                    GPM.getSitManager().moveSeat(seat.getPlayer(), Event.getDirection());

                    moveList.add(seat);
                }
            }

            if(GPM.getCManager().GET_UP_BREAK && GPM.getPoseUtil().isPoseBlock(block)) for(IGPoseSeat poseSeat : GPM.getPoseUtil().getPoses(block)) GPM.getPoseManager().removePose(poseSeat.getSeat().getPlayer(), GetUpReason.BREAK);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void BExpE(BlockExplodeEvent Event) {

        if(!GPM.getCManager().GET_UP_BREAK) return;

        for(Block block : new ArrayList<>(Event.blockList())) {

            if(GPM.getSitUtil().isSeatBlock(block)) for(GSeat seat : GPM.getSitUtil().getSeats(block)) if(!GPM.getSitManager().removeSeat(seat.getPlayer(), GetUpReason.BREAK)) Event.blockList().remove(block);

            if(GPM.getPoseUtil().isPoseBlock(block)) for(IGPoseSeat poseSeat : GPM.getPoseUtil().getPoses(block)) if(!GPM.getPoseManager().removePose(poseSeat.getSeat().getPlayer(), GetUpReason.BREAK)) Event.blockList().remove(block);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void EExpE(EntityExplodeEvent Event) {

        if(!GPM.getCManager().GET_UP_BREAK) return;

        for(Block block : new ArrayList<>(Event.blockList())) {

            if(GPM.getSitUtil().isSeatBlock(block)) for(GSeat seat : GPM.getSitUtil().getSeats(block)) if(!GPM.getSitManager().removeSeat(seat.getPlayer(), GetUpReason.BREAK)) Event.blockList().remove(block);

            if(GPM.getPoseUtil().isPoseBlock(block)) for(IGPoseSeat poseSeat : GPM.getPoseUtil().getPoses(block)) if(!GPM.getPoseManager().removePose(poseSeat.getSeat().getPlayer(), GetUpReason.BREAK)) Event.blockList().remove(block);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void BFadE(BlockFadeEvent Event) {

        if(!GPM.getCManager().GET_UP_BREAK) return;

        if(GPM.getSitUtil().isSeatBlock(Event.getBlock())) for(GSeat seat : GPM.getSitUtil().getSeats(Event.getBlock())) if(!GPM.getSitManager().removeSeat(seat.getPlayer(), GetUpReason.BREAK)) Event.setCancelled(true);

        if(GPM.getPoseUtil().isPoseBlock(Event.getBlock()) && !Event.isCancelled()) for(IGPoseSeat poseSeat : GPM.getPoseUtil().getPoses(Event.getBlock())) if(!GPM.getPoseManager().removePose(poseSeat.getSeat().getPlayer(), GetUpReason.BREAK)) Event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void LDecE(LeavesDecayEvent Event) {

        if(!GPM.getCManager().GET_UP_BREAK) return;

        if(GPM.getSitUtil().isSeatBlock(Event.getBlock())) for(GSeat seat : GPM.getSitUtil().getSeats(Event.getBlock())) if(!GPM.getSitManager().removeSeat(seat.getPlayer(), GetUpReason.BREAK)) Event.setCancelled(true);

        if(GPM.getPoseUtil().isPoseBlock(Event.getBlock()) && !Event.isCancelled()) for(IGPoseSeat poseSeat : GPM.getPoseUtil().getPoses(Event.getBlock())) if(!GPM.getPoseManager().removePose(poseSeat.getSeat().getPlayer(), GetUpReason.BREAK)) Event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void BBurE(BlockBurnEvent Event) {

        if(!GPM.getCManager().GET_UP_BREAK) return;

        if(GPM.getSitUtil().isSeatBlock(Event.getBlock())) for(GSeat seat : GPM.getSitUtil().getSeats(Event.getBlock())) if(!GPM.getSitManager().removeSeat(seat.getPlayer(), GetUpReason.BREAK)) Event.setCancelled(true);

        if(GPM.getPoseUtil().isPoseBlock(Event.getBlock()) && !Event.isCancelled()) for(IGPoseSeat poseSeat : GPM.getPoseUtil().getPoses(Event.getBlock())) if(!GPM.getPoseManager().removePose(poseSeat.getSeat().getPlayer(), GetUpReason.BREAK)) Event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void EChaBE(EntityChangeBlockEvent Event) {

        if(!GPM.getCManager().GET_UP_BREAK) return;

        if(GPM.getSitUtil().isSeatBlock(Event.getBlock())) for(GSeat seat : GPM.getSitUtil().getSeats(Event.getBlock())) if(!GPM.getSitManager().removeSeat(seat.getPlayer(), GetUpReason.BREAK)) Event.setCancelled(true);

        if(GPM.getPoseUtil().isPoseBlock(Event.getBlock()) && !Event.isCancelled()) for(IGPoseSeat poseSeat : GPM.getPoseUtil().getPoses(Event.getBlock())) if(!GPM.getPoseManager().removePose(poseSeat.getSeat().getPlayer(), GetUpReason.BREAK)) Event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void BBreE(BlockBreakEvent Event) {

        if(!GPM.getCManager().GET_UP_BREAK) return;

        if(GPM.getSitUtil().isSeatBlock(Event.getBlock())) for(GSeat seat : GPM.getSitUtil().getSeats(Event.getBlock())) if(!GPM.getSitManager().removeSeat(seat.getPlayer(), GetUpReason.BREAK)) Event.setCancelled(true);

        if(GPM.getPoseUtil().isPoseBlock(Event.getBlock()) && !Event.isCancelled()) for(IGPoseSeat poseSeat : GPM.getPoseUtil().getPoses(Event.getBlock())) if(!GPM.getPoseManager().removePose(poseSeat.getSeat().getPlayer(), GetUpReason.BREAK)) Event.setCancelled(true);
    }

}