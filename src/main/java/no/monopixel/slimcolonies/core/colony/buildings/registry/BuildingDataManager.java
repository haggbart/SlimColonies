package no.monopixel.slimcolonies.core.colony.buildings.registry;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyView;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import no.monopixel.slimcolonies.api.colony.buildings.registry.IBuildingDataManager;
import no.monopixel.slimcolonies.api.colony.buildings.registry.IBuildingRegistry;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import no.monopixel.slimcolonies.api.util.BlockPosUtil;
import no.monopixel.slimcolonies.api.util.Log;
import no.monopixel.slimcolonies.core.client.gui.WindowBuildingBrowser;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_BUILDING_TYPE;
import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_LOCATION;

public class BuildingDataManager implements IBuildingDataManager
{
    @Override
    public IBuilding createFrom(final IColony colony, final CompoundTag compound)
    {
        final ResourceLocation type = new ResourceLocation(compound.getString(TAG_BUILDING_TYPE));
        final BlockPos pos = BlockPosUtil.read(compound, TAG_LOCATION);

        IBuilding building = this.createFrom(colony, pos, type);

        if (building == null)
        {
            return null;
        }

        try
        {
            building.deserializeNBT(compound);
        }
        catch (final Exception ex)
        {
            Log.getLogger().error(String.format("A Building %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
              type, building.getClass().getName()), ex);
            building = null;
        }

        return building;
    }

    @Override
    public IBuilding createFrom(final IColony colony, final AbstractTileEntityColonyBuilding tileEntityColonyBuilding)
    {
        return this.createFrom(colony, tileEntityColonyBuilding.getPosition(), tileEntityColonyBuilding.getBuildingName());
    }

    @Override
    public IBuilding createFrom(final IColony colony, final BlockPos position, final ResourceLocation buildingName)
    {
        final BuildingEntry entry = IBuildingRegistry.getInstance().getValue(buildingName);
        if (entry == null)
        {
            if (buildingName.getPath().equals("home"))
            {
                return ModBuildings.home.get().produceBuilding(position, colony);
            }
            Log.getLogger().error(String.format("Unknown building type '%s'.", buildingName), new Exception());
            return null;
        }
        return entry.produceBuilding(position, colony);
    }

    @Override
    public IBuildingView createViewFrom(final IColonyView colony, final BlockPos position, final FriendlyByteBuf networkBuffer)
    {
        final ResourceLocation buildingName = new ResourceLocation(networkBuffer.readUtf(32767));
        final BuildingEntry entry = IBuildingRegistry.getInstance().getValue(buildingName);

        if (entry == null)
        {
            Log.getLogger().error(String.format("Unknown building type '%s'.", buildingName), new Exception());
            return null;
        }

        final IBuildingView view = entry.produceBuildingView(position, colony);
        if (view != null)
        {
            view.deserialize(networkBuffer);
        }

        return view;
    }

    @Override
    public void openBuildingBrowser(@NotNull final Block block)
    {
        new WindowBuildingBrowser(block).open();
    }
}
