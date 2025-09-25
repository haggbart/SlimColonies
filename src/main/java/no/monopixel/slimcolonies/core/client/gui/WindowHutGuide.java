package no.monopixel.slimcolonies.core.client.gui;

import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.client.gui.huts.WindowHutBuilderModule;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingBuilder;

import static no.monopixel.slimcolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the builder hut.
 */
public class WindowHutGuide extends AbstractWindowSkeleton
{
    /**
     * Color constants for builder list.
     */
    private final BuildingBuilder.View building;

    /**
     * Constructor for window builder hut.
     *
     * @param building {@link BuildingBuilder.View}.
     */
    public WindowHutGuide(final BuildingBuilder.View building)
    {
        super(Constants.MOD_ID + GUIDE_RESOURCE_SUFFIX);
        registerButton(GUIDE_CONFIRM, this::closeGuide);
        registerButton(GUIDE_CLOSE, this::closeGuide);

        this.building = building;
    }

    private void closeGuide()
    {
        close();
        new WindowHutBuilderModule(building, false).open();
    }
}
