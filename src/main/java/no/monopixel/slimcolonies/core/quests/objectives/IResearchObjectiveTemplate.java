package no.monopixel.slimcolonies.core.quests.objectives;

import no.monopixel.slimcolonies.api.quests.IQuestInstance;

/**
 * Specific objective for research tracking.
 */
public interface IResearchObjectiveTemplate
{
    /**
     * Callback for research completion event.
     * @param questInstance the quest instance.
     */
    void onResearchCompletion(final IQuestInstance questInstance);
}
