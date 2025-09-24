package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the hospital. No different from {@link AbstractBlockHut}
 */
public class BlockHutHospital extends AbstractBlockHut<BlockHutHospital>
{
    public BlockHutHospital()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhuthospital";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.hospital.get();
    }
}
