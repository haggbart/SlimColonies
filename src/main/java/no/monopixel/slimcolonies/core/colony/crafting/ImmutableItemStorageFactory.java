package no.monopixel.slimcolonies.core.colony.crafting;

import com.google.common.reflect.TypeToken;
import no.monopixel.slimcolonies.api.colony.requestsystem.StandardFactoryController;
import no.monopixel.slimcolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import no.monopixel.slimcolonies.api.colony.requestsystem.factory.IFactoryController;
import no.monopixel.slimcolonies.api.crafting.IImmutableItemStorageFactory;
import no.monopixel.slimcolonies.api.crafting.ImmutableItemStorage;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.api.util.constant.SerializationIdentifierConstants;
import no.monopixel.slimcolonies.api.util.constant.TypeConstants;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * Factory implementation taking care of creating new instances, serializing and deserializing ImmutableItemStorage.
 */
public class ImmutableItemStorageFactory implements IImmutableItemStorageFactory
{

    @NotNull
    @Override
    public TypeToken<? extends ImmutableItemStorage> getFactoryOutputType()
    {
        return TypeConstants.IMMUTABLEITEMSTORAGE;
    }

    @NotNull
    @Override
    public TypeToken<? extends FactoryVoidInput> getFactoryInputType()
    {
        return TypeConstants.FACTORYVOIDINPUT;
    }

    @Override
    public short getSerializationId()
    {
        return SerializationIdentifierConstants.IMMUTABLE_ITEM_STORAGE_ID;
    }

    @Override
    public CompoundTag serialize(IFactoryController controller, ImmutableItemStorage output)
    {
        @NotNull final CompoundTag compound = StandardFactoryController.getInstance().serialize(output.copy());

        return compound;
    }

    @Override
    public ImmutableItemStorage deserialize(IFactoryController controller, CompoundTag nbt) throws Throwable
    {
        final ItemStorage readStorage = StandardFactoryController.getInstance().deserialize(nbt);
        return readStorage.toImmutable();
    }

    @Override
    public void serialize(IFactoryController controller, ImmutableItemStorage output, FriendlyByteBuf packetBuffer)
    {
        StandardFactoryController.getInstance().serialize(packetBuffer, output.copy());
    }

    @Override
    public ImmutableItemStorage deserialize(IFactoryController controller, FriendlyByteBuf buffer) throws Throwable
    {
        @NotNull final ItemStorage newItem = StandardFactoryController.getInstance().deserialize(buffer);
        return newItem.toImmutable();
    }

    @Override
    public ImmutableItemStorage getNewInstance(ItemStack stack, int size)
    {
        @NotNull final ItemStorage newItem = new ItemStorage(stack);
        newItem.setAmount(size);
        return newItem.toImmutable();
    }

}
