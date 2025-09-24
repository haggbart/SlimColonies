package no.monopixel.slimcolonies.api.advancements.place_supply;

import no.monopixel.slimcolonies.api.advancements.CriterionListeners;
import net.minecraft.server.PlayerAdvancements;

/**
 * A default listener, as there are no conditions
 */
public class PlaceSupplyListeners extends CriterionListeners<PlaceSupplyCriterionInstance>
{
    public PlaceSupplyListeners(final PlayerAdvancements playerAdvancements)
    {
        super(playerAdvancements);
    }

    public void trigger()
    {
        trigger(instance -> true);
    }
}
