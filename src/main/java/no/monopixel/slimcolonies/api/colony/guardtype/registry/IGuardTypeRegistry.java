package no.monopixel.slimcolonies.api.colony.guardtype.registry;

import net.minecraftforge.registries.IForgeRegistry;
import no.monopixel.slimcolonies.api.ISlimColoniesAPI;
import no.monopixel.slimcolonies.api.colony.guardtype.GuardType;

public interface IGuardTypeRegistry
{

    static IForgeRegistry<GuardType> getInstance()
    {
        return ISlimColoniesAPI.getInstance().getGuardTypeRegistry();
    }
}
