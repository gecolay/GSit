package dev.geco.gsit.util;

import org.bukkit.block.*;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class EnvironmentUtil {

    private final GSitMain GPM;

    public static final double STAIR_XZ_OFFSET = 0.123d;

    public static final double STAIR_Y_OFFSET = 0.5d;

    public EnvironmentUtil(GSitMain GPluginMain) { GPM = GPluginMain; }

    public boolean isInAllowedWorld(Entity Entity) {
        boolean allowed = !GPM.getCManager().WORLDBLACKLIST.contains(Entity.getWorld().getName());
        if(!GPM.getCManager().WORLDWHITELIST.isEmpty() && !GPM.getCManager().WORLDWHITELIST.contains(Entity.getWorld().getName())) allowed = false;
        return allowed || GPM.getPManager().hasPermission(Entity, "ByPass.World", "ByPass.*");
    }

    public GSeat createSeatForStair(Block Block, LivingEntity Entity) {

        Stairs blockData = (Stairs) Block.getBlockData();
        if(blockData.getHalf() != Bisected.Half.BOTTOM) return GPM.getSitManager().createSeat(Block, Entity);

        BlockFace blockFace = blockData.getFacing().getOppositeFace();
        if(blockData.getShape() == Stairs.Shape.STRAIGHT) {

            return switch (blockFace) {
                case EAST -> GPM.getSitManager().createSeat(Block, Entity, false, STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, 0d, -90f, true);
                case SOUTH -> GPM.getSitManager().createSeat(Block, Entity, false, 0d, -STAIR_Y_OFFSET, STAIR_XZ_OFFSET, 0f, true);
                case WEST -> GPM.getSitManager().createSeat(Block, Entity, false, -STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, 0d, 90f, true);
                case NORTH -> GPM.getSitManager().createSeat(Block, Entity, false, 0d, -STAIR_Y_OFFSET, -STAIR_XZ_OFFSET, 180f, true);
                default -> null;
            };

        }

        Stairs.Shape stairShape = blockData.getShape();

        if(blockFace == BlockFace.NORTH && stairShape == Stairs.Shape.OUTER_RIGHT || blockFace == BlockFace.EAST && stairShape == Stairs.Shape.OUTER_LEFT || blockFace == BlockFace.NORTH && stairShape == Stairs.Shape.INNER_RIGHT || blockFace == BlockFace.EAST && stairShape == Stairs.Shape.INNER_LEFT) {

            return GPM.getSitManager().createSeat(Block, Entity, false, STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, -STAIR_XZ_OFFSET, -135f, true);
        } else if(blockFace == BlockFace.NORTH && stairShape == Stairs.Shape.OUTER_LEFT || blockFace == BlockFace.WEST && stairShape == Stairs.Shape.OUTER_RIGHT || blockFace == BlockFace.NORTH && stairShape == Stairs.Shape.INNER_LEFT || blockFace == BlockFace.WEST && stairShape == Stairs.Shape.INNER_RIGHT) {

            return GPM.getSitManager().createSeat(Block, Entity, false, -STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, -STAIR_XZ_OFFSET, 135f, true);
        } else if(blockFace == BlockFace.SOUTH && stairShape == Stairs.Shape.OUTER_RIGHT || blockFace == BlockFace.WEST && stairShape == Stairs.Shape.OUTER_LEFT || blockFace == BlockFace.SOUTH && stairShape == Stairs.Shape.INNER_RIGHT || blockFace == BlockFace.WEST && stairShape == Stairs.Shape.INNER_LEFT) {

            return GPM.getSitManager().createSeat(Block, Entity, false, -STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, STAIR_XZ_OFFSET, 45f, true);
        } else if(blockFace == BlockFace.SOUTH && stairShape == Stairs.Shape.OUTER_LEFT || blockFace == BlockFace.EAST && stairShape == Stairs.Shape.OUTER_RIGHT || blockFace == BlockFace.SOUTH && stairShape == Stairs.Shape.INNER_LEFT || blockFace == BlockFace.EAST && stairShape == Stairs.Shape.INNER_RIGHT) {

            return GPM.getSitManager().createSeat(Block, Entity, false, STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, STAIR_XZ_OFFSET, -45f, true);
        }

        return null;
    }

}