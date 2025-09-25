package no.monopixel.slimcolonies.core.quests.triggers;

import com.google.gson.JsonObject;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.quests.IQuestTriggerTemplate;
import no.monopixel.slimcolonies.api.quests.ITriggerReturnData;

import static no.monopixel.slimcolonies.api.quests.QuestParseConstant.QUANTITY_KEY;

/**
 * Quest reputation quest trigger.
 */
public class QuestReputationTriggerTemplate implements IQuestTriggerTemplate
{
    /**
     * Min quantity.
     */
    private final double minQuantity;

    /**
     * Create a new instance of this trigger.
     */
    public QuestReputationTriggerTemplate(final double minQuantity)
    {
        this.minQuantity = minQuantity;
    }

    /**
     * Create a new trigger directly from json.
     * @param jsonObj the json associated to this trigger.
     */
    public static QuestReputationTriggerTemplate createQuestReputationTrigger(final JsonObject jsonObj)
    {
        return new QuestReputationTriggerTemplate(jsonObj.get(QUANTITY_KEY).getAsDouble());
    }

    @Override
    public ITriggerReturnData canTriggerQuest(final IColony colony)
    {
        return new BooleanTriggerReturnData(colony.getQuestManager().getReputation() >= minQuantity);
    }
}
