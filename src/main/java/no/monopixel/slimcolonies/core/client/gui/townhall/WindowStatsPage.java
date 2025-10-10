package no.monopixel.slimcolonies.core.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.DropDownList;
import com.ldtteam.blockui.views.ScrollingList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import no.monopixel.slimcolonies.api.SlimColoniesAPIProxy;
import no.monopixel.slimcolonies.api.colony.ICitizenDataView;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.util.Tuple;
import no.monopixel.slimcolonies.api.util.constant.CitizenConstants;
import no.monopixel.slimcolonies.core.SlimColonies;
import no.monopixel.slimcolonies.core.colony.buildings.moduleviews.CombinedHiringLimitModuleView;
import no.monopixel.slimcolonies.core.colony.buildings.moduleviews.WorkerBuildingModuleView;
import no.monopixel.slimcolonies.core.colony.buildings.views.AbstractBuildingView;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingTownHall;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static no.monopixel.slimcolonies.api.research.util.ResearchConstants.CITIZEN_CAP;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.*;
import static no.monopixel.slimcolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the town hall.
 */
public class WindowStatsPage extends AbstractWindowTownHall
{
    /**
     * Map of intervals.
     */
    public static final LinkedHashMap<String, Integer> INTERVAL = new LinkedHashMap<>();
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
     * Constructor for the town hall window.
     *
     * @param townHall {@link BuildingTownHall.View}.
     */
    public WindowStatsPage(final BuildingTownHall.View townHall)
    {
        super(townHall, "layoutstats.xml");
    }

