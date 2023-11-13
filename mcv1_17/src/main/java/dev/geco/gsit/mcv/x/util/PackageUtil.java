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
import dev.geco.gsit.util.*;

public class PackageUtil extends ChannelOutboundHandlerAdapter implements IPackageUtil {

    private final GSitMain GPM = GSitMain.getInstance();
    private final Player player;

    private Field addEntityYField;
    private final HashMap<Player, Channel> players = new HashMap<>();

    public int getProtocolVersion() { return SharedConstants.getProtocolVersion(); }

    public PackageUtil() { player = null; }

    public PackageUtil(Player Player) {
        player = Player;
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
            channel.pipeline().addBefore("packet_handler", GPM.NAME.toLowerCase(), new PackageUtil(Player));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void registerPlayers() { for(Player player : Bukkit.getOnlinePlayers()) registerPlayer(player); }

    public void unregisterPlayer(Player Player) {
        Channel channel = players.get(Player);
        if(channel == null) return;
        if(channel.pipeline().get(GPM.NAME.toLowerCase()) != null) channel.pipeline().remove(GPM.NAME.toLowerCase());
        players.remove(Player);
    }

    public void unregisterPlayers() { for(Player player : Bukkit.getOnlinePlayers()) unregisterPlayer(player); }

    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        if(msg instanceof ClientboundTeleportEntityPacket) {
            ClientboundTeleportEntityPacket packet = (ClientboundTeleportEntityPacket) msg;
            if(GPM.getEntityUtil().getSeatMap().containsKey(packet.getId())) return;
        }

        if(msg instanceof ClientboundAddMobPacket) {
            ClientboundAddMobPacket packet = (ClientboundAddMobPacket) msg;
            modifyClientboundAddMobPacket(packet);
        }

        super.write(ctx, msg, promise);
    }

    private void modifyClientboundAddMobPacket(ClientboundAddMobPacket Packet) {

        try {
            addEntityYField.set(Packet, Packet.getY() + GPM.getViaVersionLink().getVersionOffset(player));
        } catch (Exception e) { e.printStackTrace(); }
    }

}