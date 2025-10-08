package dev.geco.gsit.mcv.v1_20_3.model;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.mcv.v1_20_3.entity.BoxEntity;
import dev.geco.gsit.model.StopReason;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;

public class Crawl implements dev.geco.gsit.model.Crawl {

    private final GSitMain gSitMain = GSitMain.getInstance();
    private final Player player;
    private final ServerPlayer serverPlayer;
    protected final BoxEntity boxEntity;
    private Location blockLocation;
    private boolean boxEntityExist = false;
    protected final BlockData blockData = Material.BARRIER.createBlockData();
    private final Listener listener;
    private final Listener moveListener;
    private final Listener stopListener;
    private boolean finished = false;
    private final long spawnTime = System.nanoTime();

    public Crawl(Player player) {
        this.player = player;

        serverPlayer = ((CraftPlayer) player).getHandle();

        boxEntity = new BoxEntity(player.getLocation());

        listener = new Listener() {
            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void entityToggleSwimEvent(EntityToggleSwimEvent event) { if(event.getEntity() == player) event.setCancelled(true); }

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void playerInteractEvent(PlayerInteractEvent event) {
                if(event.isAsynchronous() || event.getPlayer() != player || blockLocation == null || !blockLocation.getBlock().equals(event.getClickedBlock()) || event.getHand() != EquipmentSlot.HAND) return;
                event.setCancelled(true);
                gSitMain.getTaskService().run(() -> {
                    if(!finished) buildBlock(blockLocation);
                }, false, player);
            }
        };

        moveListener = new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void playerMoveEvent(PlayerMoveEvent event) {
                if(event.isAsynchronous() || event.getPlayer() != player) return;
                Location fromLocation = event.getFrom(), toLocation = event.getTo();
                if(fromLocation.getX() != toLocation.getX() || fromLocation.getZ() != toLocation.getZ() || fromLocation.getY() != toLocation.getY()) tick(toLocation);
            }
        };

        stopListener = new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void playerToggleSneakEvent(PlayerToggleSneakEvent event) { if(!event.isAsynchronous() && event.getPlayer() == player && event.isSneaking()) gSitMain.getCrawlService().stopCrawl(Crawl.this, StopReason.GET_UP); }
        };
    }

    @Override
    public void start() {
        player.setSwimming(true);

        Bukkit.getPluginManager().registerEvents(listener, gSitMain);

        gSitMain.getTaskService().runDelayed(() -> {
            Bukkit.getPluginManager().registerEvents(moveListener, gSitMain);
            if(gSitMain.getConfigService().C_GET_UP_SNEAK) Bukkit.getPluginManager().registerEvents(stopListener, gSitMain);
            tick(player.getLocation());
        }, false, player, 1);
    }

    private void tick(Location location) {
        if(finished || !checkCrawlValid()) return;

        Location tickLocation = location.clone();
        Block locationBlock = tickLocation.getBlock();
        int blockSize = (int) ((tickLocation.getY() - tickLocation.getBlockY()) * 100);
        tickLocation.setY(tickLocation.getBlockY() + (blockSize >= 40 ? 2.49 : 1.49));
        Block aboveBlock = tickLocation.getBlock();
        boolean hasSolidBlockAbove = aboveBlock.getBoundingBox().contains(tickLocation.toVector()) && !aboveBlock.getCollisionShape().getBoundingBoxes().isEmpty();
        boolean canPlaceBlock = isValidArea(locationBlock.getRelative(BlockFace.UP), aboveBlock, blockLocation != null ? blockLocation.getBlock() : null);
        boolean canSetBarrier = canPlaceBlock && (aboveBlock.getType().isAir() || hasSolidBlockAbove);
        if(blockLocation == null || !aboveBlock.equals(blockLocation.getBlock())) {
            destoryBlock();
            if(canSetBarrier && !hasSolidBlockAbove) {
                buildBlock(tickLocation);
                return;
            }
        }

        if(canSetBarrier || hasSolidBlockAbove) {
            destoryEntity();
            return;
        }

        Location playerLocation = location.clone();
        gSitMain.getTaskService().run(() -> {
            if(finished) return;

            int height = locationBlock.getBoundingBox().getHeight() >= 0.4 || playerLocation.getY() % 0.015625 == 0.0 ? (player.getFallDistance() > 0.7 ? 0 : blockSize) : 0;

            playerLocation.setY(playerLocation.getY() + (height >= 40 ? 1.5 : 0.5));

            boxEntity.setRawPeekAmount(height >= 40 ? 100 - height : 0);

            if(!boxEntityExist) {
                boxEntity.setPos(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
                serverPlayer.connection.send(new ClientboundAddEntityPacket(boxEntity));
                boxEntityExist = true;
                serverPlayer.connection.send(new ClientboundSetEntityDataPacket(boxEntity.getId(), boxEntity.getEntityData().getNonDefaultValues()));
            } else {
                serverPlayer.connection.send(new ClientboundSetEntityDataPacket(boxEntity.getId(), boxEntity.getEntityData().getNonDefaultValues()));
                boxEntity.setPosRaw(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ(), true);
                serverPlayer.connection.send(new ClientboundTeleportEntityPacket(boxEntity));
            }
        }, true, playerLocation);
    }

    @Override
    public void stop() {
        finished = true;

        HandlerList.unregisterAll(listener);
        HandlerList.unregisterAll(moveListener);
        HandlerList.unregisterAll(stopListener);

        player.setSwimming(false);

        destoryBlock();
        destoryEntity();
    }

    private void buildBlock(Location location) {
        blockLocation = location;
        if(blockLocation != null) player.sendBlockChange(blockLocation, blockData);
    }

    private void destoryBlock() {
        if(blockLocation == null) return;
        player.sendBlockChange(blockLocation, blockLocation.getBlock().getBlockData());
        blockLocation = null;
    }

    private void destoryEntity() {
        if(!boxEntityExist) return;
        serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(boxEntity.getId()));
        boxEntityExist = false;
    }

    private boolean checkCrawlValid() {
        if(serverPlayer.isInWater() || player.isFlying()) {
            gSitMain.getCrawlService().stopCrawl(this, StopReason.ENVIRONMENT);
            return false;
        }
        return true;
    }

    private boolean isValidArea(Block blockUp, Block aboveBlock, Block locationBlock) { return blockUp.equals(aboveBlock) || blockUp.equals(locationBlock); }

    @Override
    public Player getPlayer() { return player; }

    @Override
    public long getLifetimeInNanoSeconds() { return System.nanoTime() - spawnTime; }

    @Override
    public String toString() { return boxEntity.getUUID().toString(); }

}