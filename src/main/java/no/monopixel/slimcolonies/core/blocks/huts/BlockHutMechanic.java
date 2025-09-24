package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the mechanic. No different from {@link AbstractBlockHut}
 */
public class BlockHutMechanic extends AbstractBlockHut<BlockHutMechanic>
{
    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutmechanic";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.mechanic.get();
    }
}
