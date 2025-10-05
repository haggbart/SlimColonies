package no.monopixel.slimcolonies.core.colony.interactionhandling.registry;

import net.minecraftforge.registries.IForgeRegistry;
import no.monopixel.slimcolonies.api.ISlimColoniesAPI;
import no.monopixel.slimcolonies.api.colony.interactionhandling.registry.InteractionResponseHandlerEntry;

public interface IInteractionResponseHandlerRegistry
{
    static IForgeRegistry<InteractionResponseHandlerEntry> getInstance()
    {
        return ISlimColoniesAPI.getInstance().getInteractionResponseHandlerRegistry();
    }
}
