package dev.geco.gsit.mcv.v1_18_R1.util;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.craftbukkit.v1_18_R1.entity.*;

import net.minecraft.network.protocol.game.*;

import dev.geco.gsit.util.*;

public class PlayerUtil implements IPlayerUtil {

    public void teleportPlayer(Player Player, Location Location) { teleportPlayer(Player, Location, false); }

    public void teleportPlayer(Player Player, Location Location, boolean Dismount) { ((CraftPlayer) Player).getHandle().connection.send(new ClientboundPlayerPositionPacket(Location.getX(), Location.getY(), Location.getZ(), Location.getYaw(), Location.getPitch(), ClientboundPlayerPositionPacket.RelativeArgument.unpack(0), 0, Dismount)); }

    public void teleportEntity(org.bukkit.entity.Entity Entity, Location Location) { ((CraftEntity) Entity).getHandle().setPos(Location.getX(), Location.getY(), Location.getZ()); }

}