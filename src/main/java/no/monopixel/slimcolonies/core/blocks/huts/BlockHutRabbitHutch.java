package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the rabbit hutch. No different from {@link AbstractBlockHut}
 */
public class BlockHutRabbitHutch extends AbstractBlockHut<BlockHutRabbitHutch>
{
    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutrabbithutch";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.rabbitHutch.get();
    }
}
