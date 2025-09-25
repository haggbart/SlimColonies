package no.monopixel.slimcolonies.api.eventbus.events.colony.citizens;

import no.monopixel.slimcolonies.api.colony.ICitizen;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.eventbus.events.colony.AbstractColonyModEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract event for citizen related things.
 */
public class AbstractCitizenModEvent extends AbstractColonyModEvent
{
    /**
     * The citizen related to the event.
     */
    @NotNull
    private final ICitizenData citizen;

    /**
     * Constructs a citizen-based event.
     *
     * @param citizen the citizen related to the event.
     */
    protected AbstractCitizenModEvent(@NotNull final ICitizenData citizen)
    {
        super(citizen.getColony());
        this.citizen = citizen;
    }

    /**
     * Get the citizen related to the event.
     *
     * @return the citizen instance.
     */
    @NotNull
    public ICitizen getCitizen()
    {
        return citizen;
    }
}
