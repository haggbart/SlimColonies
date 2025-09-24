package no.monopixel.slimcolonies.api.eventbus.events.colony.buildings;

import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.core.colony.workorders.WorkOrderBuilding;

/**
 * Event for when a building was built, upgraded, repaired or removed.
 */
public final class BuildingConstructionModEvent extends AbstractBuildingModEvent
{
    /**
     * The work order which was completed.
     */
    private final WorkOrderBuilding workOrder;

    /**
     * Building construction event.
     *
     * @param building  the building the event was for.
     * @param workOrder the work order which was completed.
     */
    public BuildingConstructionModEvent(final IBuilding building, final WorkOrderBuilding workOrder)
    {
        super(building);
        this.workOrder = workOrder;
    }

    /**
     * Get the work order which was completed.
     *
     * @return the event type.
     */
    public WorkOrderBuilding getWorkOrder()
    {
        return workOrder;
    }
}
