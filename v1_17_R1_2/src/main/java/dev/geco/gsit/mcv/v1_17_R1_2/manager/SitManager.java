package dev.geco.gsit.mcv.v1_17_R1_2.manager;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;

import net.md_5.bungee.api.ChatMessageType;

import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;
import dev.geco.gsit.mcv.v1_17_R1_2.objects.*;
import dev.geco.gsit.api.event.*;

public class SitManager implements ISitManager, Listener {

    private final GSitMain GPM;

    public SitManager(GSitMain GPluginMain) {
        GPM = GPluginMain;
        GPM.getServer().getPluginManager().registerEvents(this, GPM);
    }

    private int feature_used = 0;

    public int getFeatureUsedCount() { return feature_used; }

    public void resetFeatureUsedCount() { feature_used = 0; }

    private final HashMap<GSeat, SeatArmorStand> seats = new HashMap<GSeat, SeatArmorStand>();

    private final HashMap<GSeat, BukkitRunnable> detect = new HashMap<GSeat, BukkitRunnable>();

    private final HashMap<GSeat, BukkitRunnable> rotate = new HashMap<GSeat, BukkitRunnable>();

    public List<GSeat> getSeats() { return new ArrayList<GSeat>(seats.keySet()); }

    public boolean isSitting(Player Player) { return getSeat(Player) != null; }

    public GSeat getSeat(Player Player) {
        for(GSeat s : getSeats()) if(Player.equals(s.getPlayer())) return s;
        return null;
    }

    public void clearSeats() { for(GSeat s : getSeats()) removeSeat(s, GetUpReason.PLUGIN); }

    public boolean kickSeat(Block Block, Player Player) {

        if(GPM.getSitUtil().isSeatBlock(Block)) {

            if(!GPM.getPManager().hasPermission(Player, "Kick.Sit", "Kick.*")) return false;

            for(GSeat s : GPM.getSitUtil().getSeats(Block)) if(!removeSeat(s, GetUpReason.KICKED)) return false;

        }

        return true;

    }

    public GSeat createSeat(Block Block, Player Player) { return createSeat(Block, Player, true, 0d, 0d, 0d, Player.getLocation().getYaw(), GPM.getCManager().S_BLOCK_CENTER); }

