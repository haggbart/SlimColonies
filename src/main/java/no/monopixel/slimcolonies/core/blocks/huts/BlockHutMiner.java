package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the miner. No different from {@link AbstractBlockHut}
 */

public class BlockHutMiner extends AbstractBlockHut<BlockHutMiner>
{
    public BlockHutMiner()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutminer";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.miner.get();
    }
}
