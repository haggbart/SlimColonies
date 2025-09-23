package com.minecolonies.core.client.gui.huts;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.AbstractWindowModuleBuilding;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBarracks;

/**
 * BOWindow for the barracks building.
 */
public class WindowBarracksBuilding extends AbstractWindowModuleBuilding<BuildingBarracks.View>
{

    /**
     * Suffix for the window.
     */
    private static final String HOME_BUILDING_RESOURCE_SUFFIX = ":gui/windowhutbarracks.xml";

    /**
     * Creates the BOWindow object.
     *
     * @param building View of the home building.
     */
    public WindowBarracksBuilding(final BuildingBarracks.View building)
    {
        super(building, Constants.MOD_ID + HOME_BUILDING_RESOURCE_SUFFIX);
    }

}