    public GSeat createSeat(Block Block, Player Player, boolean Rotate, double XOffset, double YOffset, double ZOffset, float SeatRotation, boolean SitAtBlock) {

        PrePlayerSitEvent pplase = new PrePlayerSitEvent(Player, Block);

        Bukkit.getPluginManager().callEvent(pplase);

        if(pplase.isCancelled()) return null;

        double o = SitAtBlock ? Block.getBoundingBox().getMinY() + Block.getBoundingBox().getHeight() : 0d;

        o = (SitAtBlock ? o == 0d ? 1d : o - Block.getY() : o) + GPM.getCManager().S_SITMATERIALS.getOrDefault(Block.getType(), 0d);

        ServerPlayer t = ((CraftPlayer) Player).getHandle();

        Location l = Player.getLocation().clone();

        Location r = l.clone();

        if(SitAtBlock) {

            l = Block.getLocation().clone().add(0.5d + XOffset, YOffset + o - 0.2d, 0.5d + ZOffset);

        } else {

            l = l.add(XOffset, YOffset - 0.2d, ZOffset);

        }

        l.setYaw(SeatRotation);

        Level w = ((CraftWorld) l.getWorld()).getHandle();

        SeatArmorStand sa = new SeatArmorStand(w, 0, 0, 0);

        sa.setInvisible(true);
        sa.setSmall(true);
        sa.setNoGravity(true);
        sa.setMarker(true);
        sa.setNoBasePlate(true);
        sa.setInvulnerable(true);
        sa.persist = false;

        sa.setPos(l.getX(), l.getY(), l.getZ());

        sa.setYRot(l.getYaw());
        sa.setXRot(l.getPitch());

        ClientboundAddEntityPacket pa = new ClientboundAddEntityPacket(sa);

        for(Player z : l.getWorld().getPlayers()) {
            ServerPlayer sp = ((CraftPlayer) z).getHandle();
            sp.connection.send(pa);
        }

        ClientboundSetEntityDataPacket pa2 = new ClientboundSetEntityDataPacket(sa.getId(), sa.getEntityData(), true);

        for(Player z : l.getWorld().getPlayers()) {
            ServerPlayer sp = ((CraftPlayer) z).getHandle();
            sp.connection.send(pa2);
        }

        t.absMoveTo(l.getX(), l.getY(), l.getZ());

        t.startRiding(sa, true);

        ClientboundSetPassengersPacket pa3 = new ClientboundSetPassengersPacket(sa);

        for(Player z : l.getWorld().getPlayers()) {
            ServerPlayer sp = ((CraftPlayer) z).getHandle();
            sp.connection.send(pa3);
        }

        if(GPM.getCManager().S_SHOW_SIT_MESSAGE) Player.spigot().sendMessage(ChatMessageType.ACTION_BAR, GPM.getMManager().getComplexMessage(GPM.getMManager().getRawMessage("Messages.action-sit-info")));

        GSeat seat = new GSeat(Block, l, Player, sa.getBukkitEntity(), r);

        sa.getBukkitEntity().setMetadata(GPM.NAME, new FixedMetadataValue(GPM, seat));

        seats.put(seat, sa);

        GPM.getSitUtil().setSeatBlock(Block, seat);

        if(Rotate) startRotateSeat(seat);

        if(GPM.getCManager().GET_UP_SNEAK) startDetectSeat(seat);

        feature_used++;

        Bukkit.getPluginManager().callEvent(new PlayerSitEvent(seat));

        return seat;

    }

    protected void showSeat(GSeat Seat, Player Player) {

        SeatArmorStand sa = seats.get(Seat);

        ServerPlayer sp = ((CraftPlayer) Player).getHandle();

        ClientboundAddEntityPacket pa = new ClientboundAddEntityPacket(sa);

        sp.connection.send(pa);

        ClientboundSetEntityDataPacket pa2 = new ClientboundSetEntityDataPacket(sa.getId(), sa.getEntityData(), true);

        sp.connection.send(pa2);

        ClientboundSetPassengersPacket pa3 = new ClientboundSetPassengersPacket(sa);

        sp.connection.send(pa3);

    }

    protected void startDetectSeat(GSeat Seat) {

        if(detect.containsKey(Seat)) stopDetectSeat(Seat);

        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {

                if(Seat.getPlayer().isSneaking()) {
                    cancel();
                    Seat.getPlayer().setSneaking(false);
                    removeSeat(Seat, GetUpReason.GET_UP);
                }

            }
        };

        r.runTaskTimer(GPM, 0, 1);