    /**
     * Executed when <code>WindowTownHall</code> is opened. Does tasks like setting buttons.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();
        updateStats();
        createAndSetStatistics();
    }

    /**
     *
     * Creates several statistics and sets them in the building GUI.
     */
    private void createAndSetStatistics()
    {
        final int citizensSize = building.getColony().getCitizens().size();
        final int citizensCap;

        if (SlimColoniesAPIProxy.getInstance().getGlobalResearchTree().hasResearchEffect(CITIZEN_CAP))
        {
            final int max =
                Math.max(CitizenConstants.CITIZEN_LIMIT_DEFAULT, (int) this.building.getColony().getResearchManager().getResearchEffects().getEffectStrength(CITIZEN_CAP));
            citizensCap = Math.min(max, SlimColonies.getConfig().getServer().maxCitizenPerColony.get());
        }
        else
        {
            citizensCap = SlimColonies.getConfig().getServer().maxCitizenPerColony.get();
        }

        final Text totalCitizenLabel = findPaneOfTypeByID(TOTAL_CITIZENS_LABEL, Text.class);
        totalCitizenLabel.setText(Component.translatable(COREMOD_GUI_TOWNHALL_POPULATION_TOTALCITIZENS_COUNT,
            citizensSize,
            Math.max(citizensSize, building.getColony().getCitizenCountLimit())));
        List<MutableComponent> hoverText = new ArrayList<>();
        if (citizensSize < (citizensCap * 0.9) && citizensSize < (building.getColony().getCitizenCountLimit() * 0.9))
        {
            totalCitizenLabel.setColors(DARKGREEN);
        }
        else if (citizensSize < citizensCap)
        {
            hoverText.add(Component.translatable(WARNING_POPULATION_NEEDS_HOUSING, this.building.getColony().getName()));
            totalCitizenLabel.setColors(ORANGE);
        }
        else
        {
            if (citizensCap < SlimColonies.getConfig().getServer().maxCitizenPerColony.get())
            {
                hoverText.add(Component.translatable(WARNING_POPULATION_RESEARCH_LIMITED, this.building.getColony().getName()));
            }
            else
            {
                hoverText.add(Component.translatable(WARNING_POPULATION_CONFIG_LIMITED, this.building.getColony().getName()));
            }
            totalCitizenLabel.setText(Component.translatable(COREMOD_GUI_TOWNHALL_POPULATION_TOTALCITIZENS_COUNT, citizensSize, citizensCap));
            totalCitizenLabel.setColors(RED);
        }
        PaneBuilders.tooltipBuilder().hoverPane(totalCitizenLabel).build().setText(hoverText);

        int children = 0;
        final Map<String, Tuple<Integer, Integer>> jobMaxCountMap = new HashMap<>();
        for (@NotNull final IBuildingView building : building.getColony().getBuildings())
        {
            if (building instanceof AbstractBuildingView)
            {
                for (final WorkerBuildingModuleView module : building.getModuleViews(WorkerBuildingModuleView.class))
                {
                    int alreadyAssigned = -1;
                    if (module instanceof CombinedHiringLimitModuleView)
                    {
                        alreadyAssigned = 0;
                        for (final WorkerBuildingModuleView combinedModule : building.getModuleViews(WorkerBuildingModuleView.class))
                        {
                            alreadyAssigned += combinedModule.getAssignedCitizens().size();
                        }
                    }
                    int max = module.getMaxInhabitants();
                    if (alreadyAssigned != -1)
                    {
                        max -= alreadyAssigned;
                        max += module.getAssignedCitizens().size();
                    }
                    int workers = module.getAssignedCitizens().size();

                    final String jobName = module.getJobDisplayName().toLowerCase(Locale.US);

                    final Tuple<Integer, Integer> tuple = jobMaxCountMap.getOrDefault(jobName, new Tuple<>(0, 0));
                    jobMaxCountMap.put(jobName, new Tuple<>(tuple.getA() + workers, tuple.getB() + max));
                }
            }
        }

        //calculate number of children
        int unemployed = 0;
        for (ICitizenDataView iCitizenDataView : building.getColony().getCitizens().values())
        {
            if (iCitizenDataView.isChild())
            {
                children++;
            }
            else if (iCitizenDataView.getJobView() == null)
            {
                unemployed++;
            }
        }

        final int childCount = children;
        final int unemployedCount = unemployed;

        final ScrollingList list = findPaneOfTypeByID("citizen-stats", ScrollingList.class);
        if (list == null)
        {
            return;
        }

        final int maxJobs = jobMaxCountMap.size();
        final List<Map.Entry<String, Tuple<Integer, Integer>>> theList = new ArrayList<>(jobMaxCountMap.entrySet());
        theList.sort(Map.Entry.comparingByKey());

        list.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return maxJobs + 2;
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final Text label = rowPane.findPaneOfTypeByID(CITIZENS_AMOUNT_LABEL, Text.class);
                // preJobsHeaders = number of all unemployed citizens

                if (index < theList.size())
                {
                    final Map.Entry<String, Tuple<Integer, Integer>> entry = theList.get(index);
                    final String jobString = Component.translatable(entry.getKey()).getString();
                    final String formattedJobString = jobString.substring(0, 1).toUpperCase(Locale.US) + jobString.substring(1);

                    final Component numberOfWorkers =
                        Component.translatable(COREMOD_GUI_TOWNHALL_POPULATION_EACH, formattedJobString, entry.getValue().getA(), entry.getValue().getB());
                    label.setText(numberOfWorkers);
                }
                else
                {
                    if (index == maxJobs + 1)
                    {
                        label.setText(Component.translatable(COREMOD_GUI_TOWNHALL_POPULATION_UNEMPLOYED, unemployedCount));
                    }
                    else
                    {
                        label.setText(Component.translatable(COREMOD_GUI_TOWNHALL_POPULATION_CHILDS, childCount));
                    }
                }
            }
        });
    }

    /**
     * Update the display for the stats.
     */
    private void updateStats()
    {
        findPaneOfTypeByID("stats", ScrollingList.class).setDataProvider(new ScrollingList.DataProvider()
        {
            private final List<Map.Entry<String, Integer>> statsData;
            {
                final int interval = INTERVAL.get(selectedInterval);
                final Map<String, Integer> statsMap;

                if (interval > 0)
                {
                    statsMap = building.getColony().getStatisticsManager().getStats(
                        building.getColony().getDay() - interval,
                        building.getColony().getDay()
                    );
                }
                else
                {
                    statsMap = building.getColony().getStatisticsManager().getStats();
                }

                statsData = new ArrayList<>(statsMap.entrySet());
                statsData.sort(Map.Entry.comparingByKey());
            }

            @Override
            public int getElementCount()
            {
                return statsData.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final Map.Entry<String, Integer> entry = statsData.get(index);
                final String id = entry.getKey();
                final int stat = entry.getValue();

                final Text resourceLabel = rowPane.findPaneOfTypeByID("desc", Text.class);
                resourceLabel.setText(Component.translatable(PARTIAL_STATS_MODIFIER_NAME + id, stat));
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

    @Override
    protected String getWindowId()
    {
        return BUTTON_STATS;
    }
}
