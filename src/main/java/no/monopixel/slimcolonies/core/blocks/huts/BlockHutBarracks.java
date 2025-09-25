package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Block of the Barracks.
 */
public class BlockHutBarracks extends AbstractBlockHut<BlockHutBarracks>
{
    /**
     * Default constructor.
     */
    public BlockHutBarracks()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutbarracks";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.barracks.get();
    }
}
