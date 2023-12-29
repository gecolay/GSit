package dev.geco.gsit.mcv.x.util;

import io.netty.channel.*;
import java.lang.reflect.*;
import java.util.*;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R3.entity.*;
import org.bukkit.entity.*;

import net.minecraft.*;
import net.minecraft.network.*;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.network.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.manager.*;
import dev.geco.gsit.util.*;

public class PackageUtil implements IPackageUtil {

    private final GSitMain GPM = GSitMain.getInstance();

    private Field channelField;
    private Field addEntityYField;
    private final HashMap<Player, Channel> players = new HashMap<>();

    public int getProtocolVersion() { return SharedConstants.getProtocolVersion(); }

    public PackageUtil() {
        Class<?> serverGamePacketListenerClass = ServerGamePacketListenerImpl.class;
        if(serverGamePacketListenerClass.getSuperclass() != Object.class) serverGamePacketListenerClass = serverGamePacketListenerClass.getSuperclass();
        for(Field declaredField : serverGamePacketListenerClass.getDeclaredFields()) if(declaredField.getType().equals(Connection.class)) {
            declaredField.setAccessible(true);
            channelField = declaredField;
            break;
        }
        int count = 0;
        for(Field declaredField : ClientboundAddEntityPacket.class.getDeclaredFields()) if(declaredField.getType().equals(double.class) && !Modifier.isStatic(declaredField.getModifiers())) {
            if(count == 1) {
                declaredField.setAccessible(true);
                addEntityYField = declaredField;
                break;
            }
            count++;
        }
    }

    public void registerPlayer(Player Player) {
        try {
            ServerGamePacketListenerImpl packetListener = ((CraftPlayer) Player).getHandle().connection;
            Channel channel = ((Connection) channelField.get(packetListener)).channel;
            players.put(Player, channel);
            channel.pipeline().addBefore("packet_handler", GPM.NAME.toLowerCase(), getHandler(Player));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void registerPlayers() { for(Player player : Bukkit.getOnlinePlayers()) registerPlayer(player); }

    private ChannelDuplexHandler getHandler(Player Player) {

        return new ChannelDuplexHandler() {

            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

                if(msg instanceof ClientboundSetEntityDataPacket) {
                    ClientboundSetEntityDataPacket packet = (ClientboundSetEntityDataPacket) msg;
                    if(packet.id() == Player.getEntityId() && GPM.getPoseManager().isPosing(Player)) {
                        if(Player.getScoreboardTags().contains(PoseManager.POSE_TAG)) return;
                        Player.addScoreboardTag(PoseManager.POSE_TAG);
                    }
                }

                if(msg instanceof ClientboundTeleportEntityPacket) {
                    ClientboundTeleportEntityPacket packet = (ClientboundTeleportEntityPacket) msg;
                    if(GPM.getEntityUtil().getSeatMap().containsKey(packet.getId())) return;
                }

                if(msg instanceof ClientboundAddEntityPacket) {
                    ClientboundAddEntityPacket packet = (ClientboundAddEntityPacket) msg;
                    modifyClientboundAddEntityPacket(Player, packet);
                }

                if(msg instanceof ClientboundBundlePacket bundle) {
                    bundle.subPackets().forEach(bundlePacket -> {
                        if(bundlePacket instanceof ClientboundAddEntityPacket) modifyClientboundAddEntityPacket(Player, (ClientboundAddEntityPacket) bundlePacket);
                    });
                }

                super.write(ctx, msg, promise);
            }
        };
    }

    private void modifyClientboundAddEntityPacket(Player Player, ClientboundAddEntityPacket Packet) {
        if(GPM.getViaVersionLink() == null || !GPM.getEntityUtil().getSeatMap().containsKey(Packet.getId())) return;
        try {
            addEntityYField.set(Packet, Packet.getY() + GPM.getViaVersionLink().getVersionOffset(Player));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void unregisterPlayer(Player Player) {
        Channel channel = players.get(Player);
        if(channel == null) return;
        try {
            if(channel.pipeline().get(GPM.NAME.toLowerCase()) != null) channel.pipeline().remove(GPM.NAME.toLowerCase());
        } catch (Throwable ignored) { }
        players.remove(Player);
    }

    public void unregisterPlayers() { for(Player player : new ArrayList<>(players.keySet())) unregisterPlayer(player); }

}