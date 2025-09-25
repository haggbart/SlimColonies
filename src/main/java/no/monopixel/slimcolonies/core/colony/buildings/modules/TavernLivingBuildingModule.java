package no.monopixel.slimcolonies.core.colony.buildings.modules;


import no.monopixel.slimcolonies.api.colony.buildings.modules.IAssignsCitizen;
import no.monopixel.slimcolonies.api.colony.buildings.modules.IBuildingEventsModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.IPersistentModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.ITickingModule;

/**
 * The tavern living module for citizen to call their home.
 */
public class TavernLivingBuildingModule extends LivingBuildingModule implements IAssignsCitizen, IBuildingEventsModule, ITickingModule, IPersistentModule
{
    @Override
    public int getModuleMax()
    {
        return building.getBuildingLevel() > 0 ? 4 : 0;
    }
}
