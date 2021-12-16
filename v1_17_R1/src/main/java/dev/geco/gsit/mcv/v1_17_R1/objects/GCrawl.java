package dev.geco.gsit.mcv.v1_17_R1.objects;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;

import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Shulker;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class GCrawl implements IGCrawl {

    private final GSitMain GPM = GSitMain.getInstance();

    private final IGCrawl i = this;

    private final Player p;

    private final ServerPlayer cp;

    protected Shulker s;

    private Location a;

    private boolean b;

    private boolean d;

    protected BlockData m = Material.BARRIER.createBlockData();

    private final Listener li;

    private final Listener lim;

    private final Listener lic;

    public GCrawl(Player Player) {

        this.p = Player;

        this.cp = ((CraftPlayer) p).getHandle();

        this.s = createShulker();

        li = new Listener() {

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void ETogSE(EntityToggleSwimEvent e) {
                if(e.getEntity() == p) {
                    e.setCancelled(true);
                }
            }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void PIntE(PlayerInteractEvent e) {
                if(e.getPlayer() == p && b && e.getClickedBlock().equals(a.getBlock()) && e.getHand() == EquipmentSlot.HAND) {
                    e.setCancelled(true);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            build();
                        }
                    }.runTaskAsynchronously(GPM);
                }
            }

        };

        lim = new Listener() {

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void PMovE(PlayerMoveEvent e) {
                if(e.getPlayer() == p) {
                    Location lf = e.getFrom();
                    Location lt = e.getTo();
                    if(lf.getX() != lt.getX() || lf.getZ() != lt.getZ() || lf.getY() != lt.getY()) {
                        tick(lf);
                    }
                }
            }

        };

        lic = new Listener() {

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void PTogSE(PlayerToggleSneakEvent e) {
                if(e.getPlayer() == p && e.isSneaking()) {
                    GPM.getCrawlManager().stopCrawl(i, GetUpReason.GET_UP);
                }
            }

        };

    }

    public void start() {
        p.setSwimming(true);
        Bukkit.getPluginManager().registerEvents(li, GPM);
        new BukkitRunnable() {
            @Override
            public void run() {

                Bukkit.getPluginManager().registerEvents(lim, GPM);
                if(GPM.getCManager().C_GET_UP_SNEAK) Bukkit.getPluginManager().registerEvents(lic, GPM);
                tick(p.getLocation());

            }
        }.runTaskLaterAsynchronously(GPM, 1);
    }

    private void tick(Location TLocation) {
        if(!checkCrawlValid()) return;
        Location l = TLocation.clone();
        Block o = l.getBlock();
        int f = (int) ((l.getY() - l.getBlockY()) * 100.0);
        l.setX(l.getX());
        l.setY(l.getBlockY() + (f >= 40 ? 2.49 : 1.49));
        l.setZ(l.getZ());
        Block i = l.getBlock();
        boolean j = i.getBoundingBox().contains(l.toVector()) && i.getCollisionShape().getBoundingBoxes().size() > 0;
        boolean g = isValidArea(o.getRelative(BlockFace.UP), i, a != null ? a.getBlock() : null);
        boolean bl = g && (i.getType().isAir() || j);
        if(a == null || !i.equals(a.getBlock())) {
            destory();
            a = l;
            if(bl && !j) build();
        }
        if(!bl && !j) {

            new BukkitRunnable() {
                @Override
                public void run() {

                    Location les = TLocation.clone();
                    int h = o.getBoundingBox().getHeight() >= 0.4 || TLocation.getY() % 0.015625 == 0.0 ? (p.getFallDistance() > 0.7 ? 0 : f) : 0;
                    les.setY(les.getY() + (h >= 40 ? 1.5 : 0.5));
                    s.setRawPeekAmount(h >= 40 ? 100 - h : 0);

                    if(d) {

                        ClientboundSetEntityDataPacket pa = new ClientboundSetEntityDataPacket(s.getId(), s.getEntityData(), true);
                        cp.connection.send(pa);
                        s.teleportToWithTicket(les.getX(), les.getY(), les.getZ());
                        ClientboundTeleportEntityPacket pa2 = new ClientboundTeleportEntityPacket(s);
                        cp.connection.send(pa2);

                    } else {

                        s.setPos(les.getX(), les.getY(), les.getZ());
                        ClientboundAddEntityPacket pa = new ClientboundAddEntityPacket(s);
                        cp.connection.send(pa);
                        ClientboundSetEntityDataPacket pa2 = new ClientboundSetEntityDataPacket(s.getId(), s.getEntityData(), true);
                        cp.connection.send(pa2);
                        d = true;

                    }

                }
            }.runTask(GPM);

        } else destoryEntity();
    }

    public void stop() {
        HandlerList.unregisterAll(li);
        HandlerList.unregisterAll(lim);
        HandlerList.unregisterAll(lic);
        p.setSwimming(false);
        destory();
        destoryEntity();
    }

    private void build() {
        p.sendBlockChange(a, m);
        b = true;
    }

    private void destory() {
        if(b) {
            p.sendBlockChange(a, a.getBlock().getBlockData());
            b = false;
        }
    }

    private void destoryEntity() {
        if(d) {
            cp.connection.send(new ClientboundRemoveEntityPacket(s.getId()));
            d = false;
        }
    }

    private boolean checkCrawlValid() {
        if(cp.isInWater() || p.isFlying()) {
            new BukkitRunnable() {
                @Override
                public void run() {

                    GPM.getCrawlManager().stopCrawl(i, GetUpReason.ACTION);

                }
            }.runTask(GPM);
            return false;
        }
        return true;
    }

    private boolean isValidArea(Block B, Block L, Block A) {
        return B.equals(L) || B.equals(A);
    }

    private Shulker createShulker() {
        Shulker s = new Shulker(EntityType.SHULKER, ((CraftWorld) p.getWorld()).getHandle());
        s.setInvisible(true);
        s.setInvulnerable(true);
        s.setNoAi(true);
        s.setSilent(true);
        s.setAttachFace(Direction.UP);
        s.persist = false;
        return s;
    }

    public Player getPlayer() { return p; }

    public String toString() { return s.getUUID().toString(); }

}