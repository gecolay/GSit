package dev.geco.gsit.mcv.v1_19_R3.objects;

import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.*;
import org.bukkit.craftbukkit.v1_19_R3.entity.*;

import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class GBoxCrawl implements IGCrawl {

    private final GSitMain GPM = GSitMain.getInstance();

    private final Player player;

    private final ServerPlayer serverPlayer;

    protected final BoxEntity boxEntity;
    protected final net.minecraft.world.entity.Entity baseEntity;

    private boolean boxPresent = false;

    private final Listener listener;
    private final Listener moveListener;

    public GBoxCrawl(Player Player) {

        player = Player;

        serverPlayer = ((CraftPlayer) player).getHandle();

        boxEntity = new BoxEntity(player.getLocation());
        baseEntity = new RotateSeatEntity(player.getLocation());

        listener = new Listener() {

            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            public void ETogSE(EntityToggleSwimEvent Event) { if(Event.getEntity() == player) Event.setCancelled(true); }
        };

        moveListener = new Listener() {

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void PMovE(PlayerMoveEvent Event) { tick(Event.getPlayer(), Event.getTo()); }

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void PTogSE(PlayerToggleSneakEvent Event) { if(GPM.getCManager().C_GET_UP_SNEAK && Event.getPlayer() == player && Event.isSneaking()) GPM.getCrawlManager().stopCrawl(player, GetUpReason.GET_UP); }
        };
    }

    public void start() {

        player.setSwimming(true);

        Bukkit.getPluginManager().registerEvents(listener, GPM);
        Bukkit.getPluginManager().registerEvents(moveListener, GPM);

        tick(player, player.getLocation());
    }

    private void tick(Player Player, Location Location) {

        if(!checkCrawlValid()) return;

        Location location = Location.clone().add(0, Player.getFallDistance() > 0.2 ? 0.6 : 0.75, 0);

        if(boxPresent) {

            baseEntity.teleportToWithTicket(location.getX(), location.getY(), location.getZ());

            serverPlayer.connection.send(new ClientboundTeleportEntityPacket(baseEntity));
        } else {

            boxEntity.setPos(location.getX(), location.getY() + 0.5, location.getZ());
            baseEntity.setPos(location.getX(), location.getY(), location.getZ());

            serverPlayer.connection.send(new ClientboundAddEntityPacket(baseEntity));
            serverPlayer.connection.send(new ClientboundAddEntityPacket(boxEntity));
            serverPlayer.connection.send(new ClientboundSetEntityDataPacket(baseEntity.getId(), baseEntity.getEntityData().isDirty() ? baseEntity.getEntityData().packDirty() : baseEntity.getEntityData().getNonDefaultValues()));
            serverPlayer.connection.send(new ClientboundSetEntityDataPacket(boxEntity.getId(), boxEntity.getEntityData().isDirty() ? boxEntity.getEntityData().packDirty() : boxEntity.getEntityData().getNonDefaultValues()));

            boxEntity.startRiding(baseEntity, true);

            serverPlayer.connection.send(new ClientboundSetPassengersPacket(baseEntity));

            boxPresent = true;
        }
    }

    public void stop() {

        HandlerList.unregisterAll(listener);
        HandlerList.unregisterAll(moveListener);

        player.setSwimming(false);

        serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(boxEntity.getId()));
        serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(baseEntity.getId()));
    }

    private boolean checkCrawlValid() {

        if(serverPlayer.isInWater() || player.isFlying()) {

            new BukkitRunnable() {

                @Override
                public void run() {

                    GPM.getCrawlManager().stopCrawl(player, GetUpReason.ACTION);
                }
            }.runTask(GPM);

            return false;
        }

        return true;
    }

    public Player getPlayer() { return player; }

    public String toString() { return boxEntity.getUUID().toString(); }

}