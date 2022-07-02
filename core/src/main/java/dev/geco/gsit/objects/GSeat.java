package dev.geco.gsit.objects;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;

public class GSeat {

    protected Block block;

    protected Location location;

    protected final Player player;

    protected final Entity entity;

    protected Location returnLocation;

    public GSeat(Block Block, Location Location, Player Player, Entity Entity, Location ReturnLocation) {

        block = Block;
        location = Location;
        player = Player;
        entity = Entity;
        returnLocation = ReturnLocation;
    }

    public Block getBlock() { return block; }

    public GSeat setBlock(Block Block) {

        block = Block;

        return this;
    }

    public Location getLocation() { return location.clone(); }

    public GSeat setLocation(Location Location) {

        location = Location.clone();

        return this;
    }

    public Player getPlayer() { return player; }

    public Entity getEntity() { return entity; }

    public Location getReturn() { return returnLocation.clone(); }

    public GSeat setReturn(Location ReturnLocation) {

        returnLocation = ReturnLocation.clone();

        return this;
    }

    public String toString() { return entity.getUniqueId().toString(); }

}