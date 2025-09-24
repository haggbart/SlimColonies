package no.monopixel.slimcolonies.core.colony.buildings.modules;

import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.IBuildingWorkerModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.IBuildingEventsModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.ICreatesResolversModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.IPersistentModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.ITickingModule;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.entity.citizen.Skill;
import no.monopixel.slimcolonies.core.util.BuildingUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.Function;

/**
 * Assignment module for pupils.
 */
public class ChildrenBuildingModule extends WorkerBuildingModule implements IBuildingEventsModule, ITickingModule, IPersistentModule, IBuildingWorkerModule, ICreatesResolversModule
{
    public ChildrenBuildingModule(final JobEntry entry,
      final Skill primary,
      final Skill secondary,
      final Function<IBuilding, Integer> sizeLimit)
    {
        super(entry, primary, secondary, sizeLimit);

    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        // If we have no active worker, grab one from the Colony
        if (!isFull() && BuildingUtils.canAutoHire(building, getHiringMode(), null))
        {
            for (final ICitizenData data : colony.getCitizenManager().getCitizens())
            {
                if (data.isChild() && data.getWorkBuilding() == null)
                {
                    assignCitizen(data);
                }
            }
        }

        for (final ICitizenData citizenData : new ArrayList<>(getAssignedCitizen()))
        {
            if (!citizenData.isChild())
            {
                removeCitizen(citizenData);
            }
        }
    }
}
