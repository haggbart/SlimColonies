package no.monopixel.slimcolonies.api.advancements.place_structure;

import no.monopixel.slimcolonies.api.advancements.CriterionListeners;
import net.minecraft.server.PlayerAdvancements;

/**
 * The listener instantiated for every advancement that listens to the associated criterion.
 * A basic class to trigger with the correct arguments
 */
public class PlaceStructureListeners extends CriterionListeners<PlaceStructureCriterionInstance>
{
    public PlaceStructureListeners(final PlayerAdvancements playerAdvancements)
    {
        super(playerAdvancements);
    }

    public void trigger(final String structureName)
    {
        trigger(instance -> instance.test(structureName));
    }
}
