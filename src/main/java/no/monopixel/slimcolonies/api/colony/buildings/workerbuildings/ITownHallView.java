package no.monopixel.slimcolonies.api.colony.buildings.workerbuildings;

import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.colony.colonyEvents.descriptions.IColonyEventDescription;
import no.monopixel.slimcolonies.api.colony.permissions.PermissionEvent;

import java.util.List;

public interface ITownHallView extends IBuildingView
{
    /**
     * Get a list of permission events.
     *
     * @return a copy of the list of events.
     */
    List<PermissionEvent> getPermissionEvents();

    /**
     * Gets a list if colony events.
     *
     * @return a copy of the list of events.
     */
    List<IColonyEventDescription> getColonyEvents();
}
