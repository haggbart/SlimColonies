package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the shepherd. No different from {@link AbstractBlockHut}
 */
public class BlockHutSwineHerder extends AbstractBlockHut<BlockHutSwineHerder>
{
    public BlockHutSwineHerder()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutswineherder";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.swineHerder.get();
    }
}
