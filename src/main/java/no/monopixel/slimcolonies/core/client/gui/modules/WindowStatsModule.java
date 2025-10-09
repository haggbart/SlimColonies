package no.monopixel.slimcolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.DropDownList;
import com.ldtteam.blockui.views.ScrollingList;
import net.minecraft.network.chat.Component;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.colony.managers.interfaces.IStatisticsManager;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.client.gui.AbstractModuleWindow;
import no.monopixel.slimcolonies.core.colony.buildings.moduleviews.BuildingStatisticsModuleView;
import no.monopixel.slimcolonies.core.colony.buildings.moduleviews.MinerLevelManagementModuleView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.PARTIAL_STATS_MODIFIER_NAME;
import static no.monopixel.slimcolonies.api.util.constant.WindowConstants.DROPDOWN_INTERVAL_ID;

/**
 * BOWindow for the miner hut.
 */
public class WindowStatsModule extends AbstractModuleWindow
{
    /**
     * Map of intervals.
     */
    private static final LinkedHashMap<String, Integer> INTERVAL = new LinkedHashMap<>();

    static
    {
        INTERVAL.put("no.monopixel.slimcolonies.coremod.gui.interval.yesterday", 1);
        INTERVAL.put("no.monopixel.slimcolonies.coremod.gui.interval.lastweek", 7);
        INTERVAL.put("no.monopixel.slimcolonies.coremod.gui.interval.100days", 100);
        INTERVAL.put("no.monopixel.slimcolonies.coremod.gui.interval.alltime", -1);
    }

    /**
     * Drop down list for interval.
     */
    private DropDownList intervalDropdown;

    /**
     * Current selected interval.
     */
    public String selectedInterval = "no.monopixel.slimcolonies.coremod.gui.interval.yesterday";

    /**
     * Util tags.
     */
    private static final String HUT_RESOURCE_SUFFIX = ":gui/layouthuts/layoutstatsmodule.xml";

    /**
     * Module view
     */
    private BuildingStatisticsModuleView moduleView = null;

    /**
     * Constructor for the window of the miner hut.
     *
     * @param moduleView {@link MinerLevelManagementModuleView}.
     */
    public WindowStatsModule(final IBuildingView building, final BuildingStatisticsModuleView moduleView)
    {
        super(building, Constants.MOD_ID + HUT_RESOURCE_SUFFIX);
        this.moduleView = moduleView;
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        updateStats();
    }

    /**
     * Update the display for the stats.
     */
    private void updateStats()
    {
        final IStatisticsManager statisticsManager = moduleView.getBuildingStatisticsManager();
        final @NotNull List<String> stats = new ArrayList<>(statisticsManager.getStatTypes());
        findPaneOfTypeByID("stats", ScrollingList.class).setDataProvider(new ScrollingList.DataProvider()
        {

            private List<String> filteredStats = new ArrayList<>();
            {
                int interval = INTERVAL.get(selectedInterval);

                for (int i = 0; i < stats.size(); i++)
                {
                    if (interval > 0)
                    {
                        if (statisticsManager.getStatsInPeriod(stats.get(i), buildingView.getColony().getDay() - interval, buildingView.getColony().getDay()) > 0)
                        {
                            filteredStats.add(stats.get(i));
                        }
                    }
                    else
                    {
                        if (statisticsManager.getStatTotal(stats.get(i)) > 0)
                        {
                            filteredStats.add(stats.get(i));
                        }
                    }
                }
            }
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {


                return filteredStats.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                int stat = statisticsManager.getStatTotal(filteredStats.get(index));
                int interval = INTERVAL.get(selectedInterval);
                if (interval > 0)
                {
                    stat = statisticsManager.getStatsInPeriod(filteredStats.get(index), buildingView.getColony().getDay() - interval, buildingView.getColony().getDay());
                }

                final Text resourceLabel = rowPane.findPaneOfTypeByID("desc", Text.class);
                final String id = filteredStats.get(index);
                if (id.contains(";"))
                {
                    final String[] split = id.split(";");
                    if (id.contains("'"))
                    {
                        //todo remove in 1.20.4
                        final String[] split2 = split[1].split("'");
                        resourceLabel.setText(Component.translatable(PARTIAL_STATS_MODIFIER_NAME + split[0], stat, Component.translatable(split2[1])));
                    }
                    else
                    {
                        resourceLabel.setText(Component.translatable(PARTIAL_STATS_MODIFIER_NAME + split[0], stat, Component.translatable(split[1])));
                    }
                }
                else
                {
                    resourceLabel.setText(Component.translatable(PARTIAL_STATS_MODIFIER_NAME + id, stat));
                }
                PaneBuilders.tooltipBuilder().hoverPane(resourceLabel).build().setText(resourceLabel.getText());
            }
        });

        intervalDropdown = findPaneOfTypeByID(DROPDOWN_INTERVAL_ID, DropDownList.class);
        intervalDropdown.setHandler(this::onDropDownListChanged);

        intervalDropdown.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return INTERVAL.size();
            }

            @Override
            public String getLabel(final int index)
            {
                return Component.translatable((String) INTERVAL.keySet().toArray()[index]).getString();
            }
        });
        intervalDropdown.setSelectedIndex(new ArrayList<>(INTERVAL.keySet()).indexOf(selectedInterval));
    }

    private void onDropDownListChanged(final DropDownList dropDownList)
    {
        final String temp = (String) INTERVAL.keySet().toArray()[dropDownList.getSelectedIndex()];
        if (!temp.equals(selectedInterval))
        {
            selectedInterval = temp;
            updateStats();
        }
    }
}
