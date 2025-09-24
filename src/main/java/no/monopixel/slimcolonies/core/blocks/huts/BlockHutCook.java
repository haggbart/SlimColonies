package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the lumberjack. No different from {@link AbstractBlockHut}
 */
public class BlockHutCook extends AbstractBlockHut<BlockHutCook>
{
    public BlockHutCook()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutcook";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.cook.get();
    }
}
