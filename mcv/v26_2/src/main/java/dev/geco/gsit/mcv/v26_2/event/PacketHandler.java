package dev.geco.gsit.mcv.v26_2.event;

import dev.geco.gsit.GSitMain;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.logging.Level;

public class PacketHandler implements dev.geco.gsit.event.PacketHandler {

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
                    if(handlePacket(packet, player)) return;
                    super.write(channelHandlerContext, packet, channelPromise);
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

    private ChannelPipeline getPipeline(ServerPlayer serverPlayer) throws NoSuchFieldException, IllegalAccessException {
        ServerCommonPacketListenerImpl serverGamePacketListener = serverPlayer.connection;
        if(!gSitMain.isPaperServer()) {
            Field connectionField = ServerCommonPacketListenerImpl.class.getDeclaredField("connection");
            connectionField.setAccessible(true);
            Connection connection = (Connection) connectionField.get(serverGamePacketListener);
            return connection.channel.pipeline();
        }
        return serverGamePacketListener.connection.channel.pipeline();
    }

    private boolean handlePacket(Object packet, Player player) {
        if(!(packet instanceof ClientboundSystemChatPacket clientboundSystemChatPacket)) return false;
        if(!clientboundSystemChatPacket.overlay()) return false;
        if(!(clientboundSystemChatPacket.content() instanceof MutableComponent mutableComponent)) return false;
        if(!(mutableComponent.getContents() instanceof TranslatableContents translatableContents)) return false;
        if(!translatableContents.getKey().equals("build.tooHigh")) return false;
        return gSitMain.getSitService().isEntitySitting(player);
    }

}