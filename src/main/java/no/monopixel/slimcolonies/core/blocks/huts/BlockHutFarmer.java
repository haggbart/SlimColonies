package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the Farmer. No different from {@link AbstractBlockHut}
 */

public class BlockHutFarmer extends AbstractBlockHut<BlockHutFarmer>
{
    public BlockHutFarmer()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutfarmer";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.farmer.get();
    }
}
