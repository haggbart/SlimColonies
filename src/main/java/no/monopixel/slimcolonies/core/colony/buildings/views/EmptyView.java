package no.monopixel.slimcolonies.core.colony.buildings.views;

import no.monopixel.slimcolonies.api.colony.IColonyView;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import net.minecraft.core.BlockPos;

/**
 * An Empty implementation of {@link IBuildingView}. Currently used for Stash.
 */
public class EmptyView extends AbstractBuildingView
{
    /**
     * Instantiates the view of the building.
     *
     * @param c the colonyView.
     * @param l the location of the block.
     */
    public EmptyView(final IColonyView c, final BlockPos l)
    {
        super(c, l);
    }
}
