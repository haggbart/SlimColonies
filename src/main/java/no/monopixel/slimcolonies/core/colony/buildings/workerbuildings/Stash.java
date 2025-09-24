package no.monopixel.slimcolonies.core.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.IRSComponent;
import no.monopixel.slimcolonies.api.colony.requestsystem.resolver.IRequestResolver;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;

/**
 * Class used to manage the stash building block.
 */
public class Stash extends AbstractBuilding implements IRSComponent
{
    /**
     * Description of the block used to set this block.
     */
    private static final String STASH = "stash";

    /**
     * Instantiates the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public Stash(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @Override
    public ImmutableCollection<IRequestResolver<?>> createResolvers()
    {
        return ImmutableList.of();
    }

    @Override
    public Tuple<BlockPos, BlockPos> getCorners()
    {
        return new Tuple<>(getPosition(),getPosition());
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return STASH;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 0;
    }

    @Override
    public int getRotation()
    {
        return 0;
    }
}
