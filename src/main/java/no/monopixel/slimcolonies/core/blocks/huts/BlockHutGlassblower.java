package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the glassblower. No different from {@link AbstractBlockHut}
 */
public class BlockHutGlassblower extends AbstractBlockHut<BlockHutGlassblower>
{
    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutglassblower";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.glassblower.get();
    }
}
