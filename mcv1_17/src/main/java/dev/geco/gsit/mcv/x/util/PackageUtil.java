package dev.geco.gsit.mcv.x.util;

import io.netty.channel.*;
import java.lang.reflect.*;
import java.util.*;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_17_R1.entity.*;
import org.bukkit.entity.*;

import net.minecraft.*;
import net.minecraft.network.protocol.game.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.manager.*;
import dev.geco.gsit.util.*;

public class PackageUtil implements IPackageUtil {

    private final GSitMain GPM = GSitMain.getInstance();

    private Field addEntityYField;
    private final HashMap<Player, Channel> players = new HashMap<>();

    public int getProtocolVersion() { return SharedConstants.getProtocolVersion(); }

    public PackageUtil() {
        int count = 0;
        for(Field declaredField : ClientboundAddMobPacket.class.getDeclaredFields()) if(declaredField.getType().equals(double.class) && !Modifier.isStatic(declaredField.getModifiers())) {
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
            Channel channel = ((CraftPlayer) Player).getHandle().connection.connection.channel;
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
                    if(packet.getId() == Player.getEntityId() && GPM.getPoseManager().isPosing(Player)) {
                        if(Player.getScoreboardTags().contains(PoseManager.POSE_TAG)) return;
                        Player.addScoreboardTag(PoseManager.POSE_TAG);
                    }
                }

                if(msg instanceof ClientboundTeleportEntityPacket) {
                    ClientboundTeleportEntityPacket packet = (ClientboundTeleportEntityPacket) msg;
                    if(GPM.getEntityUtil().getSeatMap().containsKey(packet.getId())) return;
                }

                if(msg instanceof ClientboundAddMobPacket) {
                    ClientboundAddMobPacket packet = (ClientboundAddMobPacket) msg;
                    modifyClientboundAddMobPacket(Player, packet);
                }

                super.write(ctx, msg, promise);
            }
        };
    }

    private void modifyClientboundAddMobPacket(Player Player, ClientboundAddMobPacket Packet) {
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

    public void unregisterPlayers() { for(Player player : Bukkit.getOnlinePlayers()) unregisterPlayer(player); }

}