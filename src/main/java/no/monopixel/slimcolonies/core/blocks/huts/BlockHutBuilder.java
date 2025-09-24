package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the builder. No different from {@link AbstractBlockHut}
 */
public class BlockHutBuilder extends AbstractBlockHut<BlockHutBuilder>
{
    public BlockHutBuilder()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutbuilder";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.builder.get();
    }
}
