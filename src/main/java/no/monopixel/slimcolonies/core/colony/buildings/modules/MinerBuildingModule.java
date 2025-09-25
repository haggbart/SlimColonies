package no.monopixel.slimcolonies.core.colony.buildings.modules;

import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.IBuildingWorkerModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.ICreatesResolversModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.IPersistentModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.ITickingModule;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.entity.citizen.Skill;

import java.util.function.Function;

/**
 * Assignment module for miners.
 */
public class MinerBuildingModule extends WorkerBuildingModule implements ITickingModule, IPersistentModule, IBuildingWorkerModule, ICreatesResolversModule
{
    public MinerBuildingModule(
      final JobEntry entry,
      final Skill primary,
      final Skill secondary,
      final Function<IBuilding, Integer> sizeLimit)
    {
        super(entry, primary, secondary, sizeLimit);
    }

    @Override
    public boolean isFull()
    {
        return building.getAllAssignedCitizen().size() >= getModuleMax();
    }
}
