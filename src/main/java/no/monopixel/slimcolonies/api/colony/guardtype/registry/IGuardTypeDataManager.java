package no.monopixel.slimcolonies.api.colony.guardtype.registry;

import net.minecraft.resources.ResourceLocation;
import no.monopixel.slimcolonies.api.ISlimColoniesAPI;
import no.monopixel.slimcolonies.api.colony.guardtype.GuardType;

public interface IGuardTypeDataManager
{

    static IGuardTypeDataManager getInstance()
    {
        return ISlimColoniesAPI.getInstance().getGuardTypeDataManager();
    }

    GuardType getFrom(ResourceLocation jobName);
}
