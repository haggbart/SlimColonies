package no.monopixel.slimcolonies.core.entity.pathfinding.pathresults;

import no.monopixel.slimcolonies.api.util.Pond.PondState;
import no.monopixel.slimcolonies.core.entity.pathfinding.pathjobs.PathJobFindWater;
import net.minecraft.core.BlockPos;

/**
 * Contains the result of the path job to find water.
 */
public class WaterPathResult extends PathResult<PathJobFindWater>
{
    /**
     * The position of the parent (stand block).
     */
    public BlockPos parent;

    /**
     * The position of the pond.
     */
    public BlockPos pond;

    /*
     * Whether this pond is suboptimal or not
     */
    public PondState pondState;

    /**
     * If the pond is empty.
     */
    public boolean isEmpty;
}
