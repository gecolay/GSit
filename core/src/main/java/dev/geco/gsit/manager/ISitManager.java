package dev.geco.gsit.manager;

import java.util.*;

import org.bukkit.block.*;
import org.bukkit.entity.*;

import dev.geco.gsit.objects.*;

public interface ISitManager {

    double STAIR_XZ_OFFSET = 0.123d;

    double STAIR_Y_OFFSET = 0.5d;

    int getFeatureUsedCount();

    void resetFeatureUsedCount();

    List<GSeat> getSeats();

    boolean isSitting(Player Player);

    GSeat getSeat(Player Player);

    void clearSeats();

    boolean kickSeat(Block Block, Player Player);

    GSeat createSeat(Block Block, Player Player);

    GSeat createSeat(Block Block, Player Player, boolean Rotate, double XOffset, double YOffset, double ZOffset, float SeatRotation, boolean SitAtBlock, boolean GetUpSneak);

    void moveSeat(Player Player, BlockFace Face);

    boolean removeSeat(Player Player, GetUpReason Reason);

    boolean removeSeat(Player Player, GetUpReason Reason, boolean Safe);

}