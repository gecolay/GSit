package dev.geco.gsit.util;

import java.util.*;

import org.bukkit.block.*;
import org.bukkit.metadata.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class PoseUtil {

    private final GSitMain GPM;

    public PoseUtil(GSitMain GPluginMain) { GPM = GPluginMain; }

    public boolean isPoseBlock(Block Block) { return Block.hasMetadata(GPM.NAME + "P"); }

    @SuppressWarnings("unchecked")
    public List<IGPoseSeat> getPoses(Block Blocks) {

        List<IGPoseSeat> poses = new ArrayList<>();

        if(isPoseBlock(Blocks)) {

            MetadataValue metadataValue = Blocks.getMetadata(GPM.NAME + "P").stream().filter(s -> GPM.equals(s.getOwningPlugin())).findFirst().orElse(null);

            if(metadataValue != null) poses = new ArrayList<>((List<IGPoseSeat>) metadataValue.value());
        }

        return poses;
    }

    public List<IGPoseSeat> getPoses(List<Block> Blocks) {

        List<IGPoseSeat> poses = new ArrayList<>();

        for(Block block : Blocks) for(IGPoseSeat poseSeat : getPoses(block)) if(!poses.contains(poseSeat)) poses.add(poseSeat);

        return poses;
    }

    public void setPoseBlock(Block Block, IGPoseSeat PoseSeat) {

        List<IGPoseSeat> poses = getPoses(Block);

        if(!poses.contains(PoseSeat)) poses.add(PoseSeat);

        Block.setMetadata(GPM.NAME + "P", new FixedMetadataValue(GPM, poses));
    }

    public void removePoseBlock(Block Block, IGPoseSeat PoseSeat) {

        List<IGPoseSeat> poses = getPoses(Block);

        poses.remove(PoseSeat);

        if(poses.size() > 0) Block.setMetadata(GPM.NAME + "P", new FixedMetadataValue(GPM, poses));
        else Block.removeMetadata(GPM.NAME + "P", GPM);
    }

}