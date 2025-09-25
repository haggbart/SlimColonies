package no.monopixel.slimcolonies.api.colony.guardtype.registry;

import no.monopixel.slimcolonies.api.IMinecoloniesAPI;
import no.monopixel.slimcolonies.api.colony.guardtype.GuardType;
import net.minecraft.resources.ResourceLocation;

public interface IGuardTypeDataManager
{

    static IGuardTypeDataManager getInstance()
    {
        return IMinecoloniesAPI.getInstance().getGuardTypeDataManager();
    }

    GuardType getFrom(ResourceLocation jobName);
}
