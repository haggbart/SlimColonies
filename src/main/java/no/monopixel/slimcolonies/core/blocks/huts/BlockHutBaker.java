package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the bakery. No different from {@link AbstractBlockHut}
 */
public class BlockHutBaker extends AbstractBlockHut<BlockHutBaker>
{
    public BlockHutBaker()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutbaker";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.bakery.get();
    }
}
