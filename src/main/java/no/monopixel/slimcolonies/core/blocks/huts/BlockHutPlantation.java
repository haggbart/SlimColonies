package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the plantation. No different from {@link AbstractBlockHut}
 */

public class BlockHutPlantation extends AbstractBlockHut<BlockHutPlantation>
{
    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutplantation";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.plantation.get();
    }
}
