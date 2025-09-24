package no.monopixel.slimcolonies.core.colony.buildings.modules;

import com.google.common.collect.ImmutableList;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.IBuildingWorkerModule;
import com.minecolonies.api.colony.buildings.modules.*;
import no.monopixel.slimcolonies.api.colony.buildings.modules.*;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.colony.requestsystem.resolver.IRequestResolver;
import no.monopixel.slimcolonies.api.entity.citizen.Skill;
import no.monopixel.slimcolonies.api.util.constant.TypeConstants;
import no.monopixel.slimcolonies.core.colony.requestsystem.resolvers.BuildingRequestResolver;

import java.util.List;
import java.util.function.Function;

/**
 * The worker module for citizen where they are assigned to if they work at it.
 */
public class NoPrivateCrafterWorkerModule extends WorkerBuildingModule implements IAssignsJob,
    IBuildingEventsModule,
    ITickingModule,
    IPersistentModule, IBuildingWorkerModule,
    ICreatesResolversModule
{
    public NoPrivateCrafterWorkerModule(
      final JobEntry entry,
      final Skill primary,
      final Skill secondary,
      final Function<IBuilding, Integer> sizeLimit)
    {
        super(entry, primary, secondary, sizeLimit);
    }

    @Override
    public List<IRequestResolver<?>> createResolvers()
    {
        return ImmutableList.of(new BuildingRequestResolver(building.getRequester().getLocation(), building.getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN)));
    }
}
