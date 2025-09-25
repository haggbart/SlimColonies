package no.monopixel.slimcolonies.core.colony.workorders.view;

import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.colony.buildings.workerbuildings.ITownHallView;
import no.monopixel.slimcolonies.api.util.constant.TranslationConstants;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingBuilder;
import net.minecraft.network.chat.Component;

/**
 * The client side representation for a work order that the builder can take to build plantation fields.
 */
public class WorkOrderPlantationFieldView extends AbstractWorkOrderView
{
    @Override
    public Component getDisplayName()
    {
        return getOrderTypePrefix(Component.translatable(getTranslationKey()));
    }

    private Component getOrderTypePrefix(Component nameComponent)
    {
        return switch (this.getWorkOrderType())
        {
            case BUILD -> Component.translatable(TranslationConstants.BUILDER_ACTION_BUILDING, nameComponent);
            case REPAIR -> Component.translatable(TranslationConstants.BUILDER_ACTION_REPAIRING, nameComponent);
            case REMOVE -> Component.translatable(TranslationConstants.BUILDER_ACTION_REMOVING, nameComponent);
            default -> nameComponent;
        };
    }

    @Override
    public boolean shouldShowIn(IBuildingView view)
    {
        return view instanceof ITownHallView || view instanceof BuildingBuilder.View;
    }
}
