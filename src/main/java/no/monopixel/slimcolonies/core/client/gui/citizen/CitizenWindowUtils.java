package no.monopixel.slimcolonies.core.client.gui.citizen;

import com.ldtteam.blockui.Alignment;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.View;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.ICitizenDataView;
import no.monopixel.slimcolonies.api.colony.IColonyView;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.entity.citizen.Skill;
import no.monopixel.slimcolonies.core.client.gui.AbstractWindowSkeleton;
import no.monopixel.slimcolonies.core.colony.buildings.moduleviews.WorkerBuildingModuleView;
import no.monopixel.slimcolonies.core.colony.buildings.views.AbstractBuildingView;
import no.monopixel.slimcolonies.core.entity.citizen.citizenhandlers.CitizenSkillHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.*;
import static no.monopixel.slimcolonies.api.util.constant.WindowConstants.*;
import static no.monopixel.slimcolonies.core.entity.citizen.citizenhandlers.CitizenExperienceHandler.PRIMARY_DEPENDENCY_SHARE;
import static no.monopixel.slimcolonies.core.entity.citizen.citizenhandlers.CitizenExperienceHandler.SECONDARY_DEPENDENCY_SHARE;
import static net.minecraft.client.gui.Gui.GUI_ICONS_LOCATION;

/**
 * BOWindow for the citizen.
 */
public class CitizenWindowUtils
{
    // Happiness system removed

    /**
     * Private con to hide public.
     */
    private CitizenWindowUtils()
    {
        // Intentionally left empty.
    }

    // Happiness system removed - SmileyEnum deleted

    /**
     * Enum for the available hearts
     */
    private enum HeartsEnum
    {
        EMPTY(GUI_ICONS_LOCATION, EMPTY_HEART_ICON_X, HEART_ICON_MC_Y, EMPTY_HEART_VALUE, null, null),
        HALF_RED(GUI_ICONS_LOCATION, HALF_RED_HEART_ICON_X, HEART_ICON_MC_Y, RED_HEART_VALUE - 1, null, EMPTY),
        RED(GUI_ICONS_LOCATION, RED_HEART_ICON_X, HEART_ICON_MC_Y, RED_HEART_VALUE, HALF_RED, EMPTY),
        HALF_GOLDEN(GUI_ICONS_LOCATION, HALF_GOLD_HEART_ICON_X, HEART_ICON_MC_Y, GOLDEN_HEART_VALUE - 1, null, RED),
        GOLDEN(GUI_ICONS_LOCATION, GOLD_HEART_ICON_X, HEART_ICON_MC_Y, GOLDEN_HEART_VALUE, HALF_GOLDEN, RED),
        HALF_GREEN(GREEN_BLUE_ICON, GREEN_HALF_HEART_ICON_X, GREEN_HEARTS_ICON_Y, GREEN_HEART_VALUE - 1, null, GOLDEN),
        GREEN(GREEN_BLUE_ICON, GREEN_HEART_ICON_X, GREEN_HEARTS_ICON_Y, GREEN_HEART_VALUE, HALF_GREEN, GOLDEN),
        HALF_BLUE(GREEN_BLUE_ICON, BLUE_HALF_HEART_ICON_X, BLUE_HEARTS_ICON_Y, BLUE_HEART_VALUE - 1, null, GREEN),
        BLUE(GREEN_BLUE_ICON, BLUE_HEART_ICON_X, BLUE_HEARTS_ICON_Y, BLUE_HEART_VALUE, HALF_BLUE, GREEN);

        public final int              X;
        public final int              Y;
        public final int              hpValue;
        public final HeartsEnum       prevHeart;
        public final HeartsEnum       halfHeart;
        public       boolean          isHalfHeart = false;
        public final ResourceLocation Image;

        HeartsEnum(
          final ResourceLocation heartImage, final int x, final int y, final int hpValue,
          final HeartsEnum halfHeart, final HeartsEnum prevHeart)
        {
            this.Image = heartImage;
            this.X = x;
            this.Y = y;
            this.hpValue = hpValue;
            this.halfHeart = halfHeart;
            if (halfHeart == null)
            {
                isHalfHeart = true;
            }
            this.prevHeart = prevHeart;
        }
    }

