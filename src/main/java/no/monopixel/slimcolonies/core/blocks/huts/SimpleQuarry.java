package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the simple quarry. No different from {@link AbstractBlockHut}
 */
public class SimpleQuarry extends AbstractBlockHut<SimpleQuarry>
{
    public SimpleQuarry()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return ModBuildings.SIMPLE_QUARRY_ID;
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.simpleQuarry.get();
    }
}
