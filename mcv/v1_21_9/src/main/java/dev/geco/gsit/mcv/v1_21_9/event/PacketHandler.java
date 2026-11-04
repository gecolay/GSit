package dev.geco.gsit.mcv.v1_21_9.event;

import dev.geco.gsit.GSitMain;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class PacketHandler implements dev.geco.gsit.event.PacketHandler {

    private static final byte INVISIBLE_FLAG = 0x20;

    protected final GSitMain gSitMain;

    public PacketHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @Override
    public void setupPlayerPacketHandlers() { for(Player player : Bukkit.getOnlinePlayers()) setupPlayerPacketHandler(player); }

    @Override
    public void setupPlayerPacketHandler(Player player) {
        try {
            ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
            ChannelPipeline channelPipeline = getPipeline(serverPlayer);
            if(channelPipeline == null) return;
            if(channelPipeline.get(GSitMain.NAME) != null) channelPipeline.remove(GSitMain.NAME);
            ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
                @Override
                public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
                    super.write(channelHandlerContext, hidePosingPlayerInvisibility(packet), channelPromise);
                }
            };
            if(channelPipeline.get("packet_handler") != null) channelPipeline.addBefore("packet_handler", GSitMain.NAME, channelDuplexHandler);
            else channelPipeline.addLast(GSitMain.NAME, channelDuplexHandler);
        } catch(Throwable e) { gSitMain.getLogger().log(Level.SEVERE, "Could not setup player packet handler!", e); }
    }

    @Override
    public void removePlayerPacketHandlers() { for(Player player : Bukkit.getOnlinePlayers()) removePlayerPacketHandler(player); }

    @Override
    public void removePlayerPacketHandler(Player player) {
        try {
            ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
            ChannelPipeline channelPipeline = getPipeline(serverPlayer);
            if(channelPipeline != null && channelPipeline.get(GSitMain.NAME) != null) channelPipeline.remove(GSitMain.NAME);
        } catch(Throwable e) { gSitMain.getLogger().log(Level.SEVERE, "Could not remove player packet handler!", e); }
    }

    private ChannelPipeline getPipeline(ServerPlayer serverPlayer) { return serverPlayer.connection.connection.channel.pipeline(); }

    @SuppressWarnings("unchecked")
    private Object hidePosingPlayerInvisibility(Object packet) {
        if(packet instanceof ClientboundSetEntityDataPacket dataPacket) {
            if(!gSitMain.getPoseService().isEntityIdPosing(dataPacket.id())) return packet;
            List<SynchedEntityData.DataValue<?>> values = forceInvisible(dataPacket.packedItems());
            return values == null ? packet : new ClientboundSetEntityDataPacket(dataPacket.id(), values);
        }
        if(packet instanceof ClientboundBundlePacket bundlePacket) {
            List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
            boolean changed = false;
            for(Packet<? super ClientGamePacketListener> subPacket : bundlePacket.subPackets()) {
                Object newPacket = hidePosingPlayerInvisibility(subPacket);
                if(newPacket != subPacket) changed = true;
                packets.add((Packet<? super ClientGamePacketListener>) newPacket);
            }
            return changed ? new ClientboundBundlePacket(packets) : packet;
        }
        return packet;
    }

    private List<SynchedEntityData.DataValue<?>> forceInvisible(List<SynchedEntityData.DataValue<?>> values) {
        for(int index = 0; index < values.size(); index++) {
            SynchedEntityData.DataValue<?> value = values.get(index);
            if(value.id() != 0 || !(value.value() instanceof Byte flags) || (flags & INVISIBLE_FLAG) != 0) continue;
            List<SynchedEntityData.DataValue<?>> newValues = new ArrayList<>(values);
            newValues.set(index, new SynchedEntityData.DataValue<>(0, EntityDataSerializers.BYTE, (byte) (flags | INVISIBLE_FLAG)));
            return newValues;
        }
        return null;
    }

}
