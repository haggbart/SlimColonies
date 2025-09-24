package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Alchemist hut block.
 */
public class BlockHutAlchemist extends AbstractBlockHut<BlockHutAlchemist>
{
    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutalchemist";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.alchemist.get();
    }
}