    /**
     * Creates an health bar according to the citizen maxHealth and currentHealth.
     *
     * @param citizen       the citizen.
     * @param healthBarView the health bar view.
     */
    public static void createHealthBar(final ICitizenDataView citizen, final View healthBarView)
    {
        int health = (int) citizen.getHealth();
        createHealthBar(health, healthBarView);
    }

    /**
     * Creates an health bar according to the citizen maxHealth and currentHealth.
     *
     * @param health        the health amount.
     * @param healthBarView the health bar view.
     */
    public static void createHealthBar(int health, final View healthBarView)
    {
        healthBarView.setAlignment(Alignment.MIDDLE_RIGHT);
        healthBarView.findPaneOfTypeByID(WINDOW_ID_HEALTHLABEL, Text.class).setText(Component.literal(Integer.toString(health / 2)));

        // Add Empty heart background
        for (int i = 0; i < MAX_HEART_ICONS; i++)
        {
            addHeart(healthBarView, i, HeartsEnum.EMPTY);
        }

        // Current Heart we're filling
        int heartPos = 0;

        // Order we're filling the hearts with from high to low
        final List<HeartsEnum> heartList = new ArrayList<>();
        heartList.add(HeartsEnum.BLUE);
        heartList.add(HeartsEnum.GREEN);
        heartList.add(HeartsEnum.GOLDEN);
        heartList.add(HeartsEnum.RED);

        // Iterate through hearts
        for (final HeartsEnum heart : heartList)
        {
            if (heart.isHalfHeart || heart.prevHeart == null)
            {
                continue;
            }

            // Add full hearts
            for (int i = heartPos; i < MAX_HEART_ICONS && health > (heart.prevHeart.hpValue * MAX_HEART_ICONS + 1); i++)
            {
                addHeart(healthBarView, heartPos, heart);
                health -= (heart.hpValue - heart.prevHeart.hpValue);
                heartPos++;
            }

            // Add half heart
            if (health % 2 == 1 && heartPos < MAX_HEART_ICONS && heart.halfHeart != null && health > heart.prevHeart.hpValue * MAX_HEART_ICONS)
            {
                addHeart(healthBarView, heartPos, heart.prevHeart);
                addHeart(healthBarView, heartPos, heart.halfHeart);

                health -= (heart.halfHeart.hpValue - heart.prevHeart.hpValue);
                heartPos++;
            }
            // Finished
            if (heartPos >= MAX_HEART_ICONS)
            {
                return;
            }
        }
    }

    /**
     * Adds a heart to the healthbarView at the given Position
     *
     * @param healthBarView the health bar to add the heart to.
     * @param heartPos      the number of the heart to add.
     * @param heart         the heart to add.
     */
    private static void addHeart(final View healthBarView, final int heartPos, final HeartsEnum heart)
    {
        @NotNull final Image heartImage = new Image();
        heartImage.setImage(heart.Image, heart.X, heart.Y, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH);
        heartImage.setMapDimensions(256, 256);
        heartImage.setSize(HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH);
        heartImage.setPosition(heartPos * HEART_ICON_POS_X + HEART_ICON_OFFSET_X, HEART_ICON_POS_Y);
        healthBarView.addChild(heartImage);
    }

    public static void createSaturationBar(final ICitizenDataView citizen, final View view)
    {
        createSaturationBar(citizen.getSaturation(), view);
    }

    public static void createSaturationBar(final double curSaturation, final View view)
    {
        view.findPaneOfTypeByID(WINDOW_ID_SATURATION_BAR, View.class).setAlignment(Alignment.MIDDLE_RIGHT);

        final int maxIcons = ICitizenData.MAX_SATURATION / 2;

        for (int i = 0; i < maxIcons; i++)
        {
            @NotNull final Image saturation = new Image();
            saturation.setImage(GUI_ICONS_LOCATION,
              EMPTY_SATURATION_ITEM_ROW_POS,
              SATURATION_ICON_COLUMN, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH);
            saturation.setMapDimensions(256, 256);
            saturation.setSize(SATURATION_ICON_HEIGHT_WIDTH, SATURATION_ICON_HEIGHT_WIDTH);
            saturation.setPosition(i * SATURATION_ICON_POS_X + SATURATION_ICON_OFFSET_X, SATURATION_ICON_POS_Y);
            view.findPaneOfTypeByID(WINDOW_ID_SATURATION_BAR, View.class).addChild(saturation);
        }

        final int fullIcons = (int) (curSaturation / 2);
        for (int i = 0; i < fullIcons; i++)
        {
            @NotNull final Image saturation = new Image();
            saturation.setImage(GUI_ICONS_LOCATION, FULL_SATURATION_ITEM_ROW_POS, SATURATION_ICON_COLUMN, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH);
            saturation.setMapDimensions(256, 256);
            saturation.setSize(SATURATION_ICON_HEIGHT_WIDTH, SATURATION_ICON_HEIGHT_WIDTH);
            saturation.setPosition(i * SATURATION_ICON_POS_X + SATURATION_ICON_OFFSET_X, SATURATION_ICON_POS_Y);
            view.findPaneOfTypeByID(WINDOW_ID_SATURATION_BAR, View.class).addChild(saturation);
        }

        if (curSaturation % 2 >= 1)
        {
            @NotNull final Image saturation = new Image();
            saturation.setImage(GUI_ICONS_LOCATION, HALF_SATURATION_ITEM_ROW_POS, SATURATION_ICON_COLUMN, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH);
            saturation.setMapDimensions(256, 256);
            saturation.setSize(SATURATION_ICON_HEIGHT_WIDTH, SATURATION_ICON_HEIGHT_WIDTH);
            saturation.setPosition(fullIcons * SATURATION_ICON_POS_X + SATURATION_ICON_OFFSET_X, SATURATION_ICON_POS_Y);
            view.findPaneOfTypeByID(WINDOW_ID_SATURATION_BAR, View.class).addChild(saturation);
        }
    }

    public static void createSkillContent(final ICitizenDataView citizen, final AbstractWindowSkeleton window)
    {
        final boolean isCreative = Minecraft.getInstance().player.isCreative();
        for (final Map.Entry<Skill, CitizenSkillHandler.SkillData> entry : citizen.getCitizenSkillHandler().getSkills().entrySet())
        {
            final String id = entry.getKey().name().toLowerCase(Locale.US);
            window.findPaneOfTypeByID(id, Text.class).setText(Component.literal(Integer.toString(entry.getValue().getLevel())));

            final Pane buttons = window.findPaneByID(id + "_bts");
            if (buttons != null)
            {
                buttons.setEnabled(isCreative);
            }
        }
    }

    // Happiness system removed

