package no.monopixel.slimcolonies.api.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import no.monopixel.slimcolonies.api.colony.IColony;

/**
 * The abstract implementation for farmer field tile entities.
 */
public abstract class AbstractTileEntityScarecrow extends BlockEntity
{
    /**
     * Default constructor.
     */
    protected AbstractTileEntityScarecrow(final BlockPos pos, final BlockState state)
    {
        super(SlimColoniesTileEntities.SCARECROW.get(), pos, state);
    }

    /**
     * Returns the type of the scarecrow (Important for the rendering).
     *
     * @return the enum type.
     */
    public abstract ScareCrowType getScarecrowType();

    /**
     * The colony this field is located in.
     *
     * @return the colony instance.
     */
    public abstract IColony getCurrentColony();
}
