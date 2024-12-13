package dev.geco.gsit.mcv.v1_18_2.objects;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.craftbukkit.v1_18_R2.entity.*;

import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class GCrawl implements IGCrawl {

    private final GSitMain GPM = GSitMain.getInstance();

    private final Player player;

    private final ServerPlayer serverPlayer;

    protected final BoxEntity boxEntity;

    private Location blockLocation;

    private boolean boxPresent;

    protected final BlockData blockData = Material.BARRIER.createBlockData();

    private final Listener listener;
    private final Listener moveListener;
    private final Listener stopListener;

    private final long spawnTime = System.nanoTime();

    public GCrawl(Player Player) {

        player = Player;

        serverPlayer = ((CraftPlayer) player).getHandle();

        boxEntity = new BoxEntity(player.getLocation());

        listener = new Listener() {

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void ETogSE(EntityToggleSwimEvent Event) { if(Event.getEntity() == player) Event.setCancelled(true); }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void PIntE(PlayerInteractEvent Event) {

                if(!Event.isAsynchronous() && Event.getPlayer() == player && blockLocation != null && Event.getClickedBlock().equals(blockLocation.getBlock()) && Event.getHand() == EquipmentSlot.HAND) {

                    Event.setCancelled(true);

                    GPM.getTManager().run(() -> {

                        buildBlock(blockLocation);
                    }, false, player);
                }
            }
        };

        moveListener = new Listener() {

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void PMovE(PlayerMoveEvent Event) {

                if(!Event.isAsynchronous() && Event.getPlayer() == player) {

                    Location locationFrom = Event.getFrom(), locationTo = Event.getTo();

                    if(locationFrom.getX() != locationTo.getX() || locationFrom.getZ() != locationTo.getZ() || locationFrom.getY() != locationTo.getY()) tick(locationFrom);
                }
            }
        };

        stopListener = new Listener() {

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void PTogSE(PlayerToggleSneakEvent Event) { if(!Event.isAsynchronous() && Event.getPlayer() == player && Event.isSneaking()) GPM.getCrawlManager().stopCrawl(player, GetUpReason.GET_UP); }
        };
    }

    @Override
    public void start() {

        player.setSwimming(true);

        Bukkit.getPluginManager().registerEvents(listener, GPM);

        GPM.getTManager().runDelayed(() -> {

            Bukkit.getPluginManager().registerEvents(moveListener, GPM);

            if(GPM.getCManager().C_GET_UP_SNEAK) Bukkit.getPluginManager().registerEvents(stopListener, GPM);

            tick(player.getLocation());
        }, false, player, 1);
    }

    private void tick(Location Location) {

        if(!checkCrawlValid()) return;

        Location location = Location.clone();

        Block locationBlock = location.getBlock();

        int blockSize = (int) ((location.getY() - location.getBlockY()) * 100);

        location.setY(location.getBlockY() + (blockSize >= 40 ? 2.49 : 1.49));

        Block aboveBlock = location.getBlock();

        boolean aboveBlockSolid = aboveBlock.getBoundingBox().contains(location.toVector()) && !aboveBlock.getCollisionShape().getBoundingBoxes().isEmpty();
        boolean canPlaceBlock = isValidArea(locationBlock.getRelative(BlockFace.UP), aboveBlock, blockLocation != null ? blockLocation.getBlock() : null);
        boolean canSetBarrier = canPlaceBlock && (aboveBlock.getType().isAir() || aboveBlockSolid);

        if(blockLocation == null || !aboveBlock.equals(blockLocation.getBlock())) {

            destoryBlock();

            if(canSetBarrier && !aboveBlockSolid) buildBlock(location);
        }

        if(!canSetBarrier && !aboveBlockSolid) {

            Location playerLocation = Location.clone();

            GPM.getTManager().run(() -> {

                int height = locationBlock.getBoundingBox().getHeight() >= 0.4 || playerLocation.getY() % 0.015625 == 0.0 ? (player.getFallDistance() > 0.7 ? 0 : blockSize) : 0;

                playerLocation.setY(playerLocation.getY() + (height >= 40 ? 1.5 : 0.5));

                boxEntity.setRawPeekAmount(height >= 40 ? 100 - height : 0);

                if(boxPresent) {

                    serverPlayer.connection.send(new ClientboundSetEntityDataPacket(boxEntity.getId(), boxEntity.getEntityData(), true));
                    boxEntity.teleportToWithTicket(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
                    serverPlayer.connection.send(new ClientboundTeleportEntityPacket(boxEntity));
                } else {

                    boxEntity.setPos(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
                    serverPlayer.connection.send(new ClientboundAddEntityPacket(boxEntity));
                    boxPresent = true;
                    serverPlayer.connection.send(new ClientboundSetEntityDataPacket(boxEntity.getId(), boxEntity.getEntityData(), true));
                }
            }, true, playerLocation);
        } else destoryEntity();
    }

    @Override
    public void stop() {

        HandlerList.unregisterAll(listener);
        HandlerList.unregisterAll(moveListener);
        HandlerList.unregisterAll(stopListener);

        player.setSwimming(false);

        if(blockLocation != null) player.sendBlockChange(blockLocation, blockLocation.getBlock().getBlockData());

        serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(boxEntity.getId()));
    }

    private void buildBlock(Location Location) {

        blockLocation = Location;

        if(blockLocation != null) player.sendBlockChange(blockLocation, blockData);
    }

    private void destoryBlock() {

        if(blockLocation != null) player.sendBlockChange(blockLocation, blockLocation.getBlock().getBlockData());

        blockLocation = null;
    }

    private void destoryEntity() {

        if(!boxPresent) return;

        serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(boxEntity.getId()));

        boxPresent = false;
    }

    private boolean checkCrawlValid() {

        if(serverPlayer.isInWater() || player.isFlying()) {

            GPM.getTManager().run(() -> {

                GPM.getCrawlManager().stopCrawl(player, GetUpReason.ACTION);
            }, true, blockLocation != null ? blockLocation : player.getLocation());

            return false;
        }

        return true;
    }

    private boolean isValidArea(Block BlockUp, Block AboveBlock, Block LocationBlock) { return BlockUp.equals(AboveBlock) || BlockUp.equals(LocationBlock); }

    @Override
    public Player getPlayer() { return player; }

    @Override
    public long getNano() { return System.nanoTime() - spawnTime; }

    @Override
    public String toString() { return boxEntity.getUUID().toString(); }

}