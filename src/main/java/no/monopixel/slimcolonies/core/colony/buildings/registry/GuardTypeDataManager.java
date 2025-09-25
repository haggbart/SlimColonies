package no.monopixel.slimcolonies.core.colony.buildings.registry;

import no.monopixel.slimcolonies.api.colony.guardtype.GuardType;
import no.monopixel.slimcolonies.api.colony.guardtype.registry.IGuardTypeDataManager;
import no.monopixel.slimcolonies.api.colony.guardtype.registry.IGuardTypeRegistry;
import net.minecraft.resources.ResourceLocation;

public final class GuardTypeDataManager implements IGuardTypeDataManager
{
    @Override
    public GuardType getFrom(final ResourceLocation jobName)
    {
        if (jobName == null)
        {
            return null;
        }

        return IGuardTypeRegistry.getInstance().getValue(jobName);
    }
}
