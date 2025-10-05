package no.monopixel.slimcolonies.core.client.gui.huts;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import no.monopixel.slimcolonies.api.colony.ICitizenDataView;
import no.monopixel.slimcolonies.api.util.MessageUtils;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.client.gui.AbstractWindowModuleBuilding;
import no.monopixel.slimcolonies.core.client.gui.WindowAssignCitizen;
import no.monopixel.slimcolonies.core.colony.buildings.views.LivingBuildingView;
import no.monopixel.slimcolonies.core.network.messages.server.colony.building.RecallCitizenHutMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.COREMOD_GUI_WORKERHUTS_LEVEL_0;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.LABEL_HOUSE_ASSIGNED_CITIZENS;
import static no.monopixel.slimcolonies.api.util.constant.WindowConstants.BUTTON_RECALL;

/**
 * BOWindow for the tavern
 */
public class WindowHutLiving extends AbstractWindowModuleBuilding<LivingBuildingView>
{
    /**
     * Id of the hire/fire button in the GUI.
     */
    private static final String BUTTON_ASSIGN = "assign";

    /**
     * Label showing the assigned.
     */
    private static final String ASSIGNED_LABEL = "assignedlabel";

    /**
     * Suffix describing the window xml.
     */
    private static final String HOME_BUILDING_RESOURCE_SUFFIX = ":gui/windowhuthome.xml";

    /**
     * Id to identify the list of the citizen in the view.
     */
    private static final String LIST_CITIZEN = "assignedCitizen";

    /**
     * The building the view is relates to.
     */
    private final LivingBuildingView home;

    /**
     * The list of citizen assigned to this hut.
     */
    private ScrollingList citizen;

    /**
     * Creates the BOWindow object.
     *
     * @param building View of the home building.
     */
    public WindowHutLiving(final LivingBuildingView building)
    {
        super(building, Constants.MOD_ID + HOME_BUILDING_RESOURCE_SUFFIX);

        super.registerButton(BUTTON_ASSIGN, this::assignClicked);
        super.registerButton(BUTTON_RECALL, this::recallClicked);

        this.home = building;
    }

    /**
     * On recall clicked.
     */
    private void recallClicked()
    {
        Network.getNetwork().sendToServer(new RecallCitizenHutMessage(building));
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        citizen = findPaneOfTypeByID(LIST_CITIZEN, ScrollingList.class);
        citizen.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return home.getResidents().size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ICitizenDataView citizenDataView = home.getColony().getCitizen(home.getResidents().get(index));
                if (citizenDataView != null)
                {
                    rowPane.findPaneOfTypeByID("name", Text.class).setText(Component.literal((citizenDataView.getJob().isEmpty() ? "" : (Component.translatable(citizenDataView.getJob()).getString() + ": ")) + citizenDataView.getName()));
                }
            }
        });

        refreshView();
    }

    /**
     * Refresh the view.
     */
    private void refreshView()
    {
        findPaneOfTypeByID(ASSIGNED_LABEL, Text.class).setText(Component.translatable(LABEL_HOUSE_ASSIGNED_CITIZENS, building.getResidents().size(), building.getMax()));
        citizen.refreshElementPanes();
    }

    /**
     * Action when an assign button is clicked.
     */
    private void assignClicked()
    {
        if (building.getBuildingLevel() == 0)
        {
            MessageUtils.format(COREMOD_GUI_WORKERHUTS_LEVEL_0).sendTo(Minecraft.getInstance().player);
            return;
        }

        new WindowAssignCitizen(building.getColony(), building).open();
    }
}
