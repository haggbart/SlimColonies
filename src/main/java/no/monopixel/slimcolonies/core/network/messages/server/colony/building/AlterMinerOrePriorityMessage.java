package no.monopixel.slimcolonies.core.network.messages.server.colony.building;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.core.colony.buildings.modules.MinerOrePriorityModule;
import no.monopixel.slimcolonies.core.network.messages.server.AbstractBuildingServerMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Alter a miner's ore priority list message.
 */
public class AlterMinerOrePriorityMessage extends AbstractBuildingServerMessage<IBuilding>
{
    private ItemStack oreStack;
    private int id;
    private boolean add;

    /**
     * Empty constructor used when registering the message.
     */
    public AlterMinerOrePriorityMessage()
    {
        super();
    }

    /**
     * Add an ore to the priority list.
     *
     * @param building  the building to add it to.
     * @param oreStack  the ore stack to add.
     * @param runtimeID the id of the module.
     * @return the message.
     */
    public static AlterMinerOrePriorityMessage addOre(final IBuildingView building, final ItemStack oreStack, final int runtimeID)
    {
        return new AlterMinerOrePriorityMessage(building, oreStack, runtimeID, true);
    }

    /**
     * Remove an ore from the priority list.
     *
     * @param building  the building to remove it from.
     * @param oreStack  the ore stack to remove.
     * @param runtimeID the id of the module.
     * @return the message.
     */
    public static AlterMinerOrePriorityMessage removeOre(final IBuildingView building, final ItemStack oreStack, final int runtimeID)
    {
        return new AlterMinerOrePriorityMessage(building, oreStack, runtimeID, false);
    }

    /**
     * Creates an ore priority alteration message.
     *
     * @param building  the building we're executing on.
     * @param oreStack  the ore to be altered.
     * @param runtimeID the id of the module.
     * @param add       if add = true, if remove = false.
     */
    private AlterMinerOrePriorityMessage(final IBuildingView building, final ItemStack oreStack, final int runtimeID, final boolean add)
    {
        super(building);
        this.oreStack = oreStack;
        this.id = runtimeID;
        this.add = add;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        oreStack = buf.readItem();
        id = buf.readInt();
        add = buf.readBoolean();
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeItem(oreStack);
        buf.writeInt(id);
        buf.writeBoolean(add);
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        if (building.getModule(id) instanceof MinerOrePriorityModule priorityModule)
        {
            if (add)
            {
                priorityModule.addPriorityOre(oreStack);
            }
            else
            {
                priorityModule.removePriorityOre(oreStack);
            }
        }
    }
}
