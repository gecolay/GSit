package dev.geco.gsit.mcv.v1_18.model;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.mcv.v1_18.entity.BoxEntity;
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
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
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
import org.jetbrains.annotations.NotNull;

public class Crawl implements dev.geco.gsit.model.Crawl {

    private final GSitMain gSitMain = GSitMain.getInstance();
    private final Player player;
    private final ServerPlayer serverPlayer;
    private final int layers;
    private final BoxEntity[][] boxEntities;
    private Location blockLocation;
    private boolean boxEntitiesExist = false;
    protected final BlockData blockData = Material.BARRIER.createBlockData();
    private final Listener listener;
    private final Listener moveListener;
    private final Listener stopListener;
    private boolean finished = false;
    private final long spawnTime = System.nanoTime();

    public Crawl(Player player) { this(player, GSitMain.getInstance().getConfigService().C_LAYERS); }

    public Crawl(Player player, int layers) {
        this.player = player;
        this.layers = Math.max(layers, 1);

        serverPlayer = ((CraftPlayer) player).getHandle();

        int size = this.layers * 2 - 1;
        boxEntities = new BoxEntity[size][size];
        for(int x = 0; x < size; x++) for(int z = 0; z < size; z++) boxEntities[x][z] = new BoxEntity(player.getLocation());

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

            int size = boxEntities.length;

            for(int x = 0; x < size; x++) {
                for(int z = 0; z < size; z++) {
                    BoxEntity entity = boxEntities[x][z];
                    entity.setRawPeekAmount(height >= 40 ? 100 - height : 0);

                    double entityX = playerLocation.getX() + x - (layers - 1);
                    double entityZ = playerLocation.getZ() + z - (layers - 1);

                    if(!boxEntitiesExist) {
                        entity.setPos(entityX, playerLocation.getY(), entityZ);
                        serverPlayer.connection.send(new ClientboundAddEntityPacket(entity));
                        serverPlayer.connection.send(new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData(), true));
                    } else {
                        serverPlayer.connection.send(new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData(), true));
                        entity.setPosRaw(entityX, playerLocation.getY(), entityZ);
                        serverPlayer.connection.send(new ClientboundTeleportEntityPacket(entity));
                    }
                }
            }

            boxEntitiesExist = true;
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
        if(!boxEntitiesExist) return;

        int size = boxEntities.length;
        int[] entityIds = new int[size * size];
        int entityIndex = 0;
        for(BoxEntity[] boxEntityLayer : boxEntities) for (BoxEntity boxEntity : boxEntityLayer) entityIds[entityIndex++] = boxEntity.getId();

        serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(entityIds));
        boxEntitiesExist = false;
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
    public @NotNull Player getPlayer() { return player; }

    @Override
    public long getLifetimeInNanoSeconds() { return System.nanoTime() - spawnTime; }

    @Override
    public String toString() { return GSitMain.NAME + "_crawl_" + player.getUniqueId(); }

}