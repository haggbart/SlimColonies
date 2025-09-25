package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the shepherd. No different from {@link AbstractBlockHut}
 */
public class BlockHutShepherd extends AbstractBlockHut<BlockHutShepherd>
{
    public BlockHutShepherd()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutshepherd";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.shepherd.get();
    }
}
