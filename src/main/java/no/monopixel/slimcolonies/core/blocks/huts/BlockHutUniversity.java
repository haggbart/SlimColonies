package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the university. No different from {@link AbstractBlockHut}
 */
public class BlockHutUniversity extends AbstractBlockHut<BlockHutUniversity>
{
    public BlockHutUniversity()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutuniversity";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.university.get();
    }
}
