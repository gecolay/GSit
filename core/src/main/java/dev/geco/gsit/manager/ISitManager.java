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

    boolean isSitting(LivingEntity Entity);

    GSeat getSeat(LivingEntity Entity);

    void clearSeats();

    boolean kickSeat(Block Block, LivingEntity Entity);

    GSeat createSeat(Block Block, LivingEntity Entity);

    GSeat createSeat(Block Block, LivingEntity Entity, boolean Rotate, double XOffset, double YOffset, double ZOffset, float SeatRotation, boolean SitAtBlock, boolean GetUpSneak);

    void moveSeat(LivingEntity Entity, BlockFace Face);

    boolean removeSeat(LivingEntity Entity, GetUpReason Reason);

    boolean removeSeat(LivingEntity Entity, GetUpReason Reason, boolean Safe);

}