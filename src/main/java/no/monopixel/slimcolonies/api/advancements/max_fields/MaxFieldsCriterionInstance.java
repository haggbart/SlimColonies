package no.monopixel.slimcolonies.api.advancements.max_fields;

import no.monopixel.slimcolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.resources.ResourceLocation;

/**
 * All towers criterion instance.
 */
public class MaxFieldsCriterionInstance extends AbstractCriterionTriggerInstance
{
    public MaxFieldsCriterionInstance()
    {
        super(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, Constants.CRITERION_MAX_FIELDS), ContextAwarePredicate.ANY);
    }
}
