package com.minecolonies.core.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.core.entity.citizen.citizenhandlers.CitizenSkillHandler;
import com.minecolonies.core.network.messages.server.colony.citizen.RecallSingleCitizenMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the town hall.
 */
public class WindowCitizenPage extends AbstractWindowTownHall
{
    /**
     * Citizen name comparator.
     */
    private static final Comparator<ICitizenDataView> COMPARE_BY_NAME = Comparator.comparing(ICitizen::getName);

    /**
     * List of citizens.
     */
    @NotNull
    private final List<ICitizenDataView> citizens = new ArrayList<>();

    /**
     * The selected citizen.
     */
    private ICitizenDataView selectedCitizen;

    /**
     * The selected citizen.
     */
    private Entity selectedEntity;

    /**
     * The filter for the resource list.
     */
    private String filter = "";

    /**
     * Constructor for the town hall window.
     *
     * @param townHall {@link BuildingTownHall.View}.
     */
    public WindowCitizenPage(final BuildingTownHall.View townHall)
    {
        super(townHall, "layoutcitizens.xml");

        updateCitizens();
        fillCitizensList();
        if (!citizens.isEmpty())
        {
            selectedCitizen = citizens.get(0);
        }

        registerButton(NAME_LABEL, this::citizenSelected);
        registerButton(RECALL_ONE, this::recallOneClicked);
        fillCitizenInfo();
        // Happiness system removed

        window.findPaneOfTypeByID(SEARCH_INPUT, TextField.class).setHandler(input -> {
            final String newFilter = input.getText();
            if (!newFilter.equals(filter))
            {
                filter = newFilter;
                updateCitizens();
            }
        });
    }

    /**
     * Clears and resets all citizens.
     */
    private void updateCitizens()
    {
        citizens.clear();
        if (filter.isEmpty())
        {
            citizens.addAll(building.getColony().getCitizens().values());
        }
        else
        {
            citizens.addAll(building.getColony().getCitizens().values().stream().filter(cit -> cit.getName().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
                                                                                                 || cit.getJobComponent().getString().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))).toList());
        }
        citizens.sort(COMPARE_BY_NAME);
    }

    /**
     * On clicking a citizen name in the list.
     * @param button the clicked button.
     */
    private void citizenSelected(final Button button)
    {
        final ScrollingList citizenList = findPaneOfTypeByID(LIST_CITIZENS, ScrollingList.class);
        selectedCitizen = citizens.get(citizenList.getListElementIndexByPane(button));

        fillCitizenInfo();
    }

    /**
     * Setup citizen info in UI.
     */
    private void fillCitizenInfo()
    {
        if (selectedCitizen == null)
        {
            return;
        }

        if (selectedCitizen.isFemale())
        {
            findPaneOfTypeByID(WINDOW_ID_GENDER, Image.class).setImage(new ResourceLocation(FEMALE_SOURCE), false);
        }
        else
        {
            findPaneOfTypeByID(WINDOW_ID_GENDER, Image.class).setImage(new ResourceLocation(MALE_SOURCE), false);
        }

        findPaneOfTypeByID(JOB_LABEL, Text.class).setText(selectedCitizen.getJobComponent().withStyle(ChatFormatting.BOLD));

        findPaneOfTypeByID(HEALTH_SHORT_LABEL, Text.class).setText(Component.literal((int)selectedCitizen.getHealth() + "/" + (int) selectedCitizen.getMaxHealth()));
        // Happiness system removed
        findPaneOfTypeByID(SATURATION_SHORT_LABEL, Text.class).setText(Component.literal((int)selectedCitizen.getSaturation() + "/" + 20));

        selectedEntity = Minecraft.getInstance().level.getEntity(selectedCitizen.getEntityId());
        if (selectedEntity != null && selectedEntity.getPose() == Pose.SLEEPING)
        {
            final EntityIcon entityIcon = findPaneOfTypeByID(ENTITY_ICON, EntityIcon.class);
            entityIcon.setEntity(selectedEntity);
            entityIcon.show();
        }
    }

    /**
     * Executed when the recall one button has been clicked. Recalls one specific citizen.
     *
     * @param button the clicked button.
     */
    private void recallOneClicked(final Button button)
    {
        if (selectedCitizen == null)
        {
            return;
        }
        Network.getNetwork().sendToServer(new RecallSingleCitizenMessage(building, selectedCitizen.getId()));
    }

    /**
     * Fills the citizens list in the GUI.
     */
    private void fillCitizensList()
    {
        final ScrollingList citizenList = findPaneOfTypeByID(LIST_CITIZENS, ScrollingList.class);
        citizenList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return citizens.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ICitizenDataView citizen = citizens.get(index);
                final Button button = rowPane.findPaneOfTypeByID(NAME_LABEL, ButtonImage.class);
                button.setText(Component.literal(citizen.getName()));

                final AbstractTextBuilder.TextBuilder textBuilder = PaneBuilders.textBuilder();
                for (final Map.Entry<Skill, CitizenSkillHandler.SkillData> entry : citizen.getCitizenSkillHandler().getSkills().entrySet())
                {
                    final String skillName = entry.getKey().name().toLowerCase(Locale.US);
                    final int skillLevel = entry.getValue().getLevel();

                    textBuilder.append(Component.translatable("com.minecolonies.coremod.gui.citizen.skills." + skillName));
                    textBuilder.append(Component.literal(": " + skillLevel + " "));
                }
                PaneBuilders.tooltipBuilder().hoverPane(button).build().setText(textBuilder.build().getText());
                if (selectedCitizen == citizen)
                {
                    button.disable();
                }
                else
                {
                    button.enable();
                }
            }
        });
    }


    // Happiness system removed

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        updateCitizens();
        final EntityIcon entityIcon = findPaneOfTypeByID(ENTITY_ICON, EntityIcon.class);
        if (selectedEntity != null && selectedEntity.getPose() == Pose.SLEEPING)
        {
            entityIcon.hide();
        }
        else
        {
            entityIcon.show();
        }
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_CITIZENS;
    }
}
