package no.monopixel.slimcolonies.core.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.ldtteam.blockui.views.BOWindow;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyView;
import no.monopixel.slimcolonies.api.colony.buildings.workerbuildings.IWareHouse;
import no.monopixel.slimcolonies.api.colony.requestsystem.resolver.IRequestResolver;
import no.monopixel.slimcolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import no.monopixel.slimcolonies.api.tileentities.AbstractTileEntityRack;
import no.monopixel.slimcolonies.api.tileentities.AbstractTileEntityWareHouse;
import no.monopixel.slimcolonies.api.util.constant.TypeConstants;
import no.monopixel.slimcolonies.core.blocks.BlockMinecoloniesRack;
import no.monopixel.slimcolonies.core.client.gui.WindowHutMinPlaceholder;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.modules.CourierAssignmentModule;
import no.monopixel.slimcolonies.core.colony.buildings.modules.WarehouseModule;
import no.monopixel.slimcolonies.core.colony.buildings.views.AbstractBuildingView;
import no.monopixel.slimcolonies.core.colony.requestsystem.resolvers.DeliveryRequestResolver;
import no.monopixel.slimcolonies.core.colony.requestsystem.resolvers.PickupRequestResolver;
import no.monopixel.slimcolonies.core.colony.requestsystem.resolvers.WarehouseConcreteRequestResolver;
import no.monopixel.slimcolonies.core.colony.requestsystem.resolvers.WarehouseRequestResolver;
import no.monopixel.slimcolonies.core.tileentities.TileEntityColonyBuilding;
import no.monopixel.slimcolonies.core.tileentities.TileEntityRack;
import no.monopixel.slimcolonies.core.tileentities.TileEntityWareHouse;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the warehouse building.
 */
public class BuildingWareHouse extends AbstractBuilding implements IWareHouse
{
    /**
     * String describing the Warehouse.
     */
    private static final String WAREHOUSE = "warehouse";

    /**
     * Max level of the building.
     */
    private static final int MAX_LEVEL = 5;

    /**
     * Max storage upgrades.
     */
    public static final int MAX_STORAGE_UPGRADE = 3;

    /**
     * Instantiates a new warehouse building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingWareHouse(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @Override
    public void requestRepair(final BlockPos builder)
    {
        //To ensure that the racks are all set to in the warehouse when repaired.
        for (final BlockPos pos : containerList)
        {
            if (getColony().getWorld() != null)
            {
                final BlockEntity entity = getColony().getWorld().getBlockEntity(pos);
                if (entity instanceof TileEntityRack)
                {
                    ((AbstractTileEntityRack) entity).setInWarehouse(true);
                }
            }
        }

        super.requestRepair(builder);
    }

    @Override
    public boolean canAccessWareHouse(final ICitizenData citizenData)
    {
        return getFirstModuleOccurance(CourierAssignmentModule.class).hasAssignedCitizen(citizenData);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return WAREHOUSE;
    }

    /**
     * Returns the tile entity that belongs to the colony building.
     *
     * @return {@link TileEntityColonyBuilding} object of the building.
     */
    @Override
    public AbstractTileEntityWareHouse getTileEntity()
    {
        final AbstractTileEntityColonyBuilding entity = super.getTileEntity();
        return !(entity instanceof TileEntityWareHouse) ? null : (AbstractTileEntityWareHouse) entity;
    }

    @Override
    public boolean hasContainerPosition(final BlockPos inDimensionLocation)
    {
        return containerList.contains(inDimensionLocation) || getLocation().getInDimensionLocation().equals(inDimensionLocation);
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_LEVEL;
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final Level world)
    {
        if (block instanceof BlockMinecoloniesRack)
        {
            final BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof TileEntityRack)
            {
                ((AbstractTileEntityRack) entity).setInWarehouse(true);
                while (((TileEntityRack) entity).getUpgradeSize() < getFirstModuleOccurance(WarehouseModule.class).getStorageUpgrade())
                {
                    ((TileEntityRack) entity).upgradeRackSize();
                }
            }
        }
        super.registerBlockPosition(block, pos, world);
    }

    @Override
    public ImmutableCollection<IRequestResolver<?>> createResolvers()
    {
        final ImmutableCollection<IRequestResolver<?>> supers = super.createResolvers();
        final ImmutableList.Builder<IRequestResolver<?>> builder = ImmutableList.builder();

        builder.addAll(supers);
        builder.add(new WarehouseRequestResolver(getRequester().getLocation(),
                getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN)),
            new WarehouseConcreteRequestResolver(getRequester().getLocation(),
                getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN))
        );

        builder.add(new DeliveryRequestResolver(getRequester().getLocation(),
            getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN)));
        builder.add(new PickupRequestResolver(getRequester().getLocation(),
            getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN)));

        return builder.build();
    }

    /**
     * Upgrade all containers by 9 slots.
     *
     * @param world the world object.
     */
    @Override
    public void upgradeContainers(final Level world)
    {
        if (getFirstModuleOccurance(WarehouseModule.class).getStorageUpgrade() < MAX_STORAGE_UPGRADE)
        {
            for (final BlockPos pos : getContainers())
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TileEntityRack && !(entity instanceof TileEntityColonyBuilding))
                {
                    ((AbstractTileEntityRack) entity).upgradeRackSize();
                }
            }
            getFirstModuleOccurance(WarehouseModule.class).incrementStorageUpgrade();
        }
        markDirty();
    }

    /**
     * BuildWarehouse View.
     */
    public static class View extends AbstractBuildingView
    {
        /**
         * Instantiate the warehouse view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public BOWindow getWindow()
        {
            return new WindowHutMinPlaceholder<>(this);
        }
    }
}
