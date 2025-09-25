package no.monopixel.slimcolonies.core.colony.requestsystem.resolvers.factory;

import com.google.common.reflect.TypeToken;
import no.monopixel.slimcolonies.api.IMinecoloniesAPI;
import no.monopixel.slimcolonies.api.colony.jobs.registry.IJobRegistry;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.colony.requestsystem.factory.IFactoryController;
import no.monopixel.slimcolonies.api.colony.requestsystem.location.ILocation;
import no.monopixel.slimcolonies.api.colony.requestsystem.resolver.IRequestResolverFactory;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;
import no.monopixel.slimcolonies.api.util.constant.SerializationIdentifierConstants;
import no.monopixel.slimcolonies.api.util.constant.TypeConstants;
import no.monopixel.slimcolonies.core.colony.requestsystem.resolvers.PublicWorkerCraftingRequestResolver;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class PublicWorkerCraftingRequestResolverFactory implements IRequestResolverFactory<PublicWorkerCraftingRequestResolver>
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TOKEN    = "Token";
    private static final String NBT_LOCATION = "Location";
    private static final String NBT_JOB = "Job";

    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    @Override
    public TypeToken<? extends PublicWorkerCraftingRequestResolver> getFactoryOutputType()
    {
        return TypeToken.of(PublicWorkerCraftingRequestResolver.class);
    }

    @NotNull
    @Override
    public TypeToken<? extends ILocation> getFactoryInputType()
    {
        return TypeConstants.ILOCATION;
    }

    @NotNull
    @Override
    public PublicWorkerCraftingRequestResolver getNewInstance(
      @NotNull final IFactoryController factoryController,
      @NotNull final ILocation iLocation,
      @NotNull final Object... context)
    {
        return new PublicWorkerCraftingRequestResolver(iLocation, factoryController.getNewInstance(TypeConstants.ITOKEN), (JobEntry) context[0]);
    }

    @NotNull
    @Override
    public CompoundTag serialize(@NotNull final IFactoryController controller, @NotNull final PublicWorkerCraftingRequestResolver publicWorkerCraftingRequestResolverFactory)
    {
        final CompoundTag compound = new CompoundTag();
        compound.put(NBT_TOKEN, controller.serialize(publicWorkerCraftingRequestResolverFactory.getId()));
        compound.put(NBT_LOCATION, controller.serialize(publicWorkerCraftingRequestResolverFactory.getLocation()));
        compound.putString(NBT_JOB, IJobRegistry.getInstance().getKey(publicWorkerCraftingRequestResolverFactory.getJobEntry()).toString());

        return compound;
    }

    @NotNull
    @Override
    public PublicWorkerCraftingRequestResolver deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
    {
        final IToken<?> token = controller.deserialize(nbt.getCompound(NBT_TOKEN));
        final ILocation location = controller.deserialize(nbt.getCompound(NBT_LOCATION));
        final JobEntry entry = IJobRegistry.getInstance().getValue(new ResourceLocation(nbt.getString(NBT_JOB)));

        return new PublicWorkerCraftingRequestResolver(location, token, entry);
    }

    @Override
    public void serialize(IFactoryController controller, PublicWorkerCraftingRequestResolver input, FriendlyByteBuf packetBuffer)
    {
        controller.serialize(packetBuffer, input.getId());
        controller.serialize(packetBuffer, input.getLocation());
        packetBuffer.writeRegistryId(IMinecoloniesAPI.getInstance().getJobRegistry(), input.getJobEntry());
    }

    @Override
    public PublicWorkerCraftingRequestResolver deserialize(IFactoryController controller, FriendlyByteBuf buffer) throws Throwable
    {
        final IToken<?> token = controller.deserialize(buffer);
        final ILocation location = controller.deserialize(buffer);
        final JobEntry entry = buffer.readRegistryId();
        return new PublicWorkerCraftingRequestResolver(location, token, entry);
    }

    @Override
    public short getSerializationId()
    {
        return SerializationIdentifierConstants.PUBLIC_WORKER_CRAFTING_REQUEST_RESOLVER_ID;
    }
}
