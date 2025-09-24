package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Block of the BarracksTower.
 */
public class BlockHutBarracksTower extends AbstractBlockHut<BlockHutBarracksTower>
{
    /**
     * Default constructor.
     */
    public BlockHutBarracksTower()
    {
        //No different from Abstract parent
        super();
    }

    @Override
    public boolean isVisible(@Nullable final CompoundTag beData)
    {
        return false;
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutbarrackstower";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.barracksTower.get();
    }
}
