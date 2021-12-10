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
    public void BPisEE(BlockPistonExtendEvent e) {
        List<GSeat> ml = new ArrayList<GSeat>();
        for(Block b : e.getBlocks()) {
            if(GPM.getSitUtil().isSeatBlock(b)) {
                for(GSeat s : GPM.getSitUtil().getSeats(b)) {
                    if(ml.contains(s)) continue;
                    GPM.getSitManager().moveSeat(s, e.getDirection());
                    ml.add(s);
                }
            }
            if(GPM.getPoseUtil().isPoseBlock(b)) {
                for(IGPoseSeat p : GPM.getPoseUtil().getPoses(b)) GPM.getPoseManager().removePose(p, GetUpReason.BREAK);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void BPisRE(BlockPistonRetractEvent e) {
        List<GSeat> ml = new ArrayList<GSeat>();
        for(Block b : e.getBlocks()) {
            if(GPM.getSitUtil().isSeatBlock(b)) {
                for(GSeat s : GPM.getSitUtil().getSeats(b)) {
                    if(ml.contains(s)) continue;
                    GPM.getSitManager().moveSeat(s, e.getDirection());
                    ml.add(s);
                }
            }
            if(GPM.getPoseUtil().isPoseBlock(b)) {
                for(IGPoseSeat p : GPM.getPoseUtil().getPoses(b)) GPM.getPoseManager().removePose(p, GetUpReason.BREAK);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void BExpE(BlockExplodeEvent e) {
        for(Block b : new ArrayList<>(e.blockList())) {
            if(GPM.getSitUtil().isSeatBlock(b)) {
                for(GSeat s : GPM.getSitUtil().getSeats(b)) GPM.getSitManager().removeSeat(s, GetUpReason.BREAK);
            }
            if(GPM.getPoseUtil().isPoseBlock(b)) {
                for(IGPoseSeat p : GPM.getPoseUtil().getPoses(b)) GPM.getPoseManager().removePose(p, GetUpReason.BREAK);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void EExpE(EntityExplodeEvent e) {
        for(Block b : new ArrayList<>(e.blockList())) {
            if(GPM.getSitUtil().isSeatBlock(b)) {
                for(GSeat s : GPM.getSitUtil().getSeats(b)) GPM.getSitManager().removeSeat(s, GetUpReason.BREAK);
            }
            if(GPM.getPoseUtil().isPoseBlock(b)) {
                for(IGPoseSeat p : GPM.getPoseUtil().getPoses(b)) GPM.getPoseManager().removePose(p, GetUpReason.BREAK);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void BFadE(BlockFadeEvent e) {
        if(GPM.getSitUtil().isSeatBlock(e.getBlock())) {
            for(GSeat s : GPM.getSitUtil().getSeats(e.getBlock())) GPM.getSitManager().removeSeat(s, GetUpReason.BREAK);
        }
        if(GPM.getPoseUtil().isPoseBlock(e.getBlock())) {
            for(IGPoseSeat p : GPM.getPoseUtil().getPoses(e.getBlock())) GPM.getPoseManager().removePose(p, GetUpReason.BREAK);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void LDecE(LeavesDecayEvent e) {
        if(GPM.getSitUtil().isSeatBlock(e.getBlock())) {
            for(GSeat s : GPM.getSitUtil().getSeats(e.getBlock())) GPM.getSitManager().removeSeat(s, GetUpReason.BREAK);
        }
        if(GPM.getPoseUtil().isPoseBlock(e.getBlock())) {
            for(IGPoseSeat p : GPM.getPoseUtil().getPoses(e.getBlock())) GPM.getPoseManager().removePose(p, GetUpReason.BREAK);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void BBurE(BlockBurnEvent e) {
        if(GPM.getSitUtil().isSeatBlock(e.getBlock())) {
            for(GSeat s : GPM.getSitUtil().getSeats(e.getBlock())) GPM.getSitManager().removeSeat(s, GetUpReason.BREAK);
        }
        if(GPM.getPoseUtil().isPoseBlock(e.getBlock())) {
            for(IGPoseSeat p : GPM.getPoseUtil().getPoses(e.getBlock())) GPM.getPoseManager().removePose(p, GetUpReason.BREAK);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void EChaBE(EntityChangeBlockEvent e) {
        if(GPM.getSitUtil().isSeatBlock(e.getBlock())) {
            for(GSeat s : GPM.getSitUtil().getSeats(e.getBlock())) GPM.getSitManager().removeSeat(s, GetUpReason.BREAK);
        }
        if(GPM.getPoseUtil().isPoseBlock(e.getBlock())) {
            for(IGPoseSeat p : GPM.getPoseUtil().getPoses(e.getBlock())) GPM.getPoseManager().removePose(p, GetUpReason.BREAK);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void BBreE(BlockBreakEvent e) {
        if(GPM.getSitUtil().isSeatBlock(e.getBlock())) {
            for(GSeat s : GPM.getSitUtil().getSeats(e.getBlock())) GPM.getSitManager().removeSeat(s, GetUpReason.BREAK);
        }
        if(GPM.getPoseUtil().isPoseBlock(e.getBlock())) {
            for(IGPoseSeat p : GPM.getPoseUtil().getPoses(e.getBlock())) GPM.getPoseManager().removePose(p, GetUpReason.BREAK);
        }
    }
    
}