    /**
     * Update the job page of the citizen.
     *
     * @param citizen       the citizen.
     * @param windowCitizen the window.
     * @param colony        the colony.
     */
    public static void updateJobPage(final ICitizenDataView citizen, final JobWindowCitizen windowCitizen, final IColonyView colony)
    {
        final IBuildingView building = colony.getBuilding(citizen.getWorkBuilding());

        if (building instanceof AbstractBuildingView && building.getBuildingType() != ModBuildings.library.get() && citizen.getJobView() != null)
        {
            final WorkerBuildingModuleView moduleView = building.getModuleViewMatching(WorkerBuildingModuleView.class, m -> m.getJobEntry() == citizen.getJobView().getEntry());
            if (moduleView == null)
            {
                return;
            }

            windowCitizen.findPaneOfTypeByID(JOB_TITLE_LABEL, Text.class).setText(Component.translatable(LABEL_CITIZEN_JOB, Component.translatable(citizen.getJob())));
            windowCitizen.findPaneOfTypeByID(JOB_DESC_LABEL, Text.class).setText(Component.translatable(DESCRIPTION_CITIZEN_JOB));

            final Skill primary = moduleView.getPrimarySkill();
            windowCitizen.findPaneOfTypeByID(PRIMARY_SKILL_LABEL, Text.class)
              .setText(Component.translatable(PARTIAL_SKILL_NAME + primary.name().toLowerCase(Locale.US)).append(" (100% XP)"));
            windowCitizen.findPaneOfTypeByID(PRIMARY_SKILL_LABEL + IMAGE_APPENDIX, Image.class)
              .setImage(new ResourceLocation(BASE_IMG_SRC + primary.name().toLowerCase(Locale.US) + ".png"), false);

            if (primary.getComplimentary() != null && primary.getAdverse() != null)
            {
                windowCitizen.findPaneOfTypeByID(PRIMARY_SKILL_COM, Text.class)
                  .setText(Component.translatable(PARTIAL_SKILL_NAME + primary.getComplimentary().name().toLowerCase(Locale.US)).append(" ("
                                  + PRIMARY_DEPENDENCY_SHARE + "% XP)"));
                windowCitizen.findPaneOfTypeByID(PRIMARY_SKILL_COM + IMAGE_APPENDIX, Image.class)
                  .setImage(new ResourceLocation(BASE_IMG_SRC + primary.getComplimentary().name().toLowerCase(Locale.US) + ".png"), false);

                windowCitizen.findPaneOfTypeByID(PRIMARY_SKILL_ADV, Text.class)
                  .setText(Component.translatable(PARTIAL_SKILL_NAME + primary.getAdverse().name().toLowerCase(Locale.US)).append(" (-"
                                  + PRIMARY_DEPENDENCY_SHARE + "% XP)"));
                windowCitizen.findPaneOfTypeByID(PRIMARY_SKILL_ADV + IMAGE_APPENDIX, Image.class)
                  .setImage(new ResourceLocation(BASE_IMG_SRC + primary.getAdverse().name().toLowerCase(Locale.US) + ".png"), false);
            }

            final Skill secondary = moduleView.getSecondarySkill();
            windowCitizen.findPaneOfTypeByID(SECONDARY_SKILL_LABEL, Text.class)
              .setText(Component.translatable(PARTIAL_SKILL_NAME + secondary.name().toLowerCase(Locale.US)).append(" (50% XP)"));
            windowCitizen.findPaneOfTypeByID(SECONDARY_SKILL_LABEL + IMAGE_APPENDIX, Image.class)
              .setImage(new ResourceLocation(BASE_IMG_SRC + secondary.name().toLowerCase(Locale.US) + ".png"), false);

            if (secondary.getComplimentary() != null && secondary.getAdverse() != null)
            {
                windowCitizen.findPaneOfTypeByID(SECONDARY_SKILL_COM, Text.class)
                  .setText(Component.translatable(PARTIAL_SKILL_NAME + secondary.getComplimentary().name().toLowerCase(Locale.US)).append(" ("
                                  + SECONDARY_DEPENDENCY_SHARE + "% XP)"));
                windowCitizen.findPaneOfTypeByID(SECONDARY_SKILL_COM + IMAGE_APPENDIX, Image.class)
                  .setImage(new ResourceLocation(BASE_IMG_SRC + secondary.getComplimentary().name().toLowerCase(Locale.US) + ".png"), false);

                windowCitizen.findPaneOfTypeByID(SECONDARY_SKILL_ADV, Text.class)
                  .setText(Component.translatable(PARTIAL_SKILL_NAME + secondary.getAdverse().name().toLowerCase(Locale.US)).append(" (-"
                                  + SECONDARY_DEPENDENCY_SHARE + "% XP)"));
                windowCitizen.findPaneOfTypeByID(SECONDARY_SKILL_ADV + IMAGE_APPENDIX, Image.class)
                  .setImage(new ResourceLocation(BASE_IMG_SRC + secondary.getAdverse().name().toLowerCase(Locale.US) + ".png"), false);
            }
        }
        else
        {
            windowCitizen.findPaneOfTypeByID(PRIMARY_SKILL_LABEL + IMAGE_APPENDIX, Image.class).hide();
            windowCitizen.findPaneOfTypeByID(PRIMARY_SKILL_COM + IMAGE_APPENDIX, Image.class).hide();
            windowCitizen.findPaneOfTypeByID(PRIMARY_SKILL_ADV + IMAGE_APPENDIX, Image.class).hide();
            windowCitizen.findPaneOfTypeByID(SECONDARY_SKILL_LABEL + IMAGE_APPENDIX, Image.class).hide();
            windowCitizen.findPaneOfTypeByID(SECONDARY_SKILL_COM + IMAGE_APPENDIX, Image.class).hide();
            windowCitizen.findPaneOfTypeByID(SECONDARY_SKILL_ADV + IMAGE_APPENDIX, Image.class).hide();
        }
    }
}
