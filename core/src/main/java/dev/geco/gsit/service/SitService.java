package dev.geco.gsit.service;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.EntityStopSitEvent;
import dev.geco.gsit.api.event.EntitySitEvent;
import dev.geco.gsit.api.event.PreEntityStopSitEvent;
import dev.geco.gsit.api.event.PreEntitySitEvent;
import dev.geco.gsit.model.Seat;
import dev.geco.gsit.model.StopReason;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
import java.util.logging.Level;

public class SitService {

    public static final double STAIR_XZ_OFFSET = 0.123d;
    public static final double STAIR_Y_OFFSET = 0.5d;
    public static final String SIT_TAG = GSitMain.NAME + "_sit";

    private final GSitMain gSitMain;
    private final double baseOffset;
    private final HashMap<UUID, Seat> seats = new HashMap<>();
    private final HashMap<Block, Set<Seat>> blockSeats = new HashMap<>();
    private final HashSet<UUID> entityBlocked = new HashSet<>();
    private int sitUsageCount = 0;
    private long sitUsageNanoTime = 0;

    public SitService(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        baseOffset = gSitMain.getVersionManager().isNewerOrVersion(20, 2) ? -0.05d : 0.2d;
    }

    public double getBaseOffset() { return baseOffset; }

    public HashMap<UUID, Seat> getAllSeats() { return seats; }

    public boolean isEntitySitting(LivingEntity entity) { return seats.containsKey(entity.getUniqueId()); }

    public Seat getSeatByEntity(LivingEntity entity) { return seats.get(entity.getUniqueId()); }

    public void removeAllSeats() { for(Seat seat : new ArrayList<>(seats.values())) removeSeat(seat, StopReason.PLUGIN); }

    public boolean isBlockWithSeat(Block block) { return blockSeats.containsKey(block); }

    public boolean isEntityBlocked(Entity entity) { return entityBlocked.contains(entity.getUniqueId()); }

    public Set<Seat> getSeatsByBlock(Block block) { return blockSeats.getOrDefault(block, Collections.emptySet()); }

    public boolean kickSeatEntitiesFromBlock(Block block, LivingEntity entity) {
        if(!isBlockWithSeat(block)) return true;
        if(!gSitMain.getPermissionService().hasPermission(entity, "Kick.Sit", "Kick.*")) return false;
        for(Seat seat : getSeatsByBlock(block)) if(!removeSeat(seat, StopReason.KICKED)) return false;
        return true;
    }

    public Seat createSeat(Block block, LivingEntity entity) { return createSeat(block, entity, true, 0d, 0d, 0d, entity.getLocation().getYaw(), gSitMain.getConfigService().CENTER_BLOCK); }

    public Seat createSeat(Block block, LivingEntity entity, boolean canRotate, double xOffset, double yOffset, double zOffset, float seatRotation, boolean sitInBlockCenter) {
        if(entityBlocked.contains(entity.getUniqueId())) return null;

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

        Seat seat = new Seat(block, seatLocation, entity, seatEntity, returnLocation);
        seats.put(entity.getUniqueId(), seat);
        blockSeats.computeIfAbsent(block, b -> new HashSet<>()).add(seat);
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

    public void moveSeat(Seat seat, BlockFace blockDirection) {
        if(seat.getEntity() instanceof Player player) {
            PlayerMoveEvent playerMoveEvent = new PlayerMoveEvent(player, player.getLocation(), player.getLocation().add(blockDirection.getModX(), blockDirection.getModY(), blockDirection.getModZ()));
            Bukkit.getPluginManager().callEvent(playerMoveEvent);
            if(playerMoveEvent.isCancelled()) return;
        }

        Set<Seat> blockSeatList = blockSeats.get(seat.getBlock());
        if(blockSeatList != null) blockSeatList.remove(seat);
        seat.setBlock(seat.getBlock().getRelative(blockDirection));
        blockSeats.computeIfAbsent(seat.getBlock(), b -> new HashSet<>()).add(seat);
        seat.setLocation(seat.getLocation().add(blockDirection.getModX(), blockDirection.getModY(), blockDirection.getModZ()));
        gSitMain.getEntityUtil().setEntityLocation(seat.getSeatEntity(), seat.getLocation());
    }

    public boolean removeSeat(Seat seat, StopReason stopReason) { return removeSeat(seat, stopReason, true); }

    public boolean removeSeat(Seat seat, StopReason stopReason, boolean useSafeDismount) {
        PreEntityStopSitEvent preEntityStopSitEvent = new PreEntityStopSitEvent(seat, stopReason);
        Bukkit.getPluginManager().callEvent(preEntityStopSitEvent);
        if(preEntityStopSitEvent.isCancelled() && stopReason.isCancellable()) return false;

        Entity entity = seat.getEntity();
        entityBlocked.add(entity.getUniqueId());
        if(useSafeDismount) handleSafeSeatDismount(seat);

        Set<Seat> blockSeatList = blockSeats.remove(seat.getBlock());
        if(blockSeatList != null) {
            blockSeatList.remove(seat);
            if(blockSeatList.isEmpty()) blockSeats.remove(seat.getBlock());
        }
        seats.remove(entity.getUniqueId());
        seat.getSeatEntity().remove();
        gSitMain.getTaskService().runDelayed(() -> entityBlocked.remove(entity.getUniqueId()), 1);
        Bukkit.getPluginManager().callEvent(new EntityStopSitEvent(seat, stopReason));
        sitUsageNanoTime += seat.getLifetimeInNanoSeconds();

        return true;
    }

    public void handleSafeSeatDismount(Seat seat) {
        Entity entity = seat.getEntity();

        try {
            Material blockType = seat.getBlock().getType();
            Location upLocation = seat.getLocation().add(0d, baseOffset + (Tag.STAIRS.isTagged(blockType) ? STAIR_Y_OFFSET : 0d) - gSitMain.getConfigService().S_SITMATERIALS.getOrDefault(blockType, 0d), 0d);

            Location returnLocation = gSitMain.getConfigService().GET_UP_RETURN ? seat.getReturnLocation() : upLocation;

            Location entityLocation = entity.getLocation();

            returnLocation.setYaw(entityLocation.getYaw());
            returnLocation.setPitch(entityLocation.getPitch());

            if(entity.isValid()) gSitMain.getEntityUtil().setEntityLocation(entity, returnLocation);
            if(seat.getSeatEntity().isValid() && !gSitMain.getVersionManager().isNewerOrVersion(17, 0)) gSitMain.getEntityUtil().setEntityLocation(seat.getSeatEntity(), returnLocation);
        } catch(Throwable e) {
            // If we can't access the block, entity or seat entity data in a Folia server environment we ignore this error
            if(!gSitMain.isFoliaServer()) gSitMain.getLogger().log(Level.SEVERE, "Could not safely dismount the entity!", e);
        }
    }

    public Seat createStairSeatForEntity(Block block, LivingEntity entity) {
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