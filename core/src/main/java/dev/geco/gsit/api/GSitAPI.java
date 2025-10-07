package dev.geco.gsit.api;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.model.PoseType;
import dev.geco.gsit.model.Seat;
import dev.geco.gsit.model.StopReason;
import dev.geco.gsit.model.Crawl;
import dev.geco.gsit.model.Pose;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class GSitAPI {

    /**
     * Returns the plugin instance for GSit
     * @author Gecolay
     * @since 2.0.0
     * @return Plugin instance
     */
    public static @NotNull GSitMain getInstance() {
        return GSitMain.getInstance();
    }

    /**
     * Checks if an entity can sit by click
     * @author Gecolay
     * @since 2.0.0
     * @param entity Entity
     * @return <code>true</code> if the entity can sit by click
     */
    public static boolean canEntityUseSit(@NotNull Entity entity) {
        return getInstance().getToggleService().canEntityUseSit(entity.getUniqueId());
    }

    /**
     * Sets if an entity can sit by click
     * @author Gecolay
     * @since 2.0.0
     * @param entity Entity
     * @param canUseSit Can entity sit
     */
    public static void setEntityCanUseSit(@NotNull Entity entity, boolean canUseSit) {
        getInstance().getToggleService().setEntityCanUseSit(entity.getUniqueId(), canUseSit);
    }

    /**
     * Checks if an entity is currently sitting
     * @author Gecolay
     * @since 2.0.0
     * @param entity Entity
     * @return <code>true</code> if the entity is sitting
     */
    public static boolean isEntitySitting(@NotNull LivingEntity entity) {
        return getInstance().getSitService().isEntitySitting(entity);
    }

    /**
     * Gets all seats
     * @author Gecolay
     * @since 2.0.0
     * @return Map of all seats
     */
    public static @NotNull HashMap<UUID, Seat> getAllSeats() {
        return getInstance().getSitService().getAllSeats();
    }

    /**
     * Gets the seat of an entity
     * @author Gecolay
     * @since 2.0.0
     * @param entity Entity
     * @return Seat or <code>null</code> if there was no seat
     */
    public static @Nullable Seat getSeatByEntity(@NotNull LivingEntity entity) {
        return getInstance().getSitService().getSeatByEntity(entity);
    }

    /**
     * Gets all seats by a block
     * @author Gecolay
     * @since 2.0.0
     * @param block Block
     * @return Set of seats
     */
    public static @NotNull Set<Seat> getSeatsByBlock(@NotNull Block block) {
        return getInstance().getSitService().getSeatsByBlock(block);
    }

    /**
     * Creates a new seat on a block for an entity
     * @author Gecolay
     * @since 2.0.0
     * @param block Block which should be connected to the returned seat
     * @param entity Entity
     * @return Seat or <code>null</code> if the creation failed or was cancelled by event
     */
    public static @Nullable Seat createSeat(@NotNull Block block, @NotNull LivingEntity entity) {
        return getInstance().getSitService().createSeat(block, entity);
    }

    /**
     * Creates a new seat on a block for an entity
     * The seat can be static or rotating
     * @author Gecolay
     * @since 2.0.0
     * @param block Block which should be connected to the returned seat
     * @param entity Entity
     * @param canRotate Can the seat rotate
     * @param seatRotation The default rotation of the seat
     * @param sitInBlockCenter Should the seat location be centered on the block
     * @return Seat or <code>null</code> if the creation failed or was cancelled by event
     */
    public static @Nullable Seat createSeat(@NotNull Block block, @NotNull LivingEntity entity, boolean canRotate, float seatRotation, boolean sitInBlockCenter) {
        return getInstance().getSitService().createSeat(block, entity, canRotate, 0d, 0d, 0d, seatRotation, sitInBlockCenter);
    }

    /**
     * Creates a new seat on a block for an entity
     * The seat can be static or rotating
     * The seat can be moved to with an offset
     * @author Gecolay
     * @since 2.0.0
     * @param block Block which should be connected to the returned seat
     * @param entity Entity
     * @param canRotate Can the seat rotate
     * @param xOffset The x coordinate offset for the seat
     * @param yOffset The y coordinate offset for the seat
     * @param zOffset The z coordinate offset for the seat
     * @param seatRotation The default rotation of the seat
     * @param sitInBlockCenter Should the seat location be centered on the block
     * @return Seat or <code>null</code> if the creation failed or was cancelled by event
     */
    public static @Nullable Seat createSeat(@NotNull Block block, @NotNull LivingEntity entity, boolean canRotate, double xOffset, double yOffset, double zOffset, float seatRotation, boolean sitInBlockCenter) {
        return getInstance().getSitService().createSeat(block, entity, canRotate, xOffset, yOffset, zOffset, seatRotation, sitInBlockCenter);
    }

    /**
     * Moves a seat in a block direction
     * @author Gecolay
     * @since 2.0.0
     * @param seat Seat
     * @param blockDirection The direction in which the seat should get moved
     */
    public static void moveSeat(@NotNull Seat seat, @NotNull BlockFace blockDirection) {
        getInstance().getSitService().moveSeat(seat, blockDirection);
    }

    /**
     * Removes a seat
     * @author Gecolay
     * @since 2.0.0
     * @param seat Seat
     * @param stopReason The reason why the seat gets removed
     * @return <code>true</code> or <code>false</code> if the deletion failed or was cancelled by event
     */
    public static boolean removeSeat(@NotNull Seat seat, @NotNull StopReason stopReason) {
        return getInstance().getSitService().removeSeat(seat, stopReason);
    }

    /**
     * Removes a seat
     * The return teleport can be disabled, if for example the entity already teleports somewhere else
     * @author Gecolay
     * @since 2.0.0
     * @param seat Seat
     * @param stopReason The reason why the seat gets removed
     * @param useSafeDismount Should the entity get teleported to the safe location (includes the return location)
     * @return <code>true</code> or <code>false</code> if the deletion failed or was cancelled by event
     */
    public static boolean removeSeat(@NotNull Seat seat, @NotNull StopReason stopReason, boolean useSafeDismount) {
        return getInstance().getSitService().removeSeat(seat, stopReason, useSafeDismount);
    }

    /**
     * Checks if a player can sit on another player by click
     * @author Gecolay
     * @since 2.0.0
     * @param playerUuid UUID of the player
     * @return <code>true</code> if the player can sit on another player
     */
    public static boolean canPlayerUsePlayerSit(@NotNull UUID playerUuid) {
        return getInstance().getToggleService().canPlayerUsePlayerSit(playerUuid);
    }

    /**
     * Sets if a player can sit on another player by click
     * @author Gecolay
     * @since 2.0.0
     * @param playerUuid UUID of the player
     * @param canUsePlayerSit Should the player be able to sit on another player by click
     */
    public static void setPlayerCanUsePlayerSit(@NotNull UUID playerUuid, boolean canUsePlayerSit) {
        getInstance().getToggleService().setPlayerCanUsePlayerSit(playerUuid, canUsePlayerSit);
    }

    /**
     * Places a player on top of another target player
     * @author Gecolay
     * @since 2.0.0
     * @param player Player
     * @param target Target player
     * @return <code>true</code> or <code>false</code> if the action was cancelled by event
     */
    public static boolean sitOnPlayer(@NotNull Player player, @NotNull Player target) {
        return getInstance().getPlayerSitService().sitOnPlayer(player, target);
    }

    /**
     * Stops a player from sitting on another player or from holding players
     * This can be used on any of the players in a stack
     * @author Gecolay
     * @since 2.0.0
     * @param player Player
     * @param stopReason The reason why the player sit action is stopped
     * @return <code>true</code> or <code>false</code> if the action was cancelled by event
     */
    public static boolean stopPlayerSit(@NotNull Player player, @NotNull StopReason stopReason) {
        return getInstance().getPlayerSitService().stopPlayerSit(player, stopReason);
    }

    /**
     * Checks if a player is currently posing
     * @author Gecolay
     * @since 2.0.0
     * @param player Player
     * @return <code>true</code> if the player is posing
     */
    public static boolean isPlayerPosing(@NotNull Player player) {
        return getInstance().getPoseService().isPlayerPosing(player);
    }

    /**
     * Gets all poses
     * @author Gecolay
     * @since 2.0.0
     * @return Map of all poses
     */
    public static @NotNull HashMap<UUID, Pose> getAllPoses() {
        return getInstance().getPoseService().getAllPoses();
    }

    /**
     * Gets the pose of a player
     * @author Gecolay
     * @since 2.0.0
     * @param player Player
     * @return Pose or <code>null</code> if there was no pose
     */
    public static @Nullable Pose getPoseByPlayer(@NotNull Player player) {
        return getInstance().getPoseService().getPoseByPlayer(player);
    }

    /**
     * Gets all poses by a block
     * @author Gecolay
     * @since 2.0.0
     * @param block Block
     * @return Set of poses
     */
    public static @NotNull Set<Pose> getPosesByBlock(@NotNull Block block) {
        return getInstance().getPoseService().getPosesByBlock(block);
    }

    /**
     * Creates a new pose on a block for a player
     * @author Gecolay
     * @since 2.0.0
     * @param block Block which should be connected to the returned pose
     * @param player Player
     * @param poseType PoseType {@link PoseType}
     * @return Pose or <code>null</code> if the creation failed or was cancelled by event
     */
    public static @Nullable Pose createPose(@NotNull Block block, @NotNull Player player, @NotNull PoseType poseType) {
        return getInstance().getPoseService().createPose(block, player, poseType);
    }

    /**
     * Creates a new pose on a block for a player
     * The pose can be static or rotating
     * @author Gecolay
     * @since 2.0.0
     * @param block Block which should be connected to the returned pose
     * @param player Player
     * @param poseType PoseType {@link PoseType}
     * @param seatRotation The default rotation of the seat from the pose
     * @param sitInBlockCenter Should the seat location from the pose be centered on the block
     * @return Pose or <code>null</code> if the creation failed or was cancelled by event
     */
    public static @Nullable Pose createPose(@NotNull Block block, @NotNull Player player, @NotNull PoseType poseType, float seatRotation, boolean sitInBlockCenter) {
        return getInstance().getPoseService().createPose(block, player, poseType, 0d, 0d, 0d, seatRotation, sitInBlockCenter);
    }

    /**
     * Creates a new pose on a block for a player
     * The pose can be static or rotating
     * The pose can be moved to with an offset
     * @author Gecolay
     * @since 2.0.0
     * @param block Block which should be connected to the returned pose
     * @param player Player
     * @param poseType PoseType {@link PoseType}
     * @param xOffset The x coordinate offset for the pose
     * @param yOffset The y coordinate offset for the pose
     * @param zOffset The z coordinate offset for the pose
     * @param seatRotation The default rotation of the seat from the pose
     * @param sitInBlockCenter Should the seat location from the pose be centered on the block
     * @return Pose or <code>null</code> if the creation failed or was cancelled by event
     */
    public static @Nullable Pose createPose(@NotNull Block block, @NotNull Player player, @NotNull PoseType poseType, double xOffset, double yOffset, double zOffset, float seatRotation, boolean sitInBlockCenter) {
        return getInstance().getPoseService().createPose(block, player, poseType, xOffset, yOffset, zOffset, seatRotation, sitInBlockCenter);
    }

    /**
     * Removes a pose
     * @author Gecolay
     * @since 2.0.0
     * @param pose Pose
     * @param stopReason The reason why the pose gets removed
     * @return <code>true</code> or <code>false</code> if the deletion failed or was cancelled by event
     */
    public static boolean removePose(@NotNull Pose pose, @NotNull StopReason stopReason) {
        return getInstance().getPoseService().removePose(pose, stopReason);
    }

    /**
     * Removes a pose
     * The return teleport can be disabled, if for example the entity already teleports somewhere else
     * @author Gecolay
     * @since 2.0.0
     * @param pose Pose
     * @param stopReason The reason why the pose gets removed
     * @param useSafeDismount Should the player get teleported to the safe location (includes the return location)
     * @return <code>true</code> or <code>false</code> if the deletion failed or was cancelled by event
     */
    public static boolean removePose(@NotNull Pose pose, @NotNull StopReason stopReason, boolean useSafeDismount) {
        return getInstance().getPoseService().removePose(pose, stopReason, useSafeDismount);
    }

    /**
     * Checks if a player is currently crawling
     * @author Gecolay
     * @since 2.0.0
     * @param player Player
     * @return <code>true</code> if the player is crawling
     */
    public static boolean isPlayerCrawling(@NotNull Player player) {
        return getInstance().getCrawlService().isPlayerCrawling(player);
    }

    /**
     * Gets all crawls
     * @author Gecolay
     * @since 2.0.0
     * @return Map of all crawls
     */
    public static @NotNull HashMap<UUID, Crawl> getAllCrawls() {
        return getInstance().getCrawlService().getAllCrawls();
    }

    /**
     * Gets the crawl by a player
     * @author Gecolay
     * @since 2.0.0
     * @param player Player
     * @return Crawl or <code>null</code> if there was no crawl
     */
    public static @Nullable Crawl getCrawlByPlayer(@NotNull Player player) {
        return getInstance().getCrawlService().getCrawlByPlayer(player);
    }

    /**
     * Starts a new crawl session for a player
     * @author Gecolay
     * @since 2.0.0
     * @param player Player
     * @return Crawl or <code>null</code> if the start failed or was cancelled by event
     */
    public static @Nullable Crawl startCrawl(@NotNull Player player) {
        return getInstance().getCrawlService().startCrawl(player);
    }

    /**
     * Stops a crawl session for a player
     * @author Gecolay
     * @since 2.0.0
     * @param crawl Crawl
     * @param stopReason  The reason why the crawl session stops
     * @return <code>true</code> or <code>false</code> if the stop failed or was cancelled by event
     */
    public static boolean stopCrawl(@NotNull Crawl crawl, @NotNull StopReason stopReason) {
        return getInstance().getCrawlService().stopCrawl(crawl, stopReason);
    }

}