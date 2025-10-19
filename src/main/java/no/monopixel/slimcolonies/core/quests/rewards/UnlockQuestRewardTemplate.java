package no.monopixel.slimcolonies.core.quests.rewards;

import com.google.gson.JsonObject;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.quests.IQuestInstance;
import no.monopixel.slimcolonies.api.quests.IQuestRewardTemplate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import static no.monopixel.slimcolonies.api.quests.QuestParseConstant.*;

/**
 * Quest unlock reward template.
 */
public class UnlockQuestRewardTemplate implements IQuestRewardTemplate
{
    /**
     * The quest to unlock
     */
    private final ResourceLocation questId;

    /**
     * Setup the quest unlock reward.
     */
    public UnlockQuestRewardTemplate(final ResourceLocation questId)
    {
        this.questId = questId;
    }

    /**
     * Create the reward.
     * @param jsonObject the json to read from.
     * @return the reward object.
     */
    public static IQuestRewardTemplate createReward(final JsonObject jsonObject)
    {
        JsonObject details = jsonObject.getAsJsonObject(DETAILS_KEY);
        final String id = details.get(ID_KEY).getAsString();

        return new UnlockQuestRewardTemplate(ResourceLocation.parse(id));
    }
    @Override
    public void applyReward(final IColony colony, final Player player, final IQuestInstance colonyQuest)
    {
        colony.getQuestManager().unlockQuest(this.questId);
    }
}
