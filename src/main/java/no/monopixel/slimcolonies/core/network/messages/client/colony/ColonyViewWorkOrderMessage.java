package no.monopixel.slimcolonies.core.network.messages.client.colony;

import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.colony.workorders.IServerWorkOrder;
import no.monopixel.slimcolonies.api.network.IMessage;
import no.monopixel.slimcolonies.core.colony.Colony;
import no.monopixel.slimcolonies.core.colony.workorders.view.AbstractWorkOrderView;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Add or Update a ColonyView on the client.
 */
public class ColonyViewWorkOrderMessage implements IMessage
{
    private int                colonyId;
    private ResourceKey<Level> dimension;
    private FriendlyByteBuf       workOrderBuffer;

    /**
     * Empty constructor used when registering the
     */
    public ColonyViewWorkOrderMessage()
    {
        super();
    }

    /**
     * Updates a {@link AbstractWorkOrderView} of the workOrders.
     *
     * @param colony        colony of the workOrder.
     * @param workOrderList list of workorders to send to the client
     */
    public ColonyViewWorkOrderMessage(@NotNull final Colony colony, @NotNull final List<IServerWorkOrder> workOrderList)
    {
        this.colonyId = colony.getID();
        this.workOrderBuffer = new FriendlyByteBuf(Unpooled.buffer());
        this.dimension = colony.getDimension();

        workOrderBuffer.writeInt(workOrderList.size());
        for (final IServerWorkOrder workOrder : workOrderList)
        {
            workOrder.serializeViewNetworkData(workOrderBuffer);
        }
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        final FriendlyByteBuf newbuf = new FriendlyByteBuf(buf.retain());
        colonyId = newbuf.readInt();
        dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(newbuf.readUtf(32767)));
        workOrderBuffer = newbuf;
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        workOrderBuffer.resetReaderIndex();
        buf.writeInt(colonyId);
        buf.writeUtf(dimension.location().toString());
        buf.writeBytes(workOrderBuffer);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        IColonyManager.getInstance().handleColonyViewWorkOrderMessage(colonyId, workOrderBuffer, dimension);
        workOrderBuffer.release();
    }
}
