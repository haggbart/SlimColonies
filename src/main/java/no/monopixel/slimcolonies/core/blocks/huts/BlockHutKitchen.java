package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the kitchen. No different from {@link AbstractBlockHut}
 */
public class BlockHutKitchen extends AbstractBlockHut<BlockHutKitchen>
{
    public BlockHutKitchen()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutkitchen";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.kitchen.get();
    }
}
