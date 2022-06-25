package dev.geco.gsit.objects;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;

public class GSeat {

    protected Block b;

    protected Location l;

    protected final Player p;

    protected final Entity e;

    protected Location r;

    public GSeat(Block block, Location location, Player player, Entity entity, Location returnloc) {
        b = block;
        l = location;
        p = player;
        e = entity;
        r = returnloc;
    }

    public Block getBlock() { return b; }

    public GSeat setBlock(Block block) {
        b = block;
        return this;
    }

    public Location getLocation() { return l.clone(); }

    public GSeat setLocation(Location location) {
        l = location.clone();
        return this;
    }

    public Player getPlayer() { return p; }

    public Entity getEntity() { return e; }

    public Location getReturn() { return r.clone(); }

    public GSeat setReturn(Location returnloc) {
        r = returnloc.clone();
        return this;
    }

    public String toString() { return e.getUniqueId().toString(); }

}