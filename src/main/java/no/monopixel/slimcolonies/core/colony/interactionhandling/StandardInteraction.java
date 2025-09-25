package no.monopixel.slimcolonies.core.colony.interactionhandling;

import no.monopixel.slimcolonies.api.colony.ICitizen;
import no.monopixel.slimcolonies.api.colony.interactionhandling.IChatPriority;
import no.monopixel.slimcolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import no.monopixel.slimcolonies.api.colony.interactionhandling.InteractionValidatorRegistry;
import no.monopixel.slimcolonies.api.colony.interactionhandling.ModInteractionResponseHandlers;
import no.monopixel.slimcolonies.api.util.Tuple;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

/**
 * The server side interaction response handler.
 */
public class StandardInteraction extends ServerCitizenInteraction
{
    /**
     * Standard responses
     */
    public static final String INTERACTION_R_OKAY   = "no.monopixel.slimcolonies.coremod.gui.chat.okay";
    public static final String INTERACTION_R_IGNORE = "no.monopixel.slimcolonies.coremod.gui.chat.ignore";
    public static final String INTERACTION_R_REMIND = "no.monopixel.slimcolonies.coremod.gui.chat.remindmelater";
    public static final String INTERACTION_R_SKIP   = "no.monopixel.slimcolonies.coremod.gui.chat.skipchitchat";

    @SuppressWarnings("unchecked")
    private static final Tuple<Component, Component>[] tuples = (Tuple<Component, Component>[]) new Tuple[] {
        new Tuple<>(Component.translatable(INTERACTION_R_OKAY), null),
        new Tuple<>(Component.translatable(INTERACTION_R_IGNORE), null),
        new Tuple<>(Component.translatable(INTERACTION_R_REMIND), null),
        new Tuple<>(Component.translatable(INTERACTION_R_SKIP), null)};

    /**
     * The server interaction response handler with custom validator.
     *
     * @param inquiry   the client inquiry.
     * @param validator the id of the validator.
     * @param priority  the interaction priority.
     */
    public StandardInteraction(
        final Component inquiry,
        final Component validator,
        final IChatPriority priority)
    {
        super(inquiry, true, priority, InteractionValidatorRegistry.getStandardInteractionValidatorPredicate(validator), validator, tuples);
    }

    /**
     * The server interaction response handler.
     *
     * @param inquiry  the client inquiry.
     * @param priority the interaction priority.
     */
    public StandardInteraction(
        final Component inquiry,
        final IChatPriority priority)
    {
        super(inquiry, true, priority, InteractionValidatorRegistry.getStandardInteractionValidatorPredicate(inquiry), inquiry, tuples);
    }

    /**
     * Way to load the response handler for a citizen.
     *
     * @param data the citizen owning this handler.
     */
    public StandardInteraction(final ICitizen data)
    {
        super(data);
    }

    @Override
    public List<IInteractionResponseHandler> genChildInteractions()
    {
        return Collections.emptyList();
    }

    @Override
    public String getType()
    {
        return ModInteractionResponseHandlers.STANDARD.getPath();
    }
}
