package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Block of the gate house hut.
 */
public class BlockHutGateHouse extends AbstractBlockHut<BlockHutGateHouse>
{
    /**
     * Default constructor.
     */
    public BlockHutGateHouse()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutgatehouse";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.gateHouse.get();
    }
}
