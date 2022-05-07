package dev.geco.gsit.mcv.v1_17_R1_2.objects;

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

    private final Player p;

    private final ServerPlayer cp;

    protected final Shulker s;

    private Location bloc;

    private boolean build;

    private boolean svalid;

    protected final BlockData m = Material.BARRIER.createBlockData();

    private final Listener li;

    private final Listener lim;

    private final Listener lic;

    public GCrawl(Player Player) {

        p = Player;

        cp = ((CraftPlayer) p).getHandle();

        s = createShulker();

        li = new Listener() {

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void ETogSE(EntityToggleSwimEvent e) {
                if(e.getEntity() == p) {
                    e.setCancelled(true);
                }
            }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void PIntE(PlayerInteractEvent e) {
                if(e.getPlayer() == p && build && e.getClickedBlock().equals(bloc.getBlock()) && e.getHand() == EquipmentSlot.HAND) {
                    e.setCancelled(true);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            buildBlock();
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
                    GPM.getCrawlManager().stopCrawl(GCrawl.this, GetUpReason.GET_UP);
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

    private void tick(Location L) {
        if(!checkCrawlValid()) return;
        Location l = L.clone();
        Block cblock = l.getBlock();
        int bheight = (int) ((l.getY() - l.getBlockY()) * 100.0);
        l.setY(l.getBlockY() + (bheight >= 40 ? 2.49 : 1.49));
        Block topblock = l.getBlock();
        boolean solidblock = topblock.getBoundingBox().contains(l.toVector()) && topblock.getCollisionShape().getBoundingBoxes().size() > 0;
        boolean valid = isValidArea(cblock.getRelative(BlockFace.UP), topblock, bloc != null ? bloc.getBlock() : null);
        boolean canreplace = valid && (topblock.getType().isAir() || solidblock);
        if(bloc == null || !topblock.equals(bloc.getBlock())) {
            destoryBlock();
            bloc = l;
            if(canreplace && !solidblock) buildBlock();
        }
        if(!canreplace && !solidblock) {

            new BukkitRunnable() {
                @Override
                public void run() {

                    Location les = L.clone();
                    int h = cblock.getBoundingBox().getHeight() >= 0.4 || les.getY() % 0.015625 == 0.0 ? (p.getFallDistance() > 0.7 ? 0 : bheight) : 0;
                    les.setY(les.getY() + (h >= 40 ? 1.5 : 0.5));
                    s.setRawPeekAmount(h >= 40 ? 100 - h : 0);

                    if(svalid) {

                        ClientboundSetEntityDataPacket pa = new ClientboundSetEntityDataPacket(s.getId(), s.getEntityData(), true);
                        cp.connection.send(pa);
                        s.teleportToWithTicket(les.getX(), les.getY(), les.getZ());
                        ClientboundTeleportEntityPacket pa2 = new ClientboundTeleportEntityPacket(s);
                        cp.connection.send(pa2);

                    } else {

                        s.setPos(les.getX(), les.getY(), les.getZ());
                        ClientboundAddEntityPacket pa = new ClientboundAddEntityPacket(s);
                        cp.connection.send(pa);
                        svalid = true;
                        ClientboundSetEntityDataPacket pa2 = new ClientboundSetEntityDataPacket(s.getId(), s.getEntityData(), true);
                        cp.connection.send(pa2);

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
        if(bloc != null) p.sendBlockChange(bloc, bloc.getBlock().getBlockData());
        cp.connection.send(new ClientboundRemoveEntitiesPacket(s.getId()));
    }

    private void buildBlock() {
        p.sendBlockChange(bloc, m);
        build = true;
    }

    private void destoryBlock() {
        if(build) {
            p.sendBlockChange(bloc, bloc.getBlock().getBlockData());
            build = false;
        }
    }

    private void destoryEntity() {
        if(svalid) {
            cp.connection.send(new ClientboundRemoveEntitiesPacket(s.getId()));
            svalid = false;
        }
    }

    private boolean checkCrawlValid() {
        if(cp.isInWater() || p.isFlying()) {
            new BukkitRunnable() {
                @Override
                public void run() {

                    GPM.getCrawlManager().stopCrawl(GCrawl.this, GetUpReason.ACTION);

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