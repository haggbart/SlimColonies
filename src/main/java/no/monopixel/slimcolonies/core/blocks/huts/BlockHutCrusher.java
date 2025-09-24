package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the crusher. No different from {@link AbstractBlockHut}
 */
public class BlockHutCrusher extends AbstractBlockHut<BlockHutCrusher>
{
    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutcrusher";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.crusher.get();
    }
}
