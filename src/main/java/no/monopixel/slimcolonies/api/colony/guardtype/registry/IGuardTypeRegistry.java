package no.monopixel.slimcolonies.api.colony.guardtype.registry;

import no.monopixel.slimcolonies.api.IMinecoloniesAPI;
import no.monopixel.slimcolonies.api.colony.guardtype.GuardType;
import net.minecraftforge.registries.IForgeRegistry;

public interface IGuardTypeRegistry
{

    static IForgeRegistry<GuardType> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getGuardTypeRegistry();
    }
}
