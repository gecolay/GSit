package dev.geco.gsit.manager;

import java.util.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.metadata.*;
import org.bukkit.scheduler.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.*;
import dev.geco.gsit.objects.*;

public class SitManager implements ISitManager {

    private final GSitMain GPM;

    public SitManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    private int feature_used = 0;

    public int getFeatureUsedCount() { return feature_used; }

    public void resetFeatureUsedCount() { feature_used = 0; }

    private final List<GSeat> seats = new ArrayList<>();

    private final HashMap<GSeat, BukkitRunnable> rotate = new HashMap<>();

    public List<GSeat> getSeats() { return new ArrayList<>(seats); }

    public boolean isSitting(LivingEntity Entity) { return getSeat(Entity) != null; }

    public GSeat getSeat(LivingEntity Entity) {

        for(GSeat seat : getSeats()) if(Entity.equals(seat.getEntity())) return seat;

        return null;
    }

    public void clearSeats() { for(GSeat seat : getSeats()) removeSeat(seat.getEntity(), GetUpReason.PLUGIN); }

    public boolean kickSeat(Block Block, LivingEntity Entity) {

        if(GPM.getSitUtil().isSeatBlock(Block)) {

            if(!GPM.getPManager().hasPermission(Entity, "Kick.Sit")) return false;

            for(GSeat seat : GPM.getSitUtil().getSeats(Block)) if(!removeSeat(seat.getEntity(), GetUpReason.KICKED)) return false;
        }

        return true;
    }

    public GSeat createSeat(Block Block, LivingEntity Entity) { return createSeat(Block, Entity, true, 0d, 0d, 0d, Entity.getLocation().getYaw(), GPM.getCManager().CENTER_BLOCK, true); }

    public GSeat createSeat(Block Block, LivingEntity Entity, boolean Rotate, double XOffset, double YOffset, double ZOffset, float SeatRotation, boolean SitAtBlock, boolean GetUpSneak) {

        PreEntitySitEvent preEvent = new PreEntitySitEvent(Entity, Block);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return null;

        double offset = SitAtBlock ? Block.getBoundingBox().getMinY() + Block.getBoundingBox().getHeight() : 0d;

        offset = (SitAtBlock ? offset == 0d ? 1d : offset - Block.getY() : offset) + GPM.getCManager().S_SITMATERIALS.getOrDefault(Block.getType(), 0d);

        Location location = Entity.getLocation().clone();

        Location returnLocation = location.clone();

        if(SitAtBlock) {

            location = Block.getLocation().clone().add(0.5d + XOffset, YOffset + offset - 0.2d, 0.5d + ZOffset);
        } else {

            location = location.add(XOffset, YOffset - 0.2d + GPM.getCManager().S_SITMATERIALS.getOrDefault(Block.getType(), 0d), ZOffset);
        }

        if(!GPM.getSpawnUtil().checkLocation(location)) return null;

        location.setYaw(SeatRotation);

        Entity seatEntity = GPM.getSpawnUtil().createSeatEntity(location, Entity);

        if(GPM.getCManager().S_SIT_MESSAGE && Entity instanceof Player) GPM.getMManager().sendActionBarMessage((Player) Entity, "Messages.action-sit-info");

        GSeat seat = new GSeat(Block, location, Entity, seatEntity, returnLocation);

        seatEntity.setMetadata(GPM.NAME, new FixedMetadataValue(GPM, seat));

        seats.add(seat);

        GPM.getSitUtil().setSeatBlock(Block, seat);

        if(Rotate) startRotateSeat(seat);

        feature_used++;

        Bukkit.getPluginManager().callEvent(new EntitySitEvent(seat));

        return seat;
    }

    public void moveSeat(LivingEntity Entity, BlockFace BlockFace) {

        if(!isSitting(Entity)) return;

        GSeat seat = getSeat(Entity);

        new BukkitRunnable() {

            @Override
            public void run() {

                GPM.getSitUtil().removeSeatBlock(seat.getBlock(), seat);

                seat.setBlock(seat.getBlock().getRelative(BlockFace));

                seat.setLocation(seat.getLocation().add(BlockFace.getModX(), BlockFace.getModY(), BlockFace.getModZ()));

                GPM.getSitUtil().setSeatBlock(seat.getBlock(), seat);

                GPM.getPlayerUtil().posEntity(seat.getSeatEntity(), seat.getLocation());
            }
        }.runTaskLater(GPM, 0);
    }

    private void startRotateSeat(GSeat Seat) {

        if(rotate.containsKey(Seat)) stopRotateSeat(Seat);

        BukkitRunnable task = new BukkitRunnable() {

            @Override
            public void run() {

                if(!seats.contains(Seat) || Seat.getSeatEntity().getPassengers().isEmpty()) {

                    cancel();
                    return;
                }

                Location location = Seat.getSeatEntity().getPassengers().get(0).getLocation();
                Seat.getSeatEntity().setRotation(location.getYaw(), location.getPitch());
            }
        };

        task.runTaskTimer(GPM, 0, 2);

        rotate.put(Seat, task);
    }

    protected void stopRotateSeat(GSeat Seat) {

        if(!rotate.containsKey(Seat)) return;

        BukkitRunnable task = rotate.get(Seat);

        if(task != null) task.cancel();

        rotate.remove(Seat);
    }

    public boolean removeSeat(LivingEntity Entity, GetUpReason Reason) { return removeSeat(Entity, Reason, true); }

    public boolean removeSeat(LivingEntity Entity, GetUpReason Reason, boolean Safe) {

        if(!isSitting(Entity)) return true;

        GSeat seat = getSeat(Entity);

        PreEntityGetUpSitEvent preEvent = new PreEntityGetUpSitEvent(seat, Reason);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return false;

        GPM.getSitUtil().removeSeatBlock(seat.getBlock(), seat);

        seats.remove(seat);

        stopRotateSeat(seat);

        Location returnLocation = (GPM.getCManager().GET_UP_RETURN ? seat.getReturn() : seat.getLocation().add(0d, 0.2d + (Tag.STAIRS.isTagged(seat.getBlock().getType()) ? ISitManager.STAIR_Y_OFFSET : 0d) - GPM.getCManager().S_SITMATERIALS.getOrDefault(seat.getBlock().getType(), 0d), 0d));

        if(!GPM.getCManager().GET_UP_RETURN) {

            returnLocation.setYaw(seat.getEntity().getLocation().getYaw());
            returnLocation.setPitch(seat.getEntity().getLocation().getPitch());
        }

        if(seat.getEntity().isValid() && Safe && NMSManager.isNewerOrVersion(17, 0)) {

            GPM.getPlayerUtil().posEntity(seat.getEntity(), returnLocation);
            GPM.getPlayerUtil().teleportEntity(seat.getEntity(), returnLocation, true);
        }

        if(seat.getSeatEntity().isValid()) {

            if(!NMSManager.isNewerOrVersion(17, 0)) GPM.getPlayerUtil().posEntity(seat.getSeatEntity(), returnLocation);

            seat.getSeatEntity().remove();
        }

        Bukkit.getPluginManager().callEvent(new EntityGetUpSitEvent(seat, Reason));

        return true;
    }

}