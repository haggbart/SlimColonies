package com.minecolonies.api.eventbus.events.colony.buildings;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.eventbus.events.colony.AbstractColonyEvent;

/**
 * Abstract event for building related things.
 */
public abstract class AbstractBuildingEvent extends AbstractColonyEvent
{
    /**
     * The building related to the event.
     */
    protected final IBuilding building;

    /**
     * Constructs a building-based event.
     *
     * @param building the building related to the event.
     */
    protected AbstractBuildingEvent(final IBuilding building)
    {
        super(building.getColony());
        this.building = building;
    }

    /**
     * Get the building related to the event.
     *
     * @return the building instance.
     */
    public IBuilding getBuilding()
    {
        return building;
    }
}
