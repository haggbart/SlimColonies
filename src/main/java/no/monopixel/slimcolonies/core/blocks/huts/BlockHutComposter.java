package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;

import org.jetbrains.annotations.NotNull;

public class BlockHutComposter extends AbstractBlockHut<BlockHutComposter>
{

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutcomposter";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.composter.get();
    }
}
