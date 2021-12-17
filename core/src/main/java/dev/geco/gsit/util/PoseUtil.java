package dev.geco.gsit.util;

import java.util.*;

import org.bukkit.block.Block;
import org.bukkit.metadata.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class PoseUtil {

    private final GSitMain GPM;

    public PoseUtil(GSitMain GPluginMain) { GPM = GPluginMain; }

    public boolean isPoseBlock(Block B) { return B.hasMetadata(GPM.NAME + "P"); }

    @SuppressWarnings("unchecked")
    public List<IGPoseSeat> getPoses(Block B) {
        List<IGPoseSeat> poses = new ArrayList<>();
        if(isPoseBlock(B)) {
            MetadataValue m = B.getMetadata(GPM.NAME + "P").stream().filter(s -> GPM.equals(s.getOwningPlugin())).findFirst().orElse(null);
            if(m != null) poses = new ArrayList<>((List<IGPoseSeat>) m.value());
        }
        return poses;
    }

    public List<IGPoseSeat> getPoses(List<Block> B) {
        List<IGPoseSeat> poses = new ArrayList<>();
        for(Block b : B) for(IGPoseSeat c : getPoses(b)) if(!poses.contains(c)) poses.add(c);
        return poses;
    }

    public void setPoseBlock(Block B, IGPoseSeat P) {
        List<IGPoseSeat> poses = getPoses(B);
        if(!poses.contains(P)) poses.add(P);
        B.setMetadata(GPM.NAME + "P", new FixedMetadataValue(GPM, poses));
    }

    public void removePoseBlock(Block B, IGPoseSeat P) {
        List<IGPoseSeat> poses = getPoses(B);
        poses.remove(P);
        if(poses.size() > 0) B.setMetadata(GPM.NAME + "P", new FixedMetadataValue(GPM, poses));
        else B.removeMetadata(GPM.NAME + "P", GPM);
    }

}