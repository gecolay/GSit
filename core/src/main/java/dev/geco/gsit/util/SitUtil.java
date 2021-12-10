package dev.geco.gsit.util;

import java.util.*;

import org.bukkit.block.Block;
import org.bukkit.metadata.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class SitUtil {

    private final GSitMain GPM;

    public SitUtil(GSitMain GPluginMain) { GPM = GPluginMain; }

    public boolean isSeatBlock(Block B) { return B.hasMetadata(GPM.NAME); }

    @SuppressWarnings("unchecked")
    public List<GSeat> getSeats(Block B) {
        List<GSeat> seats = new ArrayList<GSeat>();
        if(isSeatBlock(B)) {
            MetadataValue m = B.getMetadata(GPM.NAME).stream().filter(s -> GPM.equals(s.getOwningPlugin())).findFirst().orElse(null);
            if(m != null) seats = new ArrayList<GSeat>((List<GSeat>) m.value());
        }
        return seats;
    }

    public List<GSeat> getSeats(List<Block> B) {
        List<GSeat> seats = new ArrayList<GSeat>();
        for(Block b : B) for(GSeat c : getSeats(b)) if(!seats.contains(c)) seats.add(c);
        return seats;
    }

    public void setSeatBlock(Block B, GSeat S) {
        List<GSeat> seats = getSeats(B);
        if(!seats.contains(S)) seats.add(S);
        B.setMetadata(GPM.NAME, new FixedMetadataValue(GPM, seats));
    }

    public void removeSeatBlock(Block B, GSeat S) {
        List<GSeat> seats = getSeats(B);
        seats.remove(S);
        if(seats.size() > 0) B.setMetadata(GPM.NAME, new FixedMetadataValue(GPM, seats));
        else B.removeMetadata(GPM.NAME, GPM);
    }

}