package no.monopixel.slimcolonies.api.colony.interactionhandling.registry;

import no.monopixel.slimcolonies.api.IMinecoloniesAPI;
import no.monopixel.slimcolonies.api.colony.ICitizen;
import no.monopixel.slimcolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The data manager of the interaction handler.
 */
public interface IInteractionResponseHandlerDataManager
{

    static IInteractionResponseHandlerDataManager getInstance()
    {
        return IMinecoloniesAPI.getInstance().getInteractionResponseHandlerDataManager();
    }

    /**
     * Create an interactionResponseHandler from saved CompoundTag data.
     *
     * @param citizen  The citizen that owns the interaction response handler..
     * @param compound The CompoundTag containing the saved interaction data.
     * @return New InteractionResponseHandler created from the data, or null.
     */
    @Nullable
    IInteractionResponseHandler createFrom(@NotNull ICitizen citizen, @NotNull CompoundTag compound);
}
