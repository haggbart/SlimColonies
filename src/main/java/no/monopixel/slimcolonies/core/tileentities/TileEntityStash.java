package no.monopixel.slimcolonies.core.tileentities;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.tileentities.MinecoloniesTileEntities;
import no.monopixel.slimcolonies.api.util.MessageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

import static no.monopixel.slimcolonies.api.colony.requestsystem.requestable.deliveryman.AbstractDeliverymanRequestable.getPlayerActionPriority;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_DELIVERYMAN_FORCEPICKUP;

/**
 * Class which handles the tileEntity for the Stash block.
 */
public class TileEntityStash extends TileEntityColonyBuilding
{
    /**
     * Constructor of the stash based on a tile entity type
     *
     * @param type tile entity type
     */
    public TileEntityStash(final BlockEntityType<? extends TileEntityStash> type, final BlockPos pos, final BlockState state)
    {
        super(type, pos, state);
    }

    /**
     * Default constructor of the stash
     */
    public TileEntityStash(final BlockPos pos, final BlockState state)
    {
        super(MinecoloniesTileEntities.STASH.get(), pos, state);
    }

    @Override
    public ItemStackHandler createInventory(final int slots)
    {
        return new NotifyingRackInventory(slots);
    }

    /**
     * An {@link ItemStackHandler} that notifies the container TileEntity when it's inventory has changed.
     */
    public class NotifyingRackInventory extends RackInventory
    {
        public NotifyingRackInventory(final int defaultSize)
        {
            super(defaultSize);
        }

        @Override
        protected void onContentsChanged(final int slot)
        {
            super.onContentsChanged(slot);

            if (level != null && !level.isClientSide && IColonyManager.getInstance().isCoordinateInAnyColony(level, worldPosition))
            {
                final IColony colony = IColonyManager.getInstance().getClosestColony(level, worldPosition);
                if (colony != null)
                {
                    final IBuilding building = colony.getBuildingManager().getBuilding(worldPosition);
                    // Note that createPickupRequest will make sure to only create on request per building.
                    if (!isEmpty() && building != null && building.createPickupRequest(getPlayerActionPriority(true)))
                    {
                        MessageUtils.format(COM_MINECOLONIES_COREMOD_ENTITY_DELIVERYMAN_FORCEPICKUP).sendToClose(getTilePos(), 6, colony.getMessagePlayerEntities());
                    }
                }
            }
        }
    }
}
