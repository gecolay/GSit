package dev.geco.gsit.api;

import java.util.*;

import org.jetbrains.annotations.*;

import org.bukkit.block.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class GSitAPI {

    /**
     * Returns the Plugin-Instance for GSit
     * @author Gecolay
     * @since 1.0.0
     * @return Plugin-Instance
     */
    public static GSitMain getInstance() { return GSitMain.getInstance(); }

    /**
     * Checks if a Player can sit by click
     * @author Gecolay
     * @since 1.0.0
     * @param Player Player
     * @return <code>true</code> if the Player can sit
     */
    public static boolean canSit(@NotNull Player Player) {
        return getInstance().getToggleManager().canSit(Player.getUniqueId());
    }

    /**
     * Sets if a Player can sit by click
     * @author Gecolay
     * @since 1.0.0
     * @param Player Player
     */
    public static void setCanSit(@NotNull Player Player, boolean CanSit) {
        getInstance().getToggleManager().setCanSit(Player.getUniqueId(), CanSit);
    }

    /**
     * Checks if an Entity is currently sitting
     * @author Gecolay
     * @since 1.0.0
     * @param Entity Entity for this Seat
     * @return <code>true</code> if the Entity is sitting
     */
    public static boolean isSitting(@NotNull LivingEntity Entity) {
        return getInstance().getSitManager().isSitting(Entity);
    }

    /**
     * Gets all Seats
     * @author Gecolay
     * @since 1.0.0
     * @return List of all Seat-Objects
     */
    public static List<GSeat> getSeats() {
        return getInstance().getSitManager().getSeats();
    }

    /**
     * Gets the Seat of an Entity
     * @author Gecolay
     * @since 1.0.0
     * @param Entity Entity for this Seat
     * @return Seat-Object or <code>null</code> if there was no Seat
     */
    public static GSeat getSeat(@NotNull LivingEntity Entity) {
        return getInstance().getSitManager().getSeat(Entity);
    }

    /**
     * Gets all Seats of a Block
     * @author Gecolay
     * @since 1.0.0
     * @param Block Block for this Seats
     * @return List of Seat-Objects
     */
    public static List<GSeat> getSeats(@NotNull Block Block) {
        return getInstance().getSitManager().getSeats(Block);
    }

    /**
     * Gets all Seats of a List of Blocks
     * @author Gecolay
     * @since 1.0.0
     * @param Blocks Blocks for this Seats
     * @return List of Seat-Objects
     */
    public static List<GSeat> getSeats(@NotNull List<Block> Blocks) {
        return getInstance().getSitManager().getSeats(Blocks);
    }

    /**
     * Creates a new Seat on a Block for an Entity
     * @author Gecolay
     * @since 1.0.0
     * @param Block Block which should be connected to the Seat-Object
     * @param Entity Entity for this Seat
     * @return Seat-Object or <code>null</code> if the creation was canceled
     */
    public static GSeat createSeat(@NotNull Block Block, @NotNull LivingEntity Entity) {
        return getInstance().getSitManager().createSeat(Block, Entity);
    }

    /**
     * Creates a new Seat on a Block for an Entity.
     * The Seat can be static or rotating.
     * @author Gecolay
     * @since 1.0.0
     * @param Block Block which should be connected to the Seat-Object
     * @param Entity Entity for this Seat
     * @param Rotate Should this Seat rotate with direction the entity is facing
     * @param SeatRotationYaw The default Rotation of the Seat
     * @param SitAtBlock Should the Seat be aligned with the Block (<code>true</code>) or the Entity (<code>false</code>)
     * @return Seat-Object or <code>null</code> if the creation was canceled
     */
    public static GSeat createSeat(@NotNull Block Block, @NotNull LivingEntity Entity, boolean Rotate, float SeatRotationYaw, boolean SitAtBlock) {
        return createSeat(Block, Entity, Rotate, 0d, 0d, 0d, SeatRotationYaw, SitAtBlock);
    }

    /**
     * Creates a new Seat on a Block for an Entity.
     * The Seat can be static or rotating.
     * The seat can be moved to with an Offset
     * @author Gecolay
     * @since 1.0.0
     * @param Block Block which should be connected to the Seat-Object
     * @param Entity Entity for this Seat
     * @param Rotate Should this Seat rotate with direction the entity is facing
     * @param XOffset The X-Coordinate-Offset for the Seat
     * @param YOffset The Y-Coordinate-Offset for the Seat
     * @param ZOffset The Z-Coordinate-Offset for the Seat
     * @param SeatRotationYaw The default Rotation of the Seat
     * @param SitAtBlock Should the Seat be aligned with the Block (<code>true</code>) or the Entity (<code>false</code>)
     * @return Seat-Object or <code>null</code> if the creation was canceled
     */
    public static GSeat createSeat(@NotNull Block Block, @NotNull LivingEntity Entity, boolean Rotate, double XOffset, double YOffset, double ZOffset, float SeatRotationYaw, boolean SitAtBlock) {
        return getInstance().getSitManager().createSeat(Block, Entity, Rotate, XOffset, YOffset, ZOffset, SeatRotationYaw, SitAtBlock);
    }

    /**
     * Moves an existing Seat
     * @author Gecolay
     * @since 1.0.4
     * @param Entity Entity for this Seat
     * @param Direction The Direction in which the Seat should get moved
     */
    public static void moveSeat(@NotNull LivingEntity Entity, @NotNull BlockFace Direction) {
        getInstance().getSitManager().moveSeat(Entity, Direction);
    }

    /**
     * Removes an existing Seat
     * @author Gecolay
     * @since 1.0.0
     * @param Entity Entity for this Seat
     * @param Reason The Reason why the Seat gets removed
     * @return <code>true</code> or <code>false</code> if the deletion was canceled
     */
    public static boolean removeSeat(@NotNull LivingEntity Entity, @NotNull GetUpReason Reason) {
        return getInstance().getSitManager().removeSeat(Entity, Reason);
    }

    /**
     * Removes an existing Seat.
     * The Get-Up-Safe-Teleport can be disabled
     * @author Gecolay
     * @since 1.0.0
     * @param Entity Entity for this Seat
     * @param Reason The Reason why the Seat gets removed
     * @param Safe Should the Entity get teleported to a safe Position
     * @return <code>true</code> or <code>false</code> if the deletion was canceled
     */
    public static boolean removeSeat(@NotNull LivingEntity Entity, @NotNull GetUpReason Reason, boolean Safe) {
        return getInstance().getSitManager().removeSeat(Entity, Reason, Safe);
    }

    /**
     * Checks if a Player can sit on a Player by click
     * @author Gecolay
     * @since 1.0.0
     * @param Player Player
     * @return <code>true</code> if the Player can sit on a Player
     */
    public static boolean canPlayerSit(@NotNull Player Player) {
        return getInstance().getToggleManager().canPlayerSit(Player.getUniqueId());
    }

    /**
     * Sets if a Player can sit on a Player by click
     * @author Gecolay
     * @since 1.0.0
     * @param Player Player
     */
    public static void setCanPlayerSit(@NotNull Player Player, boolean CanPlayerSit) {
        getInstance().getToggleManager().setCanPlayerSit(Player.getUniqueId(), CanPlayerSit);
    }

    /**
     * Let a Player sit on top of another Player (Target)
     * @author Gecolay
     * @since 1.0.0
     * @param Player Player
     * @param Target Target
     * @return <code>true</code> or <code>false</code> if the action was canceled
     */
    public static boolean sitOnPlayer(@NotNull Player Player, @NotNull Player Target) {
        return getInstance().getPlayerSitManager().sitOnPlayer(Player, Target);
    }

    /**
     * Stop a PlayerSit
     * @author Gecolay
     * @since 1.0.0
     * @param Entity Entity
     * @param Reason The Reason why the PlayerSit is stopped
     * @return <code>true</code> or <code>false</code> if the stop was canceled
     */
    public static boolean stopPlayerSit(@NotNull Entity Entity, @NotNull GetUpReason Reason) {
        return getInstance().getPlayerSitManager().stopPlayerSit(Entity, Reason);
    }

    /**
     * Checks if a Player is currently posing
     * @author Gecolay
     * @since 1.0.0
     * @param Player Player for this PoseSeat
     * @return <code>true</code> if the Player is posing
     */
    public static boolean isPosing(@NotNull Player Player) {
        return getInstance().getPoseManager().isPosing(Player);
    }

    /**
     * Gets all PoseSeats
     * @author Gecolay
     * @since 1.0.0
     * @return List of all PoseSeat-Objects
     */
    public static List<IGPoseSeat> getPoses() {
        return getInstance().getPoseManager().getPoses();
    }

    /**
     * Gets the PoseSeat of a Player
     * @author Gecolay
     * @since 1.0.0
     * @param Player Player for this PoseSeat
     * @return PoseSeat-Object or <code>null</code> if there was no PoseSeat
     */
    public static IGPoseSeat getPose(@NotNull Player Player) {
        return getInstance().getPoseManager().getPose(Player);
    }

    /**
     * Gets all PoseSeats of a Block
     * @author Gecolay
     * @since 1.0.0
     * @param Block Block for this PoseSeats
     * @return List of PoseSeat-Objects
     */
    public static List<IGPoseSeat> getPoses(@NotNull Block Block) {
        return getInstance().getPoseManager().getPoses(Block);
    }

    /**
     * Gets all PoseSeats of a List of Blocks
     * @author Gecolay
     * @since 1.0.0
     * @param Blocks Blocks for this PoseSeats
     * @return List of PoseSeat-Objects
     */
    public static List<IGPoseSeat> getPoses(@NotNull List<Block> Blocks) {
        return getInstance().getPoseManager().getPoses(Blocks);
    }

    /**
     * Creates a new PoseSeat on a Block for a Player
     * @author Gecolay
     * @since 1.0.0
     * @param Block Block which should be connected to the PoseSeat-Object
     * @param Player Player for this PoseSeat
     * @param Pose Player-Pose {@link Pose}
     * @return PoseSeat-Object or <code>null</code> if the creation was canceled
     */
    public static IGPoseSeat createPose(@NotNull Block Block, @NotNull Player Player, @NotNull Pose Pose) {
        return getInstance().getPoseManager().createPose(Block, Player, Pose);
    }

    /**
     * Creates a new PoseSeat on a Block for a Player.
     * The PoseSeat can be static or rotating.
     * @author Gecolay
     * @since 1.0.0
     * @param Block Block which should be connected to the PoseSeat-Object
     * @param Player Player for this PoseSeat
     * @param Pose Player-Pose {@link Pose}
     * @param SeatRotationYaw The default Rotation of the PoseSeat
     * @param SitAtBlock Should the PoseSeat be aligned with the Block (<code>true</code>) or the Player (<code>false</code>)
     * @return PoseSeat-Object or <code>null</code> if the creation was canceled
     */
    public static IGPoseSeat createPose(@NotNull Block Block, @NotNull Player Player, @NotNull Pose Pose, float SeatRotationYaw, boolean SitAtBlock) {
        return createPose(Block, Player, Pose, 0d, 0d, 0d, SeatRotationYaw, SitAtBlock);
    }

    /**
     * Creates a new PoseSeat on a Block for a Player.
     * The PoseSeat can be static or rotating.
     * The PoseSeat can be moved to with an Offset
     * @author Gecolay
     * @since 1.0.0
     * @param Block Block which should be connected to the PoseSeat-Object
     * @param Player Player for this PoseSeat
     * @param Pose Player-Pose {@link Pose}
     * @param XOffset The X-Coordinate-Offset for the PoseSeat
     * @param YOffset The Y-Coordinate-Offset for the PoseSeat
     * @param ZOffset The Z-Coordinate-Offset for the PoseSeat
     * @param SeatRotationYaw The default Rotation of the PoseSeat
     * @param SitAtBlock Should the PoseSeat be aligned with the Block (<code>true</code>) or the Player (<code>false</code>)
     * @return PoseSeat-Object or <code>null</code> if the creation was canceled
     */
    public static IGPoseSeat createPose(@NotNull Block Block, @NotNull Player Player, @NotNull Pose Pose, double XOffset, double YOffset, double ZOffset, float SeatRotationYaw, boolean SitAtBlock) {
        return getInstance().getPoseManager().createPose(Block, Player, Pose, XOffset, YOffset, ZOffset, SeatRotationYaw, SitAtBlock);
    }

    /**
     * Removes an existing PoseSeat
     * @author Gecolay
     * @since 1.0.0
     * @param Player Player for this PoseSeat
     * @param Reason The Reason why the PoseSeat gets removed
     * @return <code>true</code> or <code>false</code> if the deletion was canceled
     */
    public static boolean removePose(@NotNull Player Player, @NotNull GetUpReason Reason) {
        return getInstance().getPoseManager().removePose(Player, Reason);
    }

    /**
     * Removes an existing PoseSeat.
     * The Get-Up-Safe-Teleport can be disabled
     * @author Gecolay
     * @since 1.0.0
     * @param Player Player for this PoseSeat
     * @param Reason The Reason why the PoseSeat gets removed
     * @param Safe Should the Player get teleported to a safe Position
     * @return <code>true</code> or <code>false</code> if the deletion was canceled
     */
    public static boolean removePose(@NotNull Player Player, @NotNull GetUpReason Reason, boolean Safe) {
        return getInstance().getPoseManager().removePose(Player, Reason, Safe);
    }

    /**
     * Checks if a Player is currently crawling
     * @author Gecolay
     * @since 1.0.0
     * @param Player Player for this Crawl-Object
     * @return <code>true</code> if the Player is crawling
     */
    public static boolean isCrawling(@NotNull Player Player) {
        return getInstance().getCrawlManager().isCrawling(Player);
    }

    /**
     * Gets all Crawl-Objects
     * @author Gecolay
     * @since 1.0.0
     * @return List of all Crawl-Objects
     */
    public static List<IGCrawl> getCrawls() {
        return getInstance().getCrawlManager() != null ? getInstance().getCrawlManager().getCrawls() : new ArrayList<>();
    }

    /**
     * Gets the Crawl-Object of a Player
     * @author Gecolay
     * @since 1.0.0
     * @param Player Player for this Crawl-Object
     * @return Crawl-Object or <code>null</code> if there was no Crawl-Object
     */
    public static IGCrawl getCrawl(@NotNull Player Player) {
        return getInstance().getCrawlManager() != null ? getInstance().getCrawlManager().getCrawl(Player) : null;
    }

    /**
     * Creates a new Crawl-Object for a Player
     * @author Gecolay
     * @since 1.0.0
     * @param Player Player for this Crawl-Object
     * @return Crawl-Object or <code>null</code> if the creation was canceled
     */
    public static IGCrawl startCrawl(@NotNull Player Player) {
        return getInstance().getCrawlManager() != null ? getInstance().getCrawlManager().startCrawl(Player) : null;
    }

    /**
     * Removes an existing Crawl-Object
     * @author Gecolay
     * @since 1.0.0
     * @param Player Player for this Crawl-Object
     * @param Reason The Reason why the Crawl-Object gets removed
     * @return <code>true</code> or <code>false</code> if the deletion was canceled
     */
    public static boolean stopCrawl(@NotNull Player Player, @NotNull GetUpReason Reason) {
        return getInstance().getCrawlManager() != null && getInstance().getCrawlManager().stopCrawl(Player, Reason);
    }

}