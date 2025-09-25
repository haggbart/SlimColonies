package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the fletcher. No different from {@link AbstractBlockHut}
 */
public class BlockHutFletcher extends AbstractBlockHut<BlockHutFletcher>
{
    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutfletcher";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.fletcher.get();
    }
}
