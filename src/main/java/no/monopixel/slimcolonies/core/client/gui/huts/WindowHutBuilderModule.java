package no.monopixel.slimcolonies.core.client.gui.huts;

import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.client.gui.AbstractWindowWorkerModuleBuilding;
import no.monopixel.slimcolonies.core.client.gui.WindowHutGuide;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import static no.monopixel.slimcolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the builder hut.
 */
public class WindowHutBuilderModule extends AbstractWindowWorkerModuleBuilding<BuildingBuilder.View>
{
    /**
     * The advancement location.
     */

    private static final ResourceLocation GUIDE_ADVANCEMENT = new ResourceLocation(Constants.MOD_ID, "minecolonies/check_out_guide");

    /**
     * If the guide should be attempted to be opened.
     */
    private final boolean needGuide;

    /**
     * Constructor for window builder hut.
     *
     * @param building {@link BuildingBuilder.View}.
     */
    public WindowHutBuilderModule(final BuildingBuilder.View building)
    {
        this(building, true);
    }

    /**
     * Constructor for window builder hut.
     *
     * @param needGuide if the guide should be opened.
     * @param building  {@link BuildingBuilder.View}.
     */
    public WindowHutBuilderModule(final BuildingBuilder.View building, final boolean needGuide)
    {
        super(building, Constants.MOD_ID + HUT_BUILDER_RESOURCE_SUFFIX);
        this.needGuide = needGuide;
    }

    @Override
    public void onOpened()
    {
        if (needGuide)
        {
            final Advancement ad = Minecraft.getInstance().player.connection.getAdvancements().getAdvancements().get(GUIDE_ADVANCEMENT);
            if (ad == null || !Minecraft.getInstance().player.connection.getAdvancements().progress.getOrDefault(ad, new AdvancementProgress()).isDone())
            {
                close();
                new WindowHutGuide(building).open();
                return;
            }
        }
        super.onOpened();
    }
}
