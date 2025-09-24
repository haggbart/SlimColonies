package no.monopixel.slimcolonies.core.colony.interactionhandling.registry;

import no.monopixel.slimcolonies.api.IMinecoloniesAPI;
import no.monopixel.slimcolonies.api.colony.interactionhandling.registry.InteractionResponseHandlerEntry;
import net.minecraftforge.registries.IForgeRegistry;

public interface IInteractionResponseHandlerRegistry
{
    static IForgeRegistry<InteractionResponseHandlerEntry> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getInteractionResponseHandlerRegistry();
    }
}
