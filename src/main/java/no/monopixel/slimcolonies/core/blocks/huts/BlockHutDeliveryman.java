package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the warehouse. No different from {@link AbstractBlockHut}
 */

public class BlockHutDeliveryman extends AbstractBlockHut<BlockHutDeliveryman>
{
    public BlockHutDeliveryman()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutdeliveryman";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.deliveryman.get();
    }
}
