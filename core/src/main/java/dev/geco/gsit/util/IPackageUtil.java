package dev.geco.gsit.util;

import org.bukkit.entity.*;

public interface IPackageUtil {

    int getProtocolVersion();

    void registerPlayer(Player Player);

    void unregisterPlayer(Player Player);

    void unregisterPlayers();

}