        detect.put(Seat, r);

    }

    protected void stopDetectSeat(GSeat Seat) {

        if(!detect.containsKey(Seat)) return;

        BukkitRunnable r = detect.get(Seat);

        if(r != null && !r.isCancelled()) r.cancel();

        detect.remove(Seat);

    }

    public void moveSeat(GSeat Seat, BlockFace Face) {

        new BukkitRunnable() {
            @Override
            public void run() {

                GPM.getSitUtil().removeSeatBlock(Seat.getBlock(), Seat);

                Seat.setBlock(Seat.getBlock().getRelative(Face));

                Seat.setLocation(Seat.getLocation().add(Face.getModX(), Face.getModY(), Face.getModZ()));

                GPM.getSitUtil().setSeatBlock(Seat.getBlock(), Seat);

                SeatArmorStand sa = seats.get(Seat);

                sa.moveTo(Seat.getLocation().getX(), Seat.getLocation().getY(), Seat.getLocation().getZ());

                ClientboundTeleportEntityPacket pa = new ClientboundTeleportEntityPacket(sa);
                for(Player z : Seat.getLocation().getWorld().getPlayers()) {
                    ServerPlayer sp = ((CraftPlayer) z).getHandle();
                    sp.connection.send(pa);
                }

                ServerPlayer t = ((CraftPlayer) Seat.getPlayer()).getHandle();

                t.setPos(Seat.getLocation().getX(), Seat.getLocation().getY(), Seat.getLocation().getZ());

            }
        }.runTaskLater(GPM, 2);

    }

    protected void startRotateSeat(GSeat Seat) {

        if(rotate.containsKey(Seat)) stopRotateSeat(Seat);

        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {

                if(!seats.containsKey(Seat) || Seat.getEntity().getPassengers().isEmpty()) {
                    cancel();
                    return;
                }

                Location l = Seat.getEntity().getPassengers().get(0).getLocation();
                Seat.getEntity().setRotation(l.getYaw(), l.getPitch());

                ClientboundTeleportEntityPacket pa = new ClientboundTeleportEntityPacket(seats.get(Seat));
                for(Player z : Seat.getLocation().getWorld().getPlayers()) {
                    ServerPlayer sp = ((CraftPlayer) z).getHandle();
                    sp.connection.send(pa);
                }

            }
        };

        r.runTaskTimer(GPM, 0, 2);

        rotate.put(Seat, r);

    }

    protected void stopRotateSeat(GSeat Seat) {

        if(!rotate.containsKey(Seat)) return;

        BukkitRunnable r = rotate.get(Seat);

        if(r != null && !r.isCancelled()) r.cancel();

        rotate.remove(Seat);

    }

    public boolean removeSeat(GSeat Seat, GetUpReason Reason) { return removeSeat(Seat, Reason, true); }

    public boolean removeSeat(GSeat Seat, GetUpReason Reason, boolean Safe) {

        PrePlayerGetUpSitEvent pplaguse = new PrePlayerGetUpSitEvent(Seat, Reason);

        Bukkit.getPluginManager().callEvent(pplaguse);

        if(pplaguse.isCancelled()) return false;

        GPM.getSitUtil().removeSeatBlock(Seat.getBlock(), Seat);

        seats.remove(Seat);

        stopDetectSeat(Seat);

        stopRotateSeat(Seat);

        if(Seat.getPlayer().isValid()) {

            ServerPlayer t = ((CraftPlayer) Seat.getPlayer()).getHandle();

            t.stopRiding();

            if(Safe) {

                Location l = (GPM.getCManager().S_GET_UP_RETURN ? Seat.getReturn() : Seat.getLocation()).add(0d, 0.2d, 0d);

                if(!GPM.getCManager().S_GET_UP_RETURN) {
                    l.setYaw(Seat.getPlayer().getLocation().getYaw());
                    l.setPitch(Seat.getPlayer().getLocation().getPitch());
                }

                t.setPos(l.getX(), l.getY(), l.getZ());

                GPM.getTeleportUtil().teleport(Seat.getPlayer(), l, true);

            }

        }

        ClientboundRemoveEntitiesPacket pa = new ClientboundRemoveEntitiesPacket(Seat.getEntity().getEntityId());

        for(Player z : Seat.getLocation().getWorld().getPlayers()) {
            ServerPlayer sp = ((CraftPlayer) z).getHandle();
            sp.connection.send(pa);
        }

        Bukkit.getPluginManager().callEvent(new PlayerGetUpSitEvent(Seat, Reason));

        return true;

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void PJoiE(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        for(Player t : p.getWorld().getPlayers()) {
            if(isSitting(t)) {
                showSeat(getSeat(t), p);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void PChaWE(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        for(Player t : p.getWorld().getPlayers()) {
            if(isSitting(t)) {
                showSeat(getSeat(t), p);
            }
        }
    }

}