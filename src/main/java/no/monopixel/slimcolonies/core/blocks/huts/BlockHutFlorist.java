package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

public class BlockHutFlorist extends AbstractBlockHut<BlockHutFlorist>
{
    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutflorist";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.florist.get();
    }
}
