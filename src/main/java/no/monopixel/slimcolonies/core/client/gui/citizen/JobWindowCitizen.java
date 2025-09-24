package no.monopixel.slimcolonies.core.client.gui.citizen;

import no.monopixel.slimcolonies.api.colony.ICitizenDataView;
import no.monopixel.slimcolonies.api.util.constant.Constants;

import static no.monopixel.slimcolonies.api.util.constant.WindowConstants.CITIZEN_JOB_RESOURCE_SUFFIX;

/**
 * BOWindow for the citizen.
 */
public class JobWindowCitizen extends AbstractWindowCitizen
{
    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen to bind the window to.
     */
    public JobWindowCitizen(final ICitizenDataView citizen)
    {
        super(citizen, Constants.MOD_ID + CITIZEN_JOB_RESOURCE_SUFFIX);
        CitizenWindowUtils.updateJobPage(citizen, this, colony);
    }
}
