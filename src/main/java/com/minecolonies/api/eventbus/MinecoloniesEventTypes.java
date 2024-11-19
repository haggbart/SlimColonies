package com.minecolonies.api.eventbus;

import com.minecolonies.api.eventbus.events.ColonyManagerLoadedEvent;
import com.minecolonies.api.eventbus.events.ColonyManagerUnloadedEvent;
import com.minecolonies.api.eventbus.events.colony.*;
import com.minecolonies.api.eventbus.events.colony.buildings.BuildingConstructionEvent;
import com.minecolonies.api.eventbus.events.colony.citizens.CitizenAddedEvent;
import com.minecolonies.api.eventbus.events.colony.citizens.CitizenDiedEvent;
import com.minecolonies.api.eventbus.events.colony.citizens.CitizenRemovedEvent;

/**
 * All possible minecolonies mod bus events.
 */
public final class MinecoloniesEventTypes
{
    // Colony manager events
    public static final IModEventType<ColonyManagerLoadedEvent>   COLONY_MANAGER_LOADED   = new MinecoloniesEventType<>(ColonyManagerLoadedEvent.class);
    public static final IModEventType<ColonyManagerUnloadedEvent> COLONY_MANAGER_UNLOADED = new MinecoloniesEventType<>(ColonyManagerUnloadedEvent.class);

    // Colony events
    public static final IModEventType<ColonyCreatedEvent>          COLONY_CREATED            = new MinecoloniesEventType<>(ColonyCreatedEvent.class);
    public static final IModEventType<ColonyDeletedEvent>          COLONY_DELETED            = new MinecoloniesEventType<>(ColonyDeletedEvent.class);
    public static final IModEventType<ColonyNameChangedEvent>      COLONY_NAME_CHANGED       = new MinecoloniesEventType<>(ColonyNameChangedEvent.class);
    public static final IModEventType<ColonyTeamColorChangedEvent> COLONY_TEAM_COLOR_CHANGED = new MinecoloniesEventType<>(ColonyTeamColorChangedEvent.class);
    public static final IModEventType<ColonyFlagChangedEvent>      COLONY_FLAG_CHANGED       = new MinecoloniesEventType<>(ColonyFlagChangedEvent.class);
    public static final IModEventType<ColonyViewUpdatedEvent>      COLONY_VIEW_UPDATED       = new MinecoloniesEventType<>(ColonyViewUpdatedEvent.class);

    // Colony building events
    public static final IModEventType<BuildingConstructionEvent> BUILDING_COMPLETED = new MinecoloniesEventType<>(BuildingConstructionEvent.class);

    // Colony citizen events
    public static final IModEventType<CitizenAddedEvent>   CITIZEN_ADDED   = new MinecoloniesEventType<>(CitizenAddedEvent.class);
    public static final IModEventType<CitizenDiedEvent>    CITIZEN_DIED    = new MinecoloniesEventType<>(CitizenDiedEvent.class);
    public static final IModEventType<CitizenRemovedEvent> CITIZEN_REMOVED = new MinecoloniesEventType<>(CitizenRemovedEvent.class);

    // Other events
}
