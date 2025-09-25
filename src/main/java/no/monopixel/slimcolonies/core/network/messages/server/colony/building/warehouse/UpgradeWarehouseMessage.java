package no.monopixel.slimcolonies.core.network.messages.server.colony.building.warehouse;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.util.InventoryUtils;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import no.monopixel.slimcolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.network.NetworkEvent;

/**
 * Issues the upgrade of the warehouse pos level 5.
 */
public class UpgradeWarehouseMessage extends AbstractBuildingServerMessage<BuildingWareHouse>
{
    /**
     * Empty constructor used when registering the
     */
    public UpgradeWarehouseMessage()
    {
        super();
    }

    @Override
    protected void toBytesOverride(final FriendlyByteBuf buf)
    {

    }

    @Override
    protected void fromBytesOverride(final FriendlyByteBuf buf)
    {

    }

    public UpgradeWarehouseMessage(final IBuildingView building)
    {
        super(building);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingWareHouse building)
    {
        final Player player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        building.upgradeContainers(player.level);

        final boolean isCreative = player.isCreative();
        if (!isCreative)
        {
            final int slot = InventoryUtils.
                                             findFirstSlotInItemHandlerWith(new InvWrapper(player.getInventory()),
                                               itemStack -> ItemStack.isSameItem(itemStack, new ItemStack(Blocks.EMERALD_BLOCK)));
            player.getInventory().removeItem(slot, 1);
        }
    }
}
