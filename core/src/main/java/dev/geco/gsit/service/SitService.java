package dev.geco.gsit.service;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.EntityStopSitEvent;
import dev.geco.gsit.api.event.EntitySitEvent;
import dev.geco.gsit.api.event.PreEntityStopSitEvent;
import dev.geco.gsit.api.event.PreEntitySitEvent;
import dev.geco.gsit.object.GSeat;
import dev.geco.gsit.object.GStopReason;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SitService {

    public static final double STAIR_XZ_OFFSET = 0.123d;
    public static final double STAIR_Y_OFFSET = 0.5d;

    private final GSitMain gSitMain;
    private final double baseOffset;
    private final HashMap<UUID, GSeat> seats = new HashMap<>();
    private final HashMap<Block, Set<GSeat>> blockSeats = new HashMap<>();
    private int sitUsageCount = 0;
    private long sitUsageNanoTime = 0;

    public SitService(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        baseOffset = gSitMain.getVersionManager().isNewerOrVersion(20, 2) ? -0.05d : 0.2d;
    }

    public double getBaseOffset() { return baseOffset; }

    public HashMap<UUID, GSeat> getAllSeats() { return seats; }

    public boolean isEntitySitting(LivingEntity entity) { return seats.containsKey(entity.getUniqueId()); }

    public GSeat getSeatByEntity(LivingEntity entity) { return seats.get(entity.getUniqueId()); }

    public void removeAllSeats() { for(GSeat seat : new ArrayList<>(seats.values())) removeSeat(seat, GStopReason.PLUGIN); }

    public boolean isBlockWithSeat(Block block) { return blockSeats.containsKey(block); }

    public Set<GSeat> getSeatsByBlock(Block block) { return blockSeats.getOrDefault(block, Collections.emptySet()); }

    public boolean kickSeatEntitiesFromBlock(Block block, LivingEntity entity) {
        if(!isBlockWithSeat(block)) return true;
        if(!gSitMain.getPermissionService().hasPermission(entity, "Kick.Sit")) return false;
        for(GSeat seat : getSeatsByBlock(block)) if(!removeSeat(seat, GStopReason.KICKED)) return false;
        return true;
    }

    public GSeat createSeat(Block block, LivingEntity entity) { return createSeat(block, entity, true, 0d, 0d, 0d, entity.getLocation().getYaw(), gSitMain.getConfigService().CENTER_BLOCK); }

    public GSeat createSeat(Block block, LivingEntity entity, boolean canRotate, double xOffset, double yOffset, double zOffset, float seatRotation, boolean sitInBlockCenter) {
        Location returnLocation = entity.getLocation();
        Location seatLocation = getSeatLocation(block, returnLocation, xOffset, yOffset, zOffset, sitInBlockCenter);
        if(!gSitMain.getEntityUtil().isSitLocationValid(seatLocation)) return null;

        PreEntitySitEvent preEntitySitEvent = new PreEntitySitEvent(entity, block);
        Bukkit.getPluginManager().callEvent(preEntitySitEvent);
        if(preEntitySitEvent.isCancelled()) return null;

        seatLocation.setYaw(seatRotation);
        Entity seatEntity = gSitMain.getEntityUtil().createSeatEntity(seatLocation, entity, canRotate);
        if(seatEntity == null) return null;

        if(gSitMain.getConfigService().CUSTOM_MESSAGE && entity instanceof Player) {
            gSitMain.getMessageService().sendActionBarMessage((Player) entity, "Messages.action-sit-info");
            if(gSitMain.getConfigService().ENHANCED_COMPATIBILITY) {
                gSitMain.getTaskService().runDelayed(() -> {
                    gSitMain.getMessageService().sendActionBarMessage((Player) entity, "Messages.action-sit-info");
                }, entity, 2);
            }
        }

        GSeat seat = new GSeat(block, seatLocation, entity, seatEntity, returnLocation);
        seats.put(entity.getUniqueId(), seat);
        blockSeats.computeIfAbsent(block, k -> new HashSet<>()).add(seat);
        sitUsageCount++;
        Bukkit.getPluginManager().callEvent(new EntitySitEvent(seat));

        return seat;
    }

    public Location getSeatLocation(Block block, Location location, double xOffset, double yOffset, double zOffset, boolean sitInBlockCenter) {
        double additionalOffset = sitInBlockCenter ? block.getBoundingBox().getMinY() + block.getBoundingBox().getHeight() : 0d;
        additionalOffset = (sitInBlockCenter ? additionalOffset == 0d ? 1d : additionalOffset - block.getY() : additionalOffset) + gSitMain.getConfigService().S_SITMATERIALS.getOrDefault(block.getType(), 0d);
        if(sitInBlockCenter) return block.getLocation().add(0.5d + xOffset, yOffset - baseOffset + additionalOffset, 0.5d + zOffset);
        return location.add(xOffset, yOffset - baseOffset + gSitMain.getConfigService().S_SITMATERIALS.getOrDefault(block.getType(), 0d), zOffset);
    }

    public void moveSeat(GSeat seat, BlockFace blockDirection) {
        if(seat.getEntity() instanceof Player player) {
            PlayerMoveEvent playerMoveEvent = new PlayerMoveEvent(player, player.getLocation(), player.getLocation().add(blockDirection.getModX(), blockDirection.getModY(), blockDirection.getModZ()));
            Bukkit.getPluginManager().callEvent(playerMoveEvent);
            if(playerMoveEvent.isCancelled()) return;
        }

        Set<GSeat> seats = blockSeats.get(seat.getBlock());
        if(seats != null) seats.remove(seat);
        seat.setBlock(seat.getBlock().getRelative(blockDirection));
        seats = blockSeats.get(seat.getBlock());
        if(seats != null) seats.add(seat);
        seat.setLocation(seat.getLocation().add(blockDirection.getModX(), blockDirection.getModY(), blockDirection.getModZ()));
        gSitMain.getEntityUtil().setEntityLocation(seat.getSeatEntity(), seat.getLocation());
    }

    public boolean removeSeat(GSeat seat, GStopReason stopReason) { return removeSeat(seat, stopReason, true); }

    public boolean removeSeat(GSeat seat, GStopReason stopReason, boolean useReturnLocation) {
        PreEntityStopSitEvent preEntityStopSitEvent = new PreEntityStopSitEvent(seat, stopReason);
        Bukkit.getPluginManager().callEvent(preEntityStopSitEvent);
        if(preEntityStopSitEvent.isCancelled() && stopReason.isCancellable()) return false;

        Entity entity = seat.getEntity();

        Location returnLocation = gSitMain.getConfigService().GET_UP_RETURN ? seat.getReturnLocation() : seat.getLocation().add(0d, baseOffset + (Tag.STAIRS.isTagged(seat.getBlock().getType()) ? STAIR_Y_OFFSET : 0d) - gSitMain.getConfigService().S_SITMATERIALS.getOrDefault(seat.getBlock().getType(), 0d), 0d);
        Location entityLocation = entity.getLocation();
        returnLocation.setYaw(entityLocation.getYaw());
        returnLocation.setPitch(entityLocation.getPitch());
        if(entity.isValid() && useReturnLocation && gSitMain.getVersionManager().isNewerOrVersion(18, 0)) gSitMain.getEntityUtil().setEntityLocation(entity, returnLocation);
        if(seat.getSeatEntity().isValid() && !gSitMain.getVersionManager().isNewerOrVersion(18, 0)) gSitMain.getEntityUtil().setEntityLocation(seat.getSeatEntity(), returnLocation);

        blockSeats.remove(seat.getBlock());
        seats.remove(entity.getUniqueId());
        seat.getSeatEntity().remove();
        Bukkit.getPluginManager().callEvent(new EntityStopSitEvent(seat, stopReason));
        sitUsageNanoTime += seat.getLifetimeInNanoSeconds();

        return true;
    }

    public GSeat createStairSeatForEntity(Block block, LivingEntity entity) {
        Stairs blockData = (Stairs) block.getBlockData();
        if(blockData.getHalf() != Bisected.Half.BOTTOM) return createSeat(block, entity);

        BlockFace blockFace = blockData.getFacing().getOppositeFace();
        if(blockData.getShape() == Stairs.Shape.STRAIGHT) {
            return switch (blockFace) {
                case EAST -> createSeat(block, entity, false, STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, 0d, -90f, true);
                case SOUTH -> createSeat(block, entity, false, 0d, -STAIR_Y_OFFSET, STAIR_XZ_OFFSET, 0f, true);
                case WEST -> createSeat(block, entity, false, -STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, 0d, 90f, true);
                case NORTH -> createSeat(block, entity, false, 0d, -STAIR_Y_OFFSET, -STAIR_XZ_OFFSET, 180f, true);
                default -> null;
            };
        }

        Stairs.Shape stairShape = blockData.getShape();
        if(blockFace == BlockFace.NORTH && stairShape == Stairs.Shape.OUTER_RIGHT || blockFace == BlockFace.EAST && stairShape == Stairs.Shape.OUTER_LEFT || blockFace == BlockFace.NORTH && stairShape == Stairs.Shape.INNER_RIGHT || blockFace == BlockFace.EAST && stairShape == Stairs.Shape.INNER_LEFT) {
            return createSeat(block, entity, false, STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, -STAIR_XZ_OFFSET, -135f, true);
        } else if(blockFace == BlockFace.NORTH && stairShape == Stairs.Shape.OUTER_LEFT || blockFace == BlockFace.WEST && stairShape == Stairs.Shape.OUTER_RIGHT || blockFace == BlockFace.NORTH && stairShape == Stairs.Shape.INNER_LEFT || blockFace == BlockFace.WEST && stairShape == Stairs.Shape.INNER_RIGHT) {
            return createSeat(block, entity, false, -STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, -STAIR_XZ_OFFSET, 135f, true);
        } else if(blockFace == BlockFace.SOUTH && stairShape == Stairs.Shape.OUTER_RIGHT || blockFace == BlockFace.WEST && stairShape == Stairs.Shape.OUTER_LEFT || blockFace == BlockFace.SOUTH && stairShape == Stairs.Shape.INNER_RIGHT || blockFace == BlockFace.WEST && stairShape == Stairs.Shape.INNER_LEFT) {
            return createSeat(block, entity, false, -STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, STAIR_XZ_OFFSET, 45f, true);
        } else if(blockFace == BlockFace.SOUTH && stairShape == Stairs.Shape.OUTER_LEFT || blockFace == BlockFace.EAST && stairShape == Stairs.Shape.OUTER_RIGHT || blockFace == BlockFace.SOUTH && stairShape == Stairs.Shape.INNER_LEFT || blockFace == BlockFace.EAST && stairShape == Stairs.Shape.INNER_RIGHT) {
            return createSeat(block, entity, false, STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, STAIR_XZ_OFFSET, -45f, true);
        }

        return null;
    }

    public int getSitUsageCount() { return sitUsageCount; }

    public long getSitUsageTimeInSeconds() { return sitUsageNanoTime / 1_000_000_000; }

    public void resetSitUsageStats() {
        sitUsageCount = 0;
        sitUsageNanoTime = 0;
    }

}