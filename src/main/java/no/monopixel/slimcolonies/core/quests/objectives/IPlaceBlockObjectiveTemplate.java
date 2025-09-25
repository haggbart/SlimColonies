package no.monopixel.slimcolonies.core.quests.objectives;

import no.monopixel.slimcolonies.api.quests.IObjectiveInstance;
import no.monopixel.slimcolonies.api.quests.IQuestInstance;
import net.minecraft.world.entity.player.Player;

/**
 * Specific objective for block placing.
 */
public interface IPlaceBlockObjectiveTemplate
{
    /**
     * Callback for block place event
     *
     * @param blockPlacementProgressData the objective data.
     * @param player the involved player.
     */
    void onBlockPlace(IObjectiveInstance blockPlacementProgressData, final IQuestInstance colonyQuest, final Player player);
}
