package dev.geco.gsit.util;

import java.util.*;

import org.bukkit.block.*;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.*;
import org.bukkit.metadata.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.manager.*;
import dev.geco.gsit.objects.*;

public class SitUtil {

    private final GSitMain GPM;

    public SitUtil(GSitMain GPluginMain) { GPM = GPluginMain; }

    public boolean isSeatBlock(Block B) { return B.hasMetadata(GPM.NAME); }

    @SuppressWarnings("unchecked")
    public List<GSeat> getSeats(Block B) {
        List<GSeat> seats = new ArrayList<>();
        if(isSeatBlock(B)) {
            MetadataValue m = B.getMetadata(GPM.NAME).stream().filter(s -> GPM.equals(s.getOwningPlugin())).findFirst().orElse(null);
            if(m != null) seats = new ArrayList<>((List<GSeat>) m.value());
        }
        return seats;
    }

    public List<GSeat> getSeats(List<Block> B) {
        List<GSeat> seats = new ArrayList<>();
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

    public GSeat createSeatForStair(Block Block, Player Player) {

        GSeat seat = null;

        Stairs bd = (Stairs) Block.getBlockData();

        if(bd.getHalf() != Bisected.Half.BOTTOM) return GPM.getSitManager().createSeat(Block, Player);

        BlockFace f = bd.getFacing().getOppositeFace();

        Stairs.Shape s = bd.getShape();

        if(bd.getShape() == Stairs.Shape.STRAIGHT) {
            switch(f) {
                case EAST:
                    seat = GPM.getSitManager().createSeat(Block, Player, false, ISitManager.STAIR_XZ_OFFSET, -ISitManager.STAIR_Y_OFFSET, 0d, -90f, true, GPM.getCManager().GET_UP_SNEAK);
                    break;
                case SOUTH:
                    seat = GPM.getSitManager().createSeat(Block, Player, false, 0d, -ISitManager.STAIR_Y_OFFSET, ISitManager.STAIR_XZ_OFFSET, 0f, true, GPM.getCManager().GET_UP_SNEAK);
                    break;
                case WEST:
                    seat = GPM.getSitManager().createSeat(Block, Player, false, -ISitManager.STAIR_XZ_OFFSET, -ISitManager.STAIR_Y_OFFSET, 0d, 90f, true, GPM.getCManager().GET_UP_SNEAK);
                    break;
                case NORTH:
                    seat = GPM.getSitManager().createSeat(Block, Player, false, 0d, -ISitManager.STAIR_Y_OFFSET, -ISitManager.STAIR_XZ_OFFSET, 180f, true, GPM.getCManager().GET_UP_SNEAK);
                    break;
                default:
                    break;
            }
        } else {
            if(f == BlockFace.NORTH && s == Stairs.Shape.OUTER_RIGHT || f == BlockFace.EAST && s == Stairs.Shape.OUTER_LEFT || f == BlockFace.NORTH && s == Stairs.Shape.INNER_RIGHT || f == BlockFace.EAST && s == Stairs.Shape.INNER_LEFT) {
                seat = GPM.getSitManager().createSeat(Block, Player, false, ISitManager.STAIR_XZ_OFFSET, -ISitManager.STAIR_Y_OFFSET, -ISitManager.STAIR_XZ_OFFSET, -135f, true, GPM.getCManager().GET_UP_SNEAK);
            } else if(f == BlockFace.NORTH && s == Stairs.Shape.OUTER_LEFT || f == BlockFace.WEST && s == Stairs.Shape.OUTER_RIGHT || f == BlockFace.NORTH && s == Stairs.Shape.INNER_LEFT || f == BlockFace.WEST && s == Stairs.Shape.INNER_RIGHT) {
                seat = GPM.getSitManager().createSeat(Block, Player, false, -ISitManager.STAIR_XZ_OFFSET, -ISitManager.STAIR_Y_OFFSET, -ISitManager.STAIR_XZ_OFFSET, 135f, true, GPM.getCManager().GET_UP_SNEAK);
            } else if(f == BlockFace.SOUTH && s == Stairs.Shape.OUTER_RIGHT || f == BlockFace.WEST && s == Stairs.Shape.OUTER_LEFT || f == BlockFace.SOUTH && s == Stairs.Shape.INNER_RIGHT || f == BlockFace.WEST && s == Stairs.Shape.INNER_LEFT) {
                seat = GPM.getSitManager().createSeat(Block, Player, false, -ISitManager.STAIR_XZ_OFFSET, -ISitManager.STAIR_Y_OFFSET, ISitManager.STAIR_XZ_OFFSET, 45f, true, GPM.getCManager().GET_UP_SNEAK);
            } else if(f == BlockFace.SOUTH && s == Stairs.Shape.OUTER_LEFT || f == BlockFace.EAST && s == Stairs.Shape.OUTER_RIGHT || f == BlockFace.SOUTH && s == Stairs.Shape.INNER_LEFT || f == BlockFace.EAST && s == Stairs.Shape.INNER_RIGHT) {
                seat = GPM.getSitManager().createSeat(Block, Player, false, ISitManager.STAIR_XZ_OFFSET, -ISitManager.STAIR_Y_OFFSET, ISitManager.STAIR_XZ_OFFSET, -45f, true, GPM.getCManager().GET_UP_SNEAK);
            }
        }

        return seat;
    }

}