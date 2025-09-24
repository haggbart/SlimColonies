package no.monopixel.slimcolonies.core.colony.buildings.modules;

import no.monopixel.slimcolonies.api.colony.buildings.modules.AbstractBuildingModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.IPersistentModule;
import no.monopixel.slimcolonies.api.colony.requestsystem.StandardFactoryController;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_REQUEST;

/**
 * The class of the citizen hut.
 */
public class WarehouseRequestQueueModule extends AbstractBuildingModule implements IPersistentModule
{
    /**
     * List of all beds.
     */
    @NotNull
    private final List<IToken<?>> requestList = new ArrayList<>();

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        final ListTag requestTagList = compound.getList(TAG_REQUEST, Tag.TAG_COMPOUND);
        for (int i = 0; i < requestTagList.size(); ++i)
        {
            requestList.add(StandardFactoryController.getInstance().deserialize(requestTagList.getCompound(i)));
        }
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        if (!requestList.isEmpty())
        {
            @NotNull final ListTag requestTagList = new ListTag();
            for (@NotNull final IToken<?> token : requestList)
            {
                requestTagList.add(StandardFactoryController.getInstance().serialize(token));
            }
            compound.put(TAG_REQUEST, requestTagList);
        }
    }

    @Override
    public void serializeToView(final FriendlyByteBuf buf)
    {
        super.serializeToView(buf);
        buf.writeInt(requestList.size());
        for (final IToken<?> reqId : requestList)
        {
            StandardFactoryController.getInstance().serialize(buf, reqId);
        }
    }

    /**
     * Add request to warehouse queue.
     * @param requestToken request to add.
     */
    public void addRequest(IToken<?> requestToken)
    {
        requestList.add(requestToken);
        markDirty();
    }

    /**
     * Get a mutable version of the request list.
     * @return the mutable request list.
     */
    public List<IToken<?>> getMutableRequestList()
    {
        return requestList;
    }
}
