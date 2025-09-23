package com.minecolonies.core.client.gui.huts;

import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.ItemIcon;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.AbstractWindowModuleBuilding;
import com.minecolonies.core.client.gui.WindowsBarracksSpies;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBarracks;
import net.minecraft.world.item.Items;

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
     * Spies button id.
     */
    private static final String SPIES_BUTTON = "hireSpies";

    /**
     * The spies button icon id
     */
    private static final String SPIES_BUTTON_ICON = "hireSpiesIcon";

    /**
     * Creates the BOWindow object.
     *
     * @param building View of the home building.
     */
    public WindowBarracksBuilding(final BuildingBarracks.View building)
    {
        super(building, Constants.MOD_ID + HOME_BUILDING_RESOURCE_SUFFIX);
        findPaneOfTypeByID(SPIES_BUTTON_ICON, ItemIcon.class).setItem(Items.GOLD_INGOT.getDefaultInstance());
        registerButton(SPIES_BUTTON, this::hireSpiesClicked);

        if (building.getBuildingLevel() < 3)
        {
            findPaneOfTypeByID(SPIES_BUTTON, ButtonImage.class).setVisible(false);
            findPaneOfTypeByID(SPIES_BUTTON_ICON, ItemIcon.class).setVisible(false);
        }
    }

    /**
     * Open the spies gui when the button is clicked.
     *
     * @param button the clicked button.
     */
    private void hireSpiesClicked(final Button button)
    {
        new WindowsBarracksSpies(this.building, this.building.getID()).open();
    }
}
