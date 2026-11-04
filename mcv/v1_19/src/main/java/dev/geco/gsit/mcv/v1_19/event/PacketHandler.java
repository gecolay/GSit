package dev.geco.gsit.mcv.v1_19.event;

import dev.geco.gsit.GSitMain;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

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

    private Object hidePosingPlayerInvisibility(Object packet) {
        if(!(packet instanceof ClientboundSetEntityDataPacket dataPacket)) return packet;
        if(!gSitMain.getPoseService().isEntityIdPosing(dataPacket.getId())) return packet;
        List<SynchedEntityData.DataItem<?>> items = dataPacket.getUnpackedData();
        if(items == null) return packet;
        for(SynchedEntityData.DataItem<?> item : items) {
            if(item.getAccessor().getId() != 0 || !(item.getValue() instanceof Byte flags) || (flags & INVISIBLE_FLAG) != 0) continue;
            @SuppressWarnings("unchecked") SynchedEntityData.DataItem<Byte> flagItem = (SynchedEntityData.DataItem<Byte>) item;
            flagItem.setValue((byte) (flags | INVISIBLE_FLAG));
        }
        return packet;
    }

}
