package no.monopixel.slimcolonies.core.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.DropDownList;
import com.ldtteam.structurize.client.gui.WindowSwitchPack;
import com.ldtteam.structurize.storage.StructurePacks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.client.gui.WindowBannerPicker;
import no.monopixel.slimcolonies.core.client.gui.map.WindowColonyMap;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingTownHall;
import no.monopixel.slimcolonies.core.network.messages.server.colony.ColonyNameStyleMessage;
import no.monopixel.slimcolonies.core.network.messages.server.colony.ColonyStructureStyleMessage;
import no.monopixel.slimcolonies.core.network.messages.server.colony.ColonyTextureStyleMessage;
import no.monopixel.slimcolonies.core.network.messages.server.colony.TeamColonyColorChangeMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static no.monopixel.slimcolonies.api.util.constant.WindowConstants.*;
import static no.monopixel.slimcolonies.core.event.TextureReloadListener.TEXTURE_PACKS;

/**
 * BOWindow for the town hall.
 */
public class WindowMainPage extends AbstractWindowTownHall
{

    /**
     * Drop down list for style.
     */
    private DropDownList colorDropDownList;

    /**
     * Drop down list for style.
     */
    private DropDownList textureDropDownList;

    /**
     * Drop down list for name style.
     */
    private DropDownList nameStyleDropDownList;

    /**
     * The initial texture index.
     */
    private int initialTextureIndex;

    /**
     * The initial texture index.
     */
    private int initialNamePackIndex;

    /**
     * Label for the colony name.
     */
    private final Text title;

    /**
     * Constructor for the town hall window.
     *
     * @param building {@link BuildingTownHall.View}.
     */
    public WindowMainPage(final BuildingTownHall.View building)
    {
        super(building, "layoutactions.xml");
        initDropDowns();

        title = findPaneOfTypeByID(LABEL_BUILDING_NAME, Text.class);
        findPaneOfTypeByID("actions1", Button.class).setText(Component.translatable(building.getBuildingDisplayName())
            .append(Component.literal(" " + building.getBuildingLevel())));

        registerButton(BUTTON_CHANGE_SPEC, this::doNothing);
        registerButton(BUTTON_RENAME, this::renameClicked);
        registerButton(BUTTON_TOWNHALLMAP, this::mapButtonClicked);

        registerButton(BUTTON_COLONY_SWITCH_STYLE, this::switchPack);

        findPaneOfTypeByID(BUTTON_COLONY_SWITCH_STYLE, ButtonImage.class).setText(Component.literal(building.getColony().getStructurePack()));
        registerButton(BUTTON_BANNER_PICKER, this::openBannerPicker);
        registerButton(BUTTON_RESET_TEXTURE, this::resetTextureStyle);

        this.colorDropDownList.setSelectedIndex(building.getColony().getTeamColonyColor().ordinal());
        this.textureDropDownList.setSelectedIndex(TEXTURE_PACKS.indexOf(building.getColony().getTextureStyleId()));
        this.initialTextureIndex = textureDropDownList.getSelectedIndex();

        this.nameStyleDropDownList.setSelectedIndex(building.getColony().getNameFileIds().indexOf(building.getColony().getNameStyle()));
        this.initialNamePackIndex = nameStyleDropDownList.getSelectedIndex();
    }

    /**
     * Switch the structure style pack.
     */
    private void switchPack()
    {
        new WindowSwitchPack(() -> {
            building.getColony().setStructurePack(StructurePacks.selectedPack.getName());
            Network.getNetwork().sendToServer(new ColonyStructureStyleMessage(building.getColony(), StructurePacks.selectedPack.getName()));
            return new WindowMainPage((BuildingTownHall.View) this.building);
        }).open();
    }

    /**
     * Initialise the previous/next and drop down list for style.
     */
    private void initDropDowns()
    {
        findPaneOfTypeByID(DROPDOWN_COLOR_ID, DropDownList.class).setEnabled(enabled);

        colorDropDownList = findPaneOfTypeByID(DROPDOWN_COLOR_ID, DropDownList.class);
        colorDropDownList.setHandler(this::onDropDownListChanged);

        final List<ChatFormatting> textColors = Arrays.stream(ChatFormatting.values()).filter(ChatFormatting::isColor).toList();

        colorDropDownList.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return textColors.size();
            }

