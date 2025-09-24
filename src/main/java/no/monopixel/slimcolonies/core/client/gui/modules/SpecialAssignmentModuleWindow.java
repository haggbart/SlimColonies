package no.monopixel.slimcolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import no.monopixel.slimcolonies.api.colony.ICitizenDataView;
import no.monopixel.slimcolonies.api.colony.buildings.modules.IAssignmentModuleView;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.util.MessageUtils;
import no.monopixel.slimcolonies.api.util.Tuple;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.client.gui.AbstractModuleWindow;
import no.monopixel.slimcolonies.core.client.gui.WindowHireWorker;
import no.monopixel.slimcolonies.core.colony.buildings.views.AbstractBuildingView;
import no.monopixel.slimcolonies.core.network.messages.server.colony.building.worker.RecallCitizenMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_LEVEL_0;
import static no.monopixel.slimcolonies.api.util.constant.WindowConstants.BUTTON_RECALL;

/**
 * Assignment module for workers to a building.
 * This is specifically for the assignment of workers that got their own hut and are assigned additionally to this building (e.g., warehouse, quarry).
 */
public class SpecialAssignmentModuleWindow extends AbstractModuleWindow
{
    /**
     * Id of the hire/fire button in the GUI.
     */
    private static final String BUTTON_HIRE = "hire";

    /**
     * Id of the scroll view
     */
    private static final String LIST_WORKERS = "workers";

    /**
     * Id of the name label in the GUI.
     */
    private static final String LABEL_WORKERNAME = "workerName";

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending {@link AbstractBuildingView}.
     * @param resource Resource of the window.
     */
    public SpecialAssignmentModuleWindow(final IBuildingView building, final String resource)
    {
        super(building, resource);
        super.registerButton(BUTTON_HIRE, this::hireClicked);
        super.registerButton(BUTTON_RECALL, this::recallClicked);
    }

    /**
     * On recall clicked.
     */
    private void recallClicked()
    {
        Network.getNetwork().sendToServer(new RecallCitizenMessage(buildingView));
    }

    /**
     * Action when a hire button is clicked. If there is no worker (worker.Id == 0) then Contract someone. Else then Fire the current worker.
     *
     * @param button the clicked button.
     */
    protected void hireClicked(@NotNull final Button button)
    {
        if (buildingView.getBuildingLevel() == 0)
        {
            MessageUtils.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_LEVEL_0).sendTo(Minecraft.getInstance().player);
            return;
        }

        new WindowHireWorker(buildingView.getColony(), buildingView.getPosition()).open();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        final List<Tuple<String, Integer>> workers = new ArrayList<>();

        for (final IAssignmentModuleView module : buildingView.getModuleViews(IAssignmentModuleView.class))
        {
            for (final int worker : module.getAssignedCitizens())
            {
                workers.add(new Tuple<>(Component.translatable(module.getJobEntry().getTranslationKey()).getString(), worker));
            }
        }

        if (findPaneByID(LIST_WORKERS) != null)
        {
            ScrollingList workerList = findPaneOfTypeByID(LIST_WORKERS, ScrollingList.class);
            workerList.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return workers.size();
                }

                @Override
                public void updateElement(final int index, @NotNull final Pane rowPane)
                {

                    final ICitizenDataView worker = buildingView.getColony().getCitizen(workers.get(index).getB());
                    if (worker != null)
                    {
                        rowPane.findPaneOfTypeByID(LABEL_WORKERNAME, Text.class)
                          .setText(Component.literal(Component.translatable(workers.get(index).getA()).getString() + ": " + worker.getName()));
                    }
                }
            });
        }
    }
}
