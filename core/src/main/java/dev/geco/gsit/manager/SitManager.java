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

    public boolean isSitting(Player Player) { return getSeat(Player) != null; }

    public GSeat getSeat(Player Player) {

        for(GSeat seat : getSeats()) if(Player.equals(seat.getPlayer())) return seat;

        return null;
    }

    public void clearSeats() { for(GSeat seat : getSeats()) removeSeat(seat, GetUpReason.PLUGIN); }

    public boolean kickSeat(Block Block, Player Player) {

        if(GPM.getSitUtil().isSeatBlock(Block)) {

            if(!GPM.getPManager().hasPermission(Player, "Kick.Sit")) return false;

            for(GSeat seat : GPM.getSitUtil().getSeats(Block)) if(!removeSeat(seat, GetUpReason.KICKED)) return false;
        }

        return true;
    }

    public GSeat createSeat(Block Block, Player Player) { return createSeat(Block, Player, true, 0d, 0d, 0d, Player.getLocation().getYaw(), GPM.getCManager().CENTER_BLOCK, true); }

    public GSeat createSeat(Block Block, Player Player, boolean Rotate, double XOffset, double YOffset, double ZOffset, float SeatRotation, boolean SitAtBlock, boolean GetUpSneak) {

        PrePlayerSitEvent preEvent = new PrePlayerSitEvent(Player, Block);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return null;

        double offset = SitAtBlock ? Block.getBoundingBox().getMinY() + Block.getBoundingBox().getHeight() : 0d;

        offset = (SitAtBlock ? offset == 0d ? 1d : offset - Block.getY() : offset) + GPM.getCManager().S_SITMATERIALS.getOrDefault(Block.getType(), 0d);

        Location location = Player.getLocation().clone();

        Location returnLocation = location.clone();

        if(SitAtBlock) {

            location = Block.getLocation().clone().add(0.5d + XOffset, YOffset + offset - 0.2d, 0.5d + ZOffset);
        } else {

            location = location.add(XOffset, YOffset - 0.2d + GPM.getCManager().S_SITMATERIALS.getOrDefault(Block.getType(), 0d), ZOffset);
        }

        if(!GPM.getSpawnUtil().checkLocation(location)) return null;

        location.setYaw(SeatRotation);

        Entity seatEntity = GPM.getSpawnUtil().createSeatEntity(location, Player);

        if(GPM.getCManager().S_SIT_MESSAGE) GPM.getMManager().sendActionBarMessage(Player, "Messages.action-sit-info");

        GSeat seat = new GSeat(Block, location, Player, seatEntity, returnLocation);

        seatEntity.setMetadata(GPM.NAME, new FixedMetadataValue(GPM, seat));

        seats.add(seat);

        GPM.getSitUtil().setSeatBlock(Block, seat);

        if(Rotate) startRotateSeat(seat);

        feature_used++;

        Bukkit.getPluginManager().callEvent(new PlayerSitEvent(seat));

        return seat;
    }

    public void moveSeat(GSeat Seat, BlockFace BlockFace) {

        new BukkitRunnable() {

            @Override
            public void run() {

                GPM.getSitUtil().removeSeatBlock(Seat.getBlock(), Seat);

                Seat.setBlock(Seat.getBlock().getRelative(BlockFace));

                Seat.setLocation(Seat.getLocation().add(BlockFace.getModX(), BlockFace.getModY(), BlockFace.getModZ()));

                GPM.getSitUtil().setSeatBlock(Seat.getBlock(), Seat);

                GPM.getPlayerUtil().teleportEntity(Seat.getEntity(), Seat.getLocation());
            }
        }.runTaskLater(GPM, 0);
    }

    private void startRotateSeat(GSeat Seat) {

        if(rotate.containsKey(Seat)) stopRotateSeat(Seat);

        BukkitRunnable task = new BukkitRunnable() {

            @Override
            public void run() {

                if(!seats.contains(Seat) || Seat.getEntity().getPassengers().isEmpty()) {

                    cancel();

                    return;
                }

                Location location = Seat.getEntity().getPassengers().get(0).getLocation();
                Seat.getEntity().setRotation(location.getYaw(), location.getPitch());
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

    public boolean removeSeat(GSeat Seat, GetUpReason Reason) { return removeSeat(Seat, Reason, true); }

    public boolean removeSeat(GSeat Seat, GetUpReason Reason, boolean Safe) {

        PrePlayerGetUpSitEvent preEvent = new PrePlayerGetUpSitEvent(Seat, Reason);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return false;

        GPM.getSitUtil().removeSeatBlock(Seat.getBlock(), Seat);

        seats.remove(Seat);

        stopRotateSeat(Seat);

        Location returnLocation = (GPM.getCManager().GET_UP_RETURN ? Seat.getReturn() : Seat.getLocation().add(0d, 0.2d + (Tag.STAIRS.isTagged(Seat.getBlock().getType()) ? ISitManager.STAIR_Y_OFFSET : 0d) - GPM.getCManager().S_SITMATERIALS.getOrDefault(Seat.getBlock().getType(), 0d), 0d));

        if(!GPM.getCManager().GET_UP_RETURN) {

            returnLocation.setYaw(Seat.getPlayer().getLocation().getYaw());
            returnLocation.setPitch(Seat.getPlayer().getLocation().getPitch());
        }

        if(Seat.getPlayer().isValid() && Safe && NMSManager.isNewerOrVersion(17, 0)) {

            GPM.getPlayerUtil().teleportEntity(Seat.getPlayer(), returnLocation);
            GPM.getPlayerUtil().teleportPlayer(Seat.getPlayer(), returnLocation, true);
        }

        if(Seat.getEntity().isValid()) {

            if(!NMSManager.isNewerOrVersion(17, 0)) GPM.getPlayerUtil().teleportEntity(Seat.getEntity(), returnLocation);

            Seat.getEntity().remove();
        }

        Bukkit.getPluginManager().callEvent(new PlayerGetUpSitEvent(Seat, Reason));

        return true;
    }

}