            @Override
            public String getLabel(final int index)
            {
                if (index >= 0 && index < textColors.size())
                {
                    final String colorName = textColors.get(index).getName().replace("_", " ");
                    return colorName.substring(0, 1).toUpperCase(Locale.US) + colorName.substring(1);
                }
                return "";
            }
        });

        textureDropDownList = findPaneOfTypeByID(DROPDOWN_TEXT_ID, DropDownList.class);
        textureDropDownList.setHandler(this::toggleTexture);
        textureDropDownList.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return TEXTURE_PACKS.size();
            }

            @Override
            public String getLabel(final int index)
            {
                return TEXTURE_PACKS.get(index);
            }
        });

        nameStyleDropDownList = findPaneOfTypeByID(DROPDOWN_NAME_ID, DropDownList.class);
        nameStyleDropDownList.setHandler(this::toggleNameFile);
        nameStyleDropDownList.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return building.getColony().getNameFileIds().size();
            }

            @Override
            public String getLabel(final int index)
            {
                return building.getColony().getNameFileIds().get(index);
            }
        });
    }

    /**
     * Toggle the dropdownlist with the selected index to change the texture of the colonists.
     *
     * @param dropDownList the toggle dropdown list.
     */
    private void toggleTexture(final DropDownList dropDownList)
    {
        if (dropDownList.getSelectedIndex() != initialTextureIndex)
        {
            Network.getNetwork().sendToServer(new ColonyTextureStyleMessage(building.getColony(), TEXTURE_PACKS.get(dropDownList.getSelectedIndex())));
        }
    }

    /**
     * Toggle the dropdownlist with the selected index to change the texture of the colonists.
     *
     * @param dropDownList the toggle dropdown list.
     */
    private void toggleNameFile(final DropDownList dropDownList)
    {
        if (dropDownList.getSelectedIndex() != initialNamePackIndex)
        {
            Network.getNetwork().sendToServer(new ColonyNameStyleMessage(building.getColony(), building.getColony().getNameFileIds().get(dropDownList.getSelectedIndex())));
        }
    }

    /**
     * Called when the dropdownList changed.
     *
     * @param dropDownList the list.
     */
    private void onDropDownListChanged(final DropDownList dropDownList)
    {
        Network.getNetwork().sendToServer(new TeamColonyColorChangeMessage(dropDownList.getSelectedIndex(), building));
    }

    /**
     * Opens the banner picker window. BOWindow does not use blockui, so is started manually.
     *
     * @param button the trigger button
     */
    private void openBannerPicker(@NotNull final Button button)
    {
        Screen window = new WindowBannerPicker(building.getColony(), this, new AtomicBoolean(true));
        Minecraft.getInstance().setScreen(window);
    }

    /**
     * Reset the texture style.
     */
    private void resetTextureStyle()
    {
        Network.getNetwork().sendToServer(new ColonyTextureStyleMessage(building.getColony(), TEXTURE_PACKS.get(0)));
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        final Pane textPane = findPaneByID(DROPDOWN_TEXT_ID);
        final Pane namePane = findPaneByID(DROPDOWN_NAME_ID);
        final Pane resetButton = findPaneByID(BUTTON_RESET_TEXTURE);
        final boolean isOwner = building.getColony().getPermissions().getOwner().equals(Minecraft.getInstance().player.getUUID());
        if (isOwner)
        {
            textPane.enable();
            namePane.enable();
            textPane.show();

            if (!building.getColony().getTextureStyleId().equals("default"))
            {
                resetButton.show();
            }
            else
            {
                resetButton.hide();
            }
        }
        else
        {
            textPane.disable();
            namePane.disable();
            textPane.show();
            resetButton.hide();
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        title.setText(Component.literal(building.getColony().getName()));
    }

    /**
     * Action performed when rename button is clicked.
     */
    private void renameClicked()
    {
        new WindowTownHallNameEntry(building.getColony()).open();
    }

    /**
     * Opens the map on button clicked
     */
    private void mapButtonClicked()
    {
        new WindowColonyMap(true, building).open();
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_ACTIONS;
    }
}
