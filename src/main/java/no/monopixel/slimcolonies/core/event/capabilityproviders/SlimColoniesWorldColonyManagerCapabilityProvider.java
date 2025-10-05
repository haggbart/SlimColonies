package no.monopixel.slimcolonies.core.event.capabilityproviders;

import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import no.monopixel.slimcolonies.core.colony.IColonyManagerCapability;

import javax.annotation.Nonnull;

import static no.monopixel.slimcolonies.core.SlimColonies.COLONY_MANAGER_CAP;

/**
 * Capability provider for the world capability of Minecolonies.
 */
public class SlimColoniesWorldColonyManagerCapabilityProvider implements ICapabilitySerializable<Tag>
{
    /**
     * The chunk map capability optional.
     */
    private final LazyOptional<IColonyManagerCapability> colonyManagerOptional;

    /**
     * The chunk map capability.
     */
    private final IColonyManagerCapability colonyManager;

    /**
     * Is this the main overworld cap?
     */
    private final boolean overworld;

    /**
     * Constructor of the provider.
     */
    public SlimColoniesWorldColonyManagerCapabilityProvider(final boolean overworld)
    {
        this.colonyManager = new IColonyManagerCapability.Impl();
        this.colonyManagerOptional = LazyOptional.of(() -> colonyManager);
        this.overworld = overworld;
    }

    @Override
    public Tag serializeNBT()
    {
        return IColonyManagerCapability.Storage.writeNBT(COLONY_MANAGER_CAP, colonyManager, overworld);
    }

    @Override
    public void deserializeNBT(final Tag nbt)
    {
        IColonyManagerCapability.Storage.readNBT(COLONY_MANAGER_CAP, colonyManager, overworld, nbt);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, final Direction dir)
    {
        return cap == COLONY_MANAGER_CAP ? colonyManagerOptional.cast() : LazyOptional.empty();
    }
}
