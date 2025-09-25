package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the fisherman. No different from {@link AbstractBlockHut}
 */
public class BlockHutFisherman extends AbstractBlockHut<BlockHutFisherman>
{
    public BlockHutFisherman()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutfisherman";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.fisherman.get();
    }
}
