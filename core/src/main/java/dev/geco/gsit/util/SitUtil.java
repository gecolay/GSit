package dev.geco.gsit.util;

import java.util.*;

import org.bukkit.block.*;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.*;
import org.bukkit.entity.*;
import org.bukkit.metadata.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.manager.*;
import dev.geco.gsit.objects.*;

public class SitUtil {

    private final GSitMain GPM;

    public SitUtil(GSitMain GPluginMain) { GPM = GPluginMain; }

    public boolean isSeatBlock(Block Block) { return Block.hasMetadata(GPM.NAME); }

    @SuppressWarnings("unchecked")
    public List<GSeat> getSeats(Block Block) {

        List<GSeat> seats = new ArrayList<>();

        if(isSeatBlock(Block)) {

            MetadataValue metadataValue = Block.getMetadata(GPM.NAME).stream().filter(s -> GPM.equals(s.getOwningPlugin())).findFirst().orElse(null);

            if(metadataValue != null) seats = new ArrayList<>((List<GSeat>) metadataValue.value());
        }

        return seats;
    }

    public List<GSeat> getSeats(List<Block> Blocks) {

        List<GSeat> seats = new ArrayList<>();

        for(Block block : Blocks) for(GSeat seat : getSeats(block)) if(!seats.contains(seat)) seats.add(seat);

        return seats;
    }

    public void setSeatBlock(Block Block, GSeat Seat) {

        List<GSeat> seats = getSeats(Block);

        if(!seats.contains(Seat)) seats.add(Seat);

        Block.setMetadata(GPM.NAME, new FixedMetadataValue(GPM, seats));
    }

    public void removeSeatBlock(Block Block, GSeat Seat) {

        List<GSeat> seats = getSeats(Block);

        seats.remove(Seat);

        if(seats.size() > 0) Block.setMetadata(GPM.NAME, new FixedMetadataValue(GPM, seats));
        else Block.removeMetadata(GPM.NAME, GPM);
    }

    public GSeat createSeatForStair(Block Block, Player Player) {

        GSeat seat = null;

        Stairs blockData = (Stairs) Block.getBlockData();

        if(blockData.getHalf() != Bisected.Half.BOTTOM) return GPM.getSitManager().createSeat(Block, Player);

        BlockFace blockFace = blockData.getFacing().getOppositeFace();

        Stairs.Shape stairShape = blockData.getShape();

        if(blockData.getShape() == Stairs.Shape.STRAIGHT) {

            switch(blockFace) {

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

            if(blockFace == BlockFace.NORTH && stairShape == Stairs.Shape.OUTER_RIGHT || blockFace == BlockFace.EAST && stairShape == Stairs.Shape.OUTER_LEFT || blockFace == BlockFace.NORTH && stairShape == Stairs.Shape.INNER_RIGHT || blockFace == BlockFace.EAST && stairShape == Stairs.Shape.INNER_LEFT) {

                seat = GPM.getSitManager().createSeat(Block, Player, false, ISitManager.STAIR_XZ_OFFSET, -ISitManager.STAIR_Y_OFFSET, -ISitManager.STAIR_XZ_OFFSET, -135f, true, GPM.getCManager().GET_UP_SNEAK);
            } else if(blockFace == BlockFace.NORTH && stairShape == Stairs.Shape.OUTER_LEFT || blockFace == BlockFace.WEST && stairShape == Stairs.Shape.OUTER_RIGHT || blockFace == BlockFace.NORTH && stairShape == Stairs.Shape.INNER_LEFT || blockFace == BlockFace.WEST && stairShape == Stairs.Shape.INNER_RIGHT) {

                seat = GPM.getSitManager().createSeat(Block, Player, false, -ISitManager.STAIR_XZ_OFFSET, -ISitManager.STAIR_Y_OFFSET, -ISitManager.STAIR_XZ_OFFSET, 135f, true, GPM.getCManager().GET_UP_SNEAK);
            } else if(blockFace == BlockFace.SOUTH && stairShape == Stairs.Shape.OUTER_RIGHT || blockFace == BlockFace.WEST && stairShape == Stairs.Shape.OUTER_LEFT || blockFace == BlockFace.SOUTH && stairShape == Stairs.Shape.INNER_RIGHT || blockFace == BlockFace.WEST && stairShape == Stairs.Shape.INNER_LEFT) {

                seat = GPM.getSitManager().createSeat(Block, Player, false, -ISitManager.STAIR_XZ_OFFSET, -ISitManager.STAIR_Y_OFFSET, ISitManager.STAIR_XZ_OFFSET, 45f, true, GPM.getCManager().GET_UP_SNEAK);
            } else if(blockFace == BlockFace.SOUTH && stairShape == Stairs.Shape.OUTER_LEFT || blockFace == BlockFace.EAST && stairShape == Stairs.Shape.OUTER_RIGHT || blockFace == BlockFace.SOUTH && stairShape == Stairs.Shape.INNER_LEFT || blockFace == BlockFace.EAST && stairShape == Stairs.Shape.INNER_RIGHT) {

                seat = GPM.getSitManager().createSeat(Block, Player, false, ISitManager.STAIR_XZ_OFFSET, -ISitManager.STAIR_Y_OFFSET, ISitManager.STAIR_XZ_OFFSET, -45f, true, GPM.getCManager().GET_UP_SNEAK);
            }
        }

        return seat;
    }

}