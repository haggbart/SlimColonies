package no.monopixel.slimcolonies.core.client.gui.townhall;

import com.ldtteam.blockui.Color;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.Image;
import no.monopixel.slimcolonies.api.colony.buildings.workerbuildings.ITownHallView;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.client.gui.AbstractWindowModuleBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingTownHall;

import static no.monopixel.slimcolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the town hall.
 */
public abstract class AbstractWindowTownHall extends AbstractWindowModuleBuilding<ITownHallView>
{
    /**
     * Color constants for builder list.
     */
    public static final int RED       = Color.getByName("red", 0);
    public static final int DARKGREEN = Color.getByName("darkgreen", 0);
    public static final int ORANGE    = Color.getByName("orange", 0);

    /**
     * Constructor for the town hall window.
     *
     * @param townHall {@link BuildingTownHall.View}.
     */
    public AbstractWindowTownHall(final BuildingTownHall.View townHall, final String page)
    {
        super(townHall, Constants.MOD_ID + ":gui/townhall/" + page);

        registerButton(BUTTON_ACTIONS, () -> new WindowMainPage(townHall).open());
        registerButton(BUTTON_INFOPAGE, () -> new WindowInfoPage(townHall).open());
        registerButton(BUTTON_PERMISSIONS, () -> new WindowPermissionsPage(townHall).open());
        registerButton(BUTTON_CITIZENS, () -> new WindowCitizenPage(townHall).open());
        registerButton(BUTTON_STATS, () -> new WindowStatsPage(townHall).open());
        registerButton(BUTTON_SETTINGS, () -> new WindowSettings(townHall).open());

        findPaneOfTypeByID(getWindowId() + "0", Image.class).hide();
        findPaneOfTypeByID(getWindowId(), ButtonImage.class).hide();

        findPaneOfTypeByID(getWindowId() + "1", ButtonImage.class).show();
    }

    /**
     * Get the id that identifies the window.
     * @return the string id.
     */
    protected abstract String getWindowId();

    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    @Override
    public String getBuildingName()
    {
        return building.getColony().getName();
    }
}
