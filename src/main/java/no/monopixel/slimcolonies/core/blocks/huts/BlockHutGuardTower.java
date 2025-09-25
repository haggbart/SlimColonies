package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Block of the GuardTower hut.
 */
public class BlockHutGuardTower extends AbstractBlockHut<BlockHutGuardTower>
{
    /**
     * Default constructor.
     */
    public BlockHutGuardTower()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutguardtower";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.guardTower.get();
    }
}
