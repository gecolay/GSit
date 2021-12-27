package dev.geco.gsit.mcv.v1_18_R1.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.craftbukkit.v1_18_R1.entity.*;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.*;

import dev.geco.gsit.objects.*;

public class TeleportUtil implements ITeleportUtil {

    public void teleport(Player P, Location L) { teleport(P, L, false); }

    public void teleport(Player P, Location L, boolean D) {

        ServerPlayer t = ((CraftPlayer) P).getHandle();

        ClientboundPlayerPositionPacket u = new ClientboundPlayerPositionPacket(L.getX(), L.getY(), L.getZ(), L.getYaw(), L.getPitch(), ClientboundPlayerPositionPacket.RelativeArgument.unpack(0), 0, D);

        t.connection.send(u);

    }

    public void pos(org.bukkit.entity.Entity E, Location L) { ((CraftEntity) E).getHandle().setPos(L.getX(), L.getY(), L.getZ()); }

}