package no.monopixel.slimcolonies.core.event.capabilityproviders;

import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import no.monopixel.slimcolonies.api.colony.IColonyTagCapability;

import javax.annotation.Nonnull;

import static no.monopixel.slimcolonies.api.colony.IColony.CLOSE_COLONY_CAP;

/**
 * Capability provider for the chunk capability.
 */
public class SlimColoniesChunkCapabilityProvider implements ICapabilitySerializable<Tag>
{
    /**
     * The colony list capability. (For closest colony and claimed)
     */
    private final IColonyTagCapability tag;

    /**
     * The colony list capability optional.
     */
    private final LazyOptional<IColonyTagCapability> tagOptional;

    /**
     * Constructor of the provider.
     */
    public SlimColoniesChunkCapabilityProvider()
    {
        this.tag = new IColonyTagCapability.Impl();
        this.tagOptional = LazyOptional.of(() -> tag);
    }

    @Override
    public Tag serializeNBT()
    {
        return IColonyTagCapability.Storage.writeNBT(CLOSE_COLONY_CAP, tag, null);
    }

    @Override
    public void deserializeNBT(final Tag nbt)
    {
        IColonyTagCapability.Storage.readNBT(CLOSE_COLONY_CAP, tag, null, nbt);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, final Direction direction)
    {
        return cap == CLOSE_COLONY_CAP ? tagOptional.cast() : LazyOptional.empty();
    }
}
