package dev.geco.gsit.link;

import org.bukkit.entity.*;

import com.viaversion.viaversion.api.*;

import dev.geco.gsit.GSitMain;

public class ViaVersionLink {

    private final GSitMain GPM;

    private final int ORIGIN_VERSION;

    public ViaVersionLink(GSitMain GPluginMain) {
        GPM = GPluginMain;
        ORIGIN_VERSION = GPM.getPackageUtil().getProtocolVersion();
    }

    public double getVersionOffset(Entity Entity) {
        if(!(Entity instanceof Player)) return 0;
        int playerVersion = Via.getAPI().getPlayerVersion(Entity.getUniqueId());
        if(playerVersion == -1) return 0;
        if(ORIGIN_VERSION <= 763) {
            if(playerVersion >= 764) return 0.25d;
        } else {
            if(playerVersion <= 763) return -0.25d;
        }
        return 0;
    }

}