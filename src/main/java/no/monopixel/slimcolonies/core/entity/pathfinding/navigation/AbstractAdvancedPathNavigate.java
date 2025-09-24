package no.monopixel.slimcolonies.core.entity.pathfinding.navigation;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.api.entity.pathfinding.IPathJob;
import no.monopixel.slimcolonies.api.entity.pathfinding.IStuckHandler;
import no.monopixel.slimcolonies.core.entity.pathfinding.PathingOptions;
import no.monopixel.slimcolonies.core.entity.pathfinding.pathjobs.AbstractPathJob;
import no.monopixel.slimcolonies.core.entity.pathfinding.pathjobs.PathJobMoveCloseToXNearY;
import no.monopixel.slimcolonies.core.entity.pathfinding.pathjobs.PathJobRandomPos;
import no.monopixel.slimcolonies.core.entity.pathfinding.pathresults.PathResult;
import no.monopixel.slimcolonies.core.entity.pathfinding.pathresults.TreePathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractAdvancedPathNavigate extends GroundPathNavigation
{
    //  Parent class private members
    protected final Mob      ourEntity;
    protected       double   walkSpeedFactor = 1.0D;
    @Nullable
    protected       BlockPos originalDestination;

    /**
     * The navigators node costs
     */
    private PathingOptions pathingOptions = new PathingOptions();

    public AbstractAdvancedPathNavigate(
        final Mob entityLiving,
        final Level worldIn)
    {
        super(entityLiving, worldIn);
        this.ourEntity = mob;
    }

    /**
     * Used to path away from a position.
     *
     * @param currentPosition the position to avoid.
     * @param range           the range he should move out of.
     * @param speed           the speed to run at.
     * @param safeDestination if the destination is save and should be set.
     * @return the result of the pathing.
     */
    protected abstract PathResult<? extends IPathJob> walkAwayFrom(final BlockPos currentPosition, final double range, final double speed, final boolean safeDestination);

    /**
     * Try to move to a certain position.
     *
     * @param pos             the target position.
     * @param speed           the speed to walk.
     * @param safeDestination if the destination is safe and should be set.
     * @return the PathResult.
     */
    protected abstract PathResult<? extends IPathJob> walkTo(final BlockPos pos, final double speed, final boolean safeDestination);

    /**
     * Attempt to move to a specific pos.
     *
     * @param position the position to move to.
     * @param speed    the speed.
     * @return true if successful.
     */
    protected abstract boolean walkTo(final BlockPos position, final double speed);

    /**
     * Attemps to move in the given direction, walking at least range blocks
     *
     */
    @Nullable
    protected abstract PathResult<AbstractPathJob> walkTowards(BlockPos towards, double range, double speedFactor);

    /**
     * Used to path towards a random pos.
     *
     * @param range the range he should move out of.
     * @param speed the speed to run at.
     * @return the result of the pathing.
     */
    protected abstract PathResult<? extends IPathJob> walkToRandomPos(final int range, final double speed);

    /**
     * Used to path towards a random pos.
     *
     * @param range the range he should move out of.
     * @param speed the speed to run at.
     * @param pos   the pos to circle around.
     * @return the result of the pathing.
     */
    protected abstract PathResult<? extends IPathJob> walkToRandomPosAround(final int range, final double speed, final BlockPos pos);

    /**
     * Walks towards the desired position, while trying to not steer too far from the nearby position
     *
     * @param desiredPosition
     * @param nearbyPosition
     * @param distToDesired
     * @param speedFactor
     * @param safeDestination
     * @return
     */
    protected abstract PathResult<PathJobMoveCloseToXNearY> walkCloseToXNearY(
        BlockPos desiredPosition,
        BlockPos nearbyPosition,
        int distToDesired,
        double speedFactor,
        boolean safeDestination);

    /**
     * Used to path towards a random pos within some restrictions
     *
     * @param range   the range he should move out of.
     * @param speed   the speed to run at.
     * @param corners the corners they can't leave.
     * @return the result of the pathing.
     */
    protected abstract PathResult<? extends IPathJob> walkToRandomPos(
        final int range,
        final double speed,
        final net.minecraft.util.Tuple<BlockPos, BlockPos> corners);

    /**
     * Used to path towards a random pos within some restrictions
     *
     * @param range   the range he should move out of.
     * @param speed   the speed to run at.
     * @param corners the corners they can't leave.
     * @return the result of the pathing.
     */
    protected abstract PathResult<PathJobRandomPos> walkToRandomPos(
        final int range,
        final double speed,
        final net.minecraft.util.Tuple<BlockPos, BlockPos> corners, final boolean preferInside);

    /**
     * Used to find a tree.
     *
     * @param startRestriction the start of the restricted area.
     * @param endRestriction   the end of the restricted area.
     * @param speed            walking speed.
     * @param excludedTrees    the trees which should be cut.
     * @return the result of the search.
     */
    public abstract TreePathResult walkToTree(
        final BlockPos startRestriction,
        final BlockPos endRestriction,
        final double speed,
        final List<ItemStorage> excludedTrees,
        final int dyntreesize,
        final IColony colony);

    /**
     * Used to find a tree.
     *
     * @param range         in the range.
     * @param speed         walking speed.
     * @param excludedTrees the trees which should be cut.
     * @return the result of the search.
     */
    public abstract TreePathResult walkToTree(final int range, final double speed, final List<ItemStorage> excludedTrees, final int dyntreesize, final IColony colony);

    /**
     * Get the pathing options
     *
     * @return the pathing options.
     */
    public PathingOptions getPathingOptions()
    {
        return pathingOptions;
    }

    /**
     * Get the entity of this navigator
     *
     * @return mobentity
     */
    public Mob getOurEntity()
    {
        return ourEntity;
    }

    /**
     * Sets the stuck handler for this navigator
     *
     * @param stuckHandler handler to use
     */
    public abstract void setStuckHandler(final IStuckHandler stuckHandler);

    public abstract void setSwimSpeedFactor(double factor);

    /**
     * Sets the navigation to not accept new jobs for a time
     *
     * @param pauseTicks
     */
    protected abstract void setPauseTicks(int pauseTicks);

    /**
     * Gets the current path result
     *
     * @return
     */
    public abstract PathResult getPathResult();
}
