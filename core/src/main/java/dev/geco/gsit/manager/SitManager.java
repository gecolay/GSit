package dev.geco.gsit.manager;

import java.util.*;
import java.util.stream.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.player.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.*;
import dev.geco.gsit.objects.*;
import dev.geco.gsit.util.*;

public class SitManager {

    private final GSitMain GPM;

    public final double BASE_OFFSET;

    public SitManager(GSitMain GPluginMain) {
        GPM = GPluginMain;
        BASE_OFFSET = GPM.getSVManager().isNewerOrVersion(20, 2) ? -0.05d : 0.2d;
    }

    private int sit_used = 0;
    private long sit_used_nano = 0;

    public int getSitUsedCount() { return sit_used; }
    public long getSitUsedSeconds() { return sit_used_nano  / 1_000_000_000; }

    public void resetFeatureUsedCount() {
        sit_used = 0;
        sit_used_nano = 0;
    }

    private final List<GSeat> seats = new ArrayList<>();

    public List<GSeat> getSeats() { return new ArrayList<>(seats); }

    public boolean isSitting(LivingEntity Entity) { return getSeat(Entity) != null; }

    public GSeat getSeat(LivingEntity Entity) { return getSeats().stream().filter(seat -> Entity.equals(seat.getEntity())).findFirst().orElse(null); }

    public void clearSeats() { for(GSeat seat : getSeats()) removeSeat(seat.getEntity(), GetUpReason.PLUGIN); }

    public boolean isSeatBlock(Block Block) { return getSeats().stream().anyMatch(seat -> Block.equals(seat.getBlock())); }

    public List<GSeat> getSeats(Block Block) { return getSeats().stream().filter(seat -> Block.equals(seat.getBlock())).collect(Collectors.toList()); }

    public List<GSeat> getSeats(List<Block> Blocks) { return getSeats().stream().filter(seat -> Blocks.contains(seat.getBlock())).collect(Collectors.toList()); }

    public boolean kickSeat(Block Block, LivingEntity Entity) {

        if(isSeatBlock(Block)) {

            if(!GPM.getPManager().hasPermission(Entity, "Kick.Sit")) return false;

            for(GSeat seat : getSeats(Block)) if(!removeSeat(seat.getEntity(), GetUpReason.KICKED)) return false;
        }

        return true;
    }

    public GSeat createSeat(Block Block, LivingEntity Entity) { return createSeat(Block, Entity, true, 0d, 0d, 0d, Entity.getLocation().getYaw(), GPM.getCManager().CENTER_BLOCK); }

    public GSeat createSeat(Block Block, LivingEntity Entity, boolean Rotate, double XOffset, double YOffset, double ZOffset, float SeatRotation, boolean SitAtBlock) {

        Location returnLocation = Entity.getLocation();

        Location seatLocation = getSeatLocation(Block, returnLocation, XOffset, YOffset, ZOffset, SitAtBlock);

        if(!GPM.getEntityUtil().isLocationValid(seatLocation)) return null;

        PreEntitySitEvent preEvent = new PreEntitySitEvent(Entity, Block);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return null;

        seatLocation.setYaw(SeatRotation);

        Entity seatEntity = GPM.getEntityUtil().createSeatEntity(seatLocation, Entity, Rotate);

        if(seatEntity == null) return null;

        if(GPM.getCManager().CUSTOM_MESSAGE && Entity instanceof Player) {

            GPM.getMManager().sendActionBarMessage((Player) Entity, "Messages.action-sit-info");

            if(GPM.getCManager().ENHANCED_COMPATIBILITY) {

                GPM.getTManager().runDelayed(() -> {
                    GPM.getMManager().sendActionBarMessage((Player) Entity, "Messages.action-sit-info");
                }, Entity, 2);
            }
        }

        GSeat seat = new GSeat(Block, seatLocation, Entity, seatEntity, returnLocation);

        seats.add(seat);

        sit_used++;

        Bukkit.getPluginManager().callEvent(new EntitySitEvent(seat));

        return seat;
    }

    public Location getSeatLocation(Block Block, Location EntityLocation, double XOffset, double YOffset, double ZOffset, boolean SitAtBlock) {

        double offset = SitAtBlock ? Block.getBoundingBox().getMinY() + Block.getBoundingBox().getHeight() : 0d;

        offset = (SitAtBlock ? offset == 0d ? 1d : offset - Block.getY() : offset) + GPM.getCManager().S_SITMATERIALS.getOrDefault(Block.getType(), 0d);

        if(SitAtBlock) return Block.getLocation().add(0.5d + XOffset, YOffset - BASE_OFFSET + offset, 0.5d + ZOffset);

        return EntityLocation.add(XOffset, YOffset - BASE_OFFSET + GPM.getCManager().S_SITMATERIALS.getOrDefault(Block.getType(), 0d), ZOffset);
    }

    public void moveSeat(LivingEntity Entity, BlockFace BlockFace) {

        GSeat seat = getSeat(Entity);

        if(seat == null) return;

        if(Entity instanceof Player) {

            Player player = (Player) Entity;

            PlayerMoveEvent playerMoveEvent = new PlayerMoveEvent(player, player.getLocation(), player.getLocation().add(BlockFace.getModX(), BlockFace.getModY(), BlockFace.getModZ()));

            Bukkit.getPluginManager().callEvent(playerMoveEvent);

            if(playerMoveEvent.isCancelled()) return;
        }

        seat.setBlock(seat.getBlock().getRelative(BlockFace));

        seat.setLocation(seat.getLocation().add(BlockFace.getModX(), BlockFace.getModY(), BlockFace.getModZ()));

        GPM.getEntityUtil().posEntity(seat.getSeatEntity(), seat.getLocation());
    }

    public boolean removeSeat(LivingEntity Entity, GetUpReason Reason) { return removeSeat(Entity, Reason, true); }

    public boolean removeSeat(LivingEntity Entity, GetUpReason Reason, boolean Safe) {

        GSeat seat = getSeat(Entity);

        if(seat == null) return true;

        PreEntityGetUpSitEvent preEvent = new PreEntityGetUpSitEvent(seat, Reason);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return false;

        seats.remove(seat);

        Location returnLocation = seat.getReturn();

        if(!GPM.getCManager().GET_UP_RETURN) {

            try {

                returnLocation = seat.getLocation().add(0d, BASE_OFFSET + (Tag.STAIRS.isTagged(seat.getBlock().getType()) ? EnvironmentUtil.STAIR_Y_OFFSET : 0d) - GPM.getCManager().S_SITMATERIALS.getOrDefault(seat.getBlock().getType(), 0d), 0d);
                returnLocation.setYaw(seat.getEntity().getLocation().getYaw());
                returnLocation.setPitch(seat.getEntity().getLocation().getPitch());
            } catch (Throwable ignored) { }
        }

        if(seat.getEntity().isValid() && Safe && GPM.getPackageUtil() != null) GPM.getEntityUtil().posEntity(seat.getEntity(), returnLocation);

        if(seat.getSeatEntity().isValid() && GPM.getPackageUtil() == null) GPM.getEntityUtil().posEntity(seat.getSeatEntity(), returnLocation);

        GPM.getEntityUtil().removeSeatEntity(seat.getSeatEntity());

        Bukkit.getPluginManager().callEvent(new EntityGetUpSitEvent(seat, Reason));

        sit_used_nano += seat.getNano();

        return true;
    }

}