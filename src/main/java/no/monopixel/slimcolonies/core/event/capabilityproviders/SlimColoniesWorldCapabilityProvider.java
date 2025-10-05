package no.monopixel.slimcolonies.core.event.capabilityproviders;

import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import no.monopixel.slimcolonies.api.colony.IChunkmanagerCapability;

import javax.annotation.Nonnull;

import static no.monopixel.slimcolonies.core.SlimColonies.CHUNK_STORAGE_UPDATE_CAP;

/**
 * Capability provider for the world capability of Minecolonies.
 */
public class SlimColoniesWorldCapabilityProvider implements ICapabilitySerializable<Tag>
{
    /**
     * The chunk map capability.
     */
    private final IChunkmanagerCapability chunkMap;

    /**
     * The chunk map capability optional.
     */
    private final LazyOptional<IChunkmanagerCapability> chunkMapOptional;

    /**
     * Constructor of the provider.
     */
    public SlimColoniesWorldCapabilityProvider()
    {
        this.chunkMap = new IChunkmanagerCapability.Impl();
        this.chunkMapOptional = LazyOptional.of(() -> chunkMap);
    }

    @Override
    public Tag serializeNBT()
    {
        return IChunkmanagerCapability.Storage.writeNBT(CHUNK_STORAGE_UPDATE_CAP, chunkMap, null);
    }

    @Override
    public void deserializeNBT(final Tag nbt)
    {
        IChunkmanagerCapability.Storage.readNBT(CHUNK_STORAGE_UPDATE_CAP, chunkMap, null, nbt);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, final Direction direction)
    {
        return cap == CHUNK_STORAGE_UPDATE_CAP ? chunkMapOptional.cast() : LazyOptional.empty();
    }
}
