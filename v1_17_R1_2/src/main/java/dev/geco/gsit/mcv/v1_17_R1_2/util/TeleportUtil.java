package dev.geco.gsit.mcv.v1_17_R1_2.util;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.craftbukkit.v1_17_R1.entity.*;

import net.minecraft.network.protocol.game.*;

import dev.geco.gsit.util.*;

public class TeleportUtil implements ITeleportUtil {

    public void teleportEntity(Entity Entity, Location Location, boolean Dismount) {

        if(Entity instanceof Player) {

            ((CraftPlayer) Entity).getHandle().connection.send(new ClientboundPlayerPositionPacket(Location.getX(), Location.getY(), Location.getZ(), Location.getYaw(), Location.getPitch(), ClientboundPlayerPositionPacket.RelativeArgument.unpack(0), 0, Dismount));
        } else ((CraftEntity) Entity).getHandle().dismountTo(Location.getX(), Location.getY(), Location.getZ());
    }

    public void posEntity(org.bukkit.entity.Entity Entity, Location Location) { ((CraftEntity) Entity).getHandle().setPos(Location.getX(), Location.getY(), Location.getZ()); }

}