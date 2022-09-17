package dev.geco.gsit.util;

import org.bukkit.block.*;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class SitUtil {

    private final GSitMain GPM;

    public static final double STAIR_XZ_OFFSET = 0.123d;

    public static final double STAIR_Y_OFFSET = 0.5d;

    public SitUtil(GSitMain GPluginMain) { GPM = GPluginMain; }

    public GSeat createSeatForStair(Block Block, LivingEntity Entity) {

        GSeat seat = null;

        Stairs blockData = (Stairs) Block.getBlockData();

        if(blockData.getHalf() != Bisected.Half.BOTTOM) return GPM.getSitManager().createSeat(Block, Entity);

        BlockFace blockFace = blockData.getFacing().getOppositeFace();

        Stairs.Shape stairShape = blockData.getShape();

        if(blockData.getShape() == Stairs.Shape.STRAIGHT) {

            switch(blockFace) {

                case EAST:

                    seat = GPM.getSitManager().createSeat(Block, Entity, false, STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, 0d, -90f, true);
                    break;

                case SOUTH:

                    seat = GPM.getSitManager().createSeat(Block, Entity, false, 0d, -STAIR_Y_OFFSET, STAIR_XZ_OFFSET, 0f, true);
                    break;

                case WEST:

                    seat = GPM.getSitManager().createSeat(Block, Entity, false, -STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, 0d, 90f, true);
                    break;

                case NORTH:

                    seat = GPM.getSitManager().createSeat(Block, Entity, false, 0d, -STAIR_Y_OFFSET, -STAIR_XZ_OFFSET, 180f, true);
                    break;

                default:

                    break;
            }
        } else {

            if(blockFace == BlockFace.NORTH && stairShape == Stairs.Shape.OUTER_RIGHT || blockFace == BlockFace.EAST && stairShape == Stairs.Shape.OUTER_LEFT || blockFace == BlockFace.NORTH && stairShape == Stairs.Shape.INNER_RIGHT || blockFace == BlockFace.EAST && stairShape == Stairs.Shape.INNER_LEFT) {

                seat = GPM.getSitManager().createSeat(Block, Entity, false, STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, -STAIR_XZ_OFFSET, -135f, true);
            } else if(blockFace == BlockFace.NORTH && stairShape == Stairs.Shape.OUTER_LEFT || blockFace == BlockFace.WEST && stairShape == Stairs.Shape.OUTER_RIGHT || blockFace == BlockFace.NORTH && stairShape == Stairs.Shape.INNER_LEFT || blockFace == BlockFace.WEST && stairShape == Stairs.Shape.INNER_RIGHT) {

                seat = GPM.getSitManager().createSeat(Block, Entity, false, -STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, -STAIR_XZ_OFFSET, 135f, true);
            } else if(blockFace == BlockFace.SOUTH && stairShape == Stairs.Shape.OUTER_RIGHT || blockFace == BlockFace.WEST && stairShape == Stairs.Shape.OUTER_LEFT || blockFace == BlockFace.SOUTH && stairShape == Stairs.Shape.INNER_RIGHT || blockFace == BlockFace.WEST && stairShape == Stairs.Shape.INNER_LEFT) {

                seat = GPM.getSitManager().createSeat(Block, Entity, false, -STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, STAIR_XZ_OFFSET, 45f, true);
            } else if(blockFace == BlockFace.SOUTH && stairShape == Stairs.Shape.OUTER_LEFT || blockFace == BlockFace.EAST && stairShape == Stairs.Shape.OUTER_RIGHT || blockFace == BlockFace.SOUTH && stairShape == Stairs.Shape.INNER_LEFT || blockFace == BlockFace.EAST && stairShape == Stairs.Shape.INNER_RIGHT) {

                seat = GPM.getSitManager().createSeat(Block, Entity, false, STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, STAIR_XZ_OFFSET, -45f, true);
            }
        }

        return seat;
    }

}