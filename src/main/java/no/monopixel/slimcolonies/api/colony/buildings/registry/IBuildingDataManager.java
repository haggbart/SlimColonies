package no.monopixel.slimcolonies.api.colony.buildings.registry;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import no.monopixel.slimcolonies.api.ISlimColoniesAPI;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyView;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.tileentities.AbstractTileEntityColonyBuilding;

/**
 * Helper manager to analyse and process the registry for {@link BuildingEntry}.
 */
public interface IBuildingDataManager
{

    static IBuildingDataManager getInstance()
    {
        return ISlimColoniesAPI.getInstance().getBuildingDataManager();
    }

    /**
     * Creates a new entry from a given {@link IColony} and the data passed in as {@link CompoundTag}.
     *
     * @param colony   The {@link IColony} to which the new {@link IBuilding} belongs.
     * @param compound The data from which to load new {@link IBuilding} stored in a {@link CompoundTag}.
     * @return The {@link IBuilding} with the data loaded from {@link CompoundTag}.
     */
    IBuilding createFrom(final IColony colony, final CompoundTag compound);

    /**
     * Creates a new entry from a given {@link IColony} and the data passed in as {@link AbstractTileEntityColonyBuilding}.
     *
     * @param colony                   The {@link IColony} to which the new {@link IBuilding} belongs.
     * @param tileEntityColonyBuilding The data from which to load new {@link IBuilding} stored in a {@link AbstractTileEntityColonyBuilding}.
     * @return The {@link IBuilding} with the data loaded from {@link AbstractTileEntityColonyBuilding}.
     */
    IBuilding createFrom(final IColony colony, final AbstractTileEntityColonyBuilding tileEntityColonyBuilding);

    /**
     * Creates a new entry from a given {@link IColony} and the data passed in as {@link AbstractTileEntityColonyBuilding}.
     *
     * @param colony       The {@link IColony} to which the new {@link IBuilding} belongs.
     * @param position     The position on which the new {@link IBuilding} is created.
     * @param buildingName The name of the {@link IBuilding} as registered to the registry.
     * @return The {@link IBuilding} with the data loaded from {@link AbstractTileEntityColonyBuilding}.
     */
    IBuilding createFrom(final IColony colony, BlockPos position, final ResourceLocation buildingName);

    /**
     * Creates a new entry from a given {@link IColonyView}, the position as {@link BlockPos} and the data passed in as {@link ByteBuf}.
     *
     * @param colony        The {@link IColonyView} to which the new {@link IBuildingView} belongs.
     * @param position      The position of the new {@link IBuildingView}.
     * @param networkBuffer The data from which to load the new {@link IBuildingView} stored in the networks {@link ByteBuf}.
     * @return The {@link IBuildingView} with the data loaded from the {@link ByteBuf}.
     */
    IBuildingView createViewFrom(final IColonyView colony, final BlockPos position, final FriendlyByteBuf networkBuffer);

    /**
     * Opens the building browser window for the specific building type.  Client side only.
     *
     * @param block The building block.
     */
    void openBuildingBrowser(final Block block);
}
