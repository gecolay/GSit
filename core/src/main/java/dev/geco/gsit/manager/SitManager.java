package dev.geco.gsit.manager;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatMessageType;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;
import dev.geco.gsit.api.event.*;

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
        for(GSeat s : getSeats()) if(Player.equals(s.getPlayer())) return s;
        return null;
    }

    public void clearSeats() { for(GSeat s : getSeats()) removeSeat(s, GetUpReason.PLUGIN); }
    
    public boolean kickSeat(Block Block, Player Player) {
        
        if(GPM.getSitUtil().isSeatBlock(Block)) {
            
            if(!GPM.getPManager().hasPermission(Player, "Kick.Sit")) return false;
            
            for(GSeat s : GPM.getSitUtil().getSeats(Block)) if(!removeSeat(s, GetUpReason.KICKED)) return false;
            
        }
        
        return true;
        
    }
    
    public GSeat createSeat(Block Block, Player Player) { return createSeat(Block, Player, true, 0d, 0d, 0d, Player.getLocation().getYaw(), GPM.getCManager().S_BLOCK_CENTER, true); }

    public GSeat createSeat(Block Block, Player Player, boolean Rotate, double XOffset, double YOffset, double ZOffset, float SeatRotation, boolean SitAtBlock, boolean GetUpSneak) {

        PrePlayerSitEvent pplase = new PrePlayerSitEvent(Player, Block);

        Bukkit.getPluginManager().callEvent(pplase);

        if(pplase.isCancelled()) return null;

        double o = SitAtBlock ? Block.getBoundingBox().getMinY() + Block.getBoundingBox().getHeight() : 0d;

        o = (SitAtBlock ? o == 0d ? 1d : o - Block.getY() : o) + GPM.getCManager().S_SITMATERIALS.getOrDefault(Block.getType(), 0d);

        Location l = Player.getLocation().clone();

        Location r = l.clone();

        if(SitAtBlock) {

            l = Block.getLocation().clone().add(0.5d + XOffset, YOffset + o - 0.2d, 0.5d + ZOffset);

        } else {

            l = l.add(XOffset, YOffset - 0.2d + GPM.getCManager().S_SITMATERIALS.getOrDefault(Block.getType(), 0d), ZOffset);

        }

        l.setYaw(SeatRotation);

        ArmorStand sa = l.getWorld().spawn(l, ArmorStand.class, b -> {
            b.setInvisible(true);
            b.setSmall(true);
            b.setGravity(false);
            b.setMarker(true);
            b.setBasePlate(false);
            b.setInvulnerable(true);
        });

        if(sa.isValid()) {

            sa.addPassenger(Player);

        } else return null;

        if(GPM.getCManager().S_SHOW_SIT_MESSAGE) Player.spigot().sendMessage(ChatMessageType.ACTION_BAR, GPM.getMManager().getComplexMessage(GPM.getMManager().getRawMessage("Messages.action-sit-info")));

        GSeat seat = new GSeat(Block, l, Player, sa, r);

        sa.setMetadata(GPM.NAME, new FixedMetadataValue(GPM, seat));

        seats.add(seat);

        GPM.getSitUtil().setSeatBlock(Block, seat);

        if(Rotate) startRotateSeat(seat);

        feature_used++;

        Bukkit.getPluginManager().callEvent(new PlayerSitEvent(seat));
        
        return seat;
        
    }

    public void moveSeat(GSeat Seat, BlockFace Face) {
        
        new BukkitRunnable() {
            @Override
            public void run() {

                GPM.getSitUtil().removeSeatBlock(Seat.getBlock(), Seat);

                Seat.setBlock(Seat.getBlock().getRelative(Face));

                Seat.setLocation(Seat.getLocation().add(Face.getModX(), Face.getModY(), Face.getModZ()));

                GPM.getSitUtil().setSeatBlock(Seat.getBlock(), Seat);

                try {

                    Object sa = NMSManager.getHandle(Seat.getEntity());

                    NMSManager.getMethod("setPosition", sa.getClass(), double.class, double.class, double.class).invoke(sa, Seat.getLocation().getX(), Seat.getLocation().getY(), Seat.getLocation().getZ());

                } catch(Exception e) { e.printStackTrace(); }
                
            }
        }.runTaskLater(GPM, 0);

    }

    protected void startRotateSeat(GSeat Seat) {

        if(rotate.containsKey(Seat)) stopRotateSeat(Seat);

        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {

                if(Seat.getEntity().getPassengers().isEmpty()) {
                    cancel();
                    return;
                }

                Location l = Seat.getEntity().getPassengers().get(0).getLocation();
                Seat.getEntity().setRotation(l.getYaw(), l.getPitch());

            }
        };

        r.runTaskTimer(GPM, 0, 2);

        rotate.put(Seat, r);

    }

    protected void stopRotateSeat(GSeat Seat) {

        if(!rotate.containsKey(Seat)) return;

        BukkitRunnable r = rotate.get(Seat);

        if(r != null) r.cancel();

        rotate.remove(Seat);

    }

    public boolean removeSeat(GSeat Seat, GetUpReason Reason) { return removeSeat(Seat, Reason, true); }

    public boolean removeSeat(GSeat Seat, GetUpReason Reason, boolean Safe) {

        PrePlayerGetUpSitEvent pplaguse = new PrePlayerGetUpSitEvent(Seat, Reason);

        Bukkit.getPluginManager().callEvent(pplaguse);

        if(pplaguse.isCancelled()) return false;

        GPM.getSitUtil().removeSeatBlock(Seat.getBlock(), Seat);

        seats.remove(Seat);

        stopRotateSeat(Seat);

        if(Seat.getEntity() != null && Seat.getEntity().isValid()) {

            Location l = (GPM.getCManager().S_GET_UP_RETURN ? Seat.getReturn() : Seat.getLocation().add(0d, 0.2d + (Tag.STAIRS.isTagged(Seat.getBlock().getType()) ? ISitManager.STAIR_Y_OFFSET : 0d) - GPM.getCManager().S_SITMATERIALS.getOrDefault(Seat.getBlock().getType(), 0d), 0d));

            try {

                Object sa = NMSManager.getHandle(Seat.getEntity());

                NMSManager.getMethod("setPosition", sa.getClass(), double.class, double.class, double.class).invoke(sa, l.getX(), l.getY(), l.getZ());

            } catch(Exception e) { e.printStackTrace(); }

            Seat.getEntity().remove();

        }

        Bukkit.getPluginManager().callEvent(new PlayerGetUpSitEvent(Seat, Reason));
        
        return true;
        
    }
    
}