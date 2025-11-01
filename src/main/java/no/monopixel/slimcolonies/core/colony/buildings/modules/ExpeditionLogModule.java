package no.monopixel.slimcolonies.core.colony.buildings.modules;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import no.monopixel.slimcolonies.api.colony.buildings.modules.AbstractBuildingModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.IPersistentModule;
import no.monopixel.slimcolonies.core.colony.buildings.modules.expedition.ExpeditionLog;
import org.jetbrains.annotations.NotNull;

/**
 * Building module that stores an expedition log
 */
public class ExpeditionLogModule extends AbstractBuildingModule implements IPersistentModule
{
    private static final String TAG_LOG = "expedition";

    @NotNull
    private final ExpeditionLog log = new ExpeditionLog();

    public ExpeditionLogModule()
    {
    }

    @NotNull
    public ExpeditionLog getLog()
    {
        return this.log;
    }

    @Override
    public void serializeNBT(@NotNull final CompoundTag compound)
    {
        this.log.serializeNBT(compound);
    }

    @Override
    public void deserializeNBT(@NotNull final CompoundTag compound)
    {
        final CompoundTag log = compound.contains(TAG_LOG) ? compound.getCompound(TAG_LOG) : compound;
        this.log.deserializeNBT(log);
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBoolean(true);
        this.log.serialize(buf);
    }
}
