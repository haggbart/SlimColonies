package no.monopixel.slimcolonies.core.quests.triggers;

import no.monopixel.slimcolonies.api.quests.ITriggerReturnData;

/**
 * Wrapper around boolean for return data.
 */
public class BooleanTriggerReturnData implements ITriggerReturnData<Boolean>
{
    /**
     * If positive match.
     */
    private final boolean match;

    /**
     * Create a new return data obj.
     * @param match the match to return.
     */
    public BooleanTriggerReturnData(final boolean match)
    {
        this.match = match;
    }

    @Override
    public boolean isPositive()
    {
        return this.match;
    }

    @Override
    public Boolean getContent()
    {
        return this.match;
    }
}
