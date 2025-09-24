package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the beekeeper. No different from {@link AbstractBlockHut}
 */
public class BlockHutBeekeeper extends AbstractBlockHut<BlockHutBeekeeper>
{
    /**
     * Method to return the name of the block.
     *
     * @return Name of the block.
     */
    @Override
    public String getHutName()
    {
        return "blockhutbeekeeper";
    }

    /**
     * Method to get the building registry entry.
     *
     * @return The building entry.
     */
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.beekeeper.get();
    }
}
