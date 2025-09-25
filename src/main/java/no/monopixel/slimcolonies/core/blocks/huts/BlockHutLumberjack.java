package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the lumberjack. No different from {@link AbstractBlockHut}
 */
public class BlockHutLumberjack extends AbstractBlockHut<BlockHutLumberjack>
{
    public BlockHutLumberjack()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutlumberjack";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.lumberjack.get();
    }
}
