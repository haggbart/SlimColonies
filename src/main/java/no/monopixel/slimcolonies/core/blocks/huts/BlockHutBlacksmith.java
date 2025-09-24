package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the blacksmith. No different from {@link AbstractBlockHut}
 */
public class BlockHutBlacksmith extends AbstractBlockHut<BlockHutBlacksmith>
{
    public BlockHutBlacksmith()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutblacksmith";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.blacksmith.get();
    }
}
