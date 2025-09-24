package no.monopixel.slimcolonies.api.advancements.undertaker_totem;

import no.monopixel.slimcolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.resources.ResourceLocation;

/**
 * An undertaker recieves a totem of undying criterion instance.
 */
public class UndertakerTotemCriterionInstance extends AbstractCriterionTriggerInstance
{
    public UndertakerTotemCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_UNDERTAKER_TOTEM), ContextAwarePredicate.ANY);
    }
}
