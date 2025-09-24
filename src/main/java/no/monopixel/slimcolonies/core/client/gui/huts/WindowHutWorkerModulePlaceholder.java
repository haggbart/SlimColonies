package no.monopixel.slimcolonies.core.client.gui.huts;

import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.client.gui.AbstractWindowWorkerModuleBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.views.AbstractBuildingView;

/**
 * BOWindow for worker. Placeholder for many different jobs.
 *
 * @param <B> Object extending {@link AbstractBuildingView}.
 */
public class WindowHutWorkerModulePlaceholder<B extends IBuildingView> extends AbstractWindowWorkerModuleBuilding<B>
{
    private static final String WORKER_PLACEHOLDER_RESOURCE_SUFFIX = ":gui/windowhutworkerplaceholder.xml";

    /**
     * BOWindow for worker placeholder. Used by buildings not listed above this file.
     *
     * @param building AbstractBuilding extending {@link AbstractBuildingView}.
     */
    public WindowHutWorkerModulePlaceholder(final B building)
    {
        super(building, Constants.MOD_ID + WORKER_PLACEHOLDER_RESOURCE_SUFFIX);
    }
}
