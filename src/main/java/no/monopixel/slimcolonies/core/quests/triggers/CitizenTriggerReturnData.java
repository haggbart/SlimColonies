package no.monopixel.slimcolonies.core.quests.triggers;

import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.quests.ITriggerReturnData;

/**
 * Wrapper around a citizen id.
 */
public class CitizenTriggerReturnData implements ITriggerReturnData<ICitizenData>
{
    /**
     * The citizen id or - max int if negative.
     */
    private final ICitizenData match;

    /**
     * Create a new return data obj.
     * @param match citizen id.
     */
    public CitizenTriggerReturnData(final ICitizenData match)
    {
        this.match = match;
    }

    @Override
    public boolean isPositive()
    {
        return this.match != null;
    }

    @Override
    public ICitizenData getContent()
    {
        return match;
    }
}
