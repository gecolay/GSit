package dev.geco.gsit.mcv.v26_1.model;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.mcv.v26_1.entity.CrawlEntity;
import dev.geco.gsit.model.StopReason;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class Crawl implements dev.geco.gsit.model.Crawl {

    private final GSitMain gSitMain = GSitMain.getInstance();
    private final Player player;
    private final ServerPlayer serverPlayer;
    protected final CrawlEntity crawlEntity;
    private boolean crawlEntityExist = false;
    private final Listener listener;
    //private final Listener moveListener;
    private final Listener stopListener;
    private boolean finished = false;
    private final long spawnTime = System.nanoTime();
    private final Timer timer = new Timer();

    public Crawl(Player player) {
        this.player = player;

        serverPlayer = ((CraftPlayer) player).getHandle();

        crawlEntity = new CrawlEntity(player.getLocation());

        listener = new Listener() {
            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void entityToggleSwimEvent(EntityToggleSwimEvent event) { if(event.getEntity() == player) event.setCancelled(true); }
        };

        /*moveListener = new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void playerMoveEvent(PlayerMoveEvent event) {
                if(event.isAsynchronous() || event.getPlayer() != player) return;
                Location fromLocation = event.getFrom(), toLocation = event.getTo();
                if(fromLocation.getX() != toLocation.getX() || fromLocation.getZ() != toLocation.getZ() || fromLocation.getY() != toLocation.getY()) tick(toLocation);
            }
        };*/

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
            //Bukkit.getPluginManager().registerEvents(moveListener, gSitMain);
            if(gSitMain.getConfigService().C_GET_UP_SNEAK) Bukkit.getPluginManager().registerEvents(stopListener, gSitMain);
            tick(player.getLocation());

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    tick(player.getLocation());
                }
            }, 0, 1);
        }, false, player, 1);
    }

    private void tick(Location location) {
        if(finished || !checkCrawlValid()) return;

        //player.setSwimming(true);

        Location playerLocation = location.clone();
        gSitMain.getTaskService().run(() -> {
            if(finished) return;

            playerLocation.setY(playerLocation.getY() - (0.625 - (serverPlayer.getScale() * 0.625)) + 0.625);

            if(!crawlEntityExist) {
                crawlEntity.setPos(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
                serverPlayer.connection.send(new ClientboundAddEntityPacket(crawlEntity.getId(), crawlEntity.getUUID(), crawlEntity.getX(), crawlEntity.getY(), crawlEntity.getZ(), crawlEntity.getXRot(), crawlEntity.getYRot(), crawlEntity.getType(), 0, crawlEntity.getDeltaMovement(), crawlEntity.getYHeadRot()));
                crawlEntityExist = true;
                serverPlayer.connection.send(new ClientboundSetEntityDataPacket(crawlEntity.getId(), crawlEntity.getEntityData().getNonDefaultValues()));
            } else {
                serverPlayer.connection.send(new ClientboundSetEntityDataPacket(crawlEntity.getId(), crawlEntity.getEntityData().getNonDefaultValues()));
                crawlEntity.setPosRaw(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
                serverPlayer.connection.send(new ClientboundTeleportEntityPacket(crawlEntity.getId(), net.minecraft.world.entity.PositionMoveRotation.of(crawlEntity), Set.of(), false));
            }
        }, true, playerLocation);
    }

    @Override
    public void stop() {
        finished = true;
        timer.cancel();

        HandlerList.unregisterAll(listener);
        HandlerList.unregisterAll(stopListener);

        player.setSwimming(false);

        destoryEntity();
    }

    private void destoryEntity() {
        if(!crawlEntityExist) return;
        serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(crawlEntity.getId()));
        crawlEntityExist = false;
    }

    private boolean checkCrawlValid() {
        if(serverPlayer.isInWater()) {
            gSitMain.getCrawlService().stopCrawl(this, StopReason.ENVIRONMENT);
            return false;
        }
        return true;
    }

    @Override
    public Player getPlayer() { return player; }

    @Override
    public long getLifetimeInNanoSeconds() { return System.nanoTime() - spawnTime; }

    @Override
    public String toString() { return crawlEntity.getUUID().toString(); }

}