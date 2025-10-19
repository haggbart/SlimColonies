package no.monopixel.slimcolonies.api.advancements.deep_mine;

import no.monopixel.slimcolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.resources.ResourceLocation;

/**
 * All towers criterion instance.
 */
public class DeepMineCriterionInstance extends AbstractCriterionTriggerInstance
{
    public DeepMineCriterionInstance()
    {
        super(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, Constants.CRITERION_DEEP_MINE), ContextAwarePredicate.ANY);
    }
}
