package no.monopixel.slimcolonies.core.client.gui.huts;

import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.client.gui.AbstractWindowModuleBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingBarracks;

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
