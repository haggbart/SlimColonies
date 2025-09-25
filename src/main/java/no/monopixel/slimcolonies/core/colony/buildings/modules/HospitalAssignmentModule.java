package no.monopixel.slimcolonies.core.colony.buildings.modules;

import net.minecraft.world.entity.ai.attributes.Attributes;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.IBuildingWorkerModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.IBuildingEventsModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.ICreatesResolversModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.IPersistentModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.ITickingModule;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.api.entity.citizen.Skill;
import no.monopixel.slimcolonies.core.util.AttributeModifierUtils;

import java.util.Optional;
import java.util.function.Function;

import static no.monopixel.slimcolonies.api.util.constant.CitizenConstants.SKILL_BONUS_ADD;

/**
 * Assignment module for couriers
 */
public class HospitalAssignmentModule extends WorkerBuildingModule implements IBuildingEventsModule,
    ITickingModule,
    IPersistentModule, IBuildingWorkerModule,
    ICreatesResolversModule
{
    public HospitalAssignmentModule(
        final JobEntry entry,
        final Skill primary,
        final Skill secondary,
        final Function<IBuilding, Integer> sizeLimit)
    {
        super(entry, primary, secondary, sizeLimit);
    }

    @Override
    void onRemoval(final ICitizenData citizen)
    {
        super.onRemoval(citizen);
        final Optional<AbstractEntityCitizen> optCitizen = citizen.getEntity();
        optCitizen.ifPresent(entityCitizen -> AttributeModifierUtils.removeModifier(entityCitizen, SKILL_BONUS_ADD, Attributes.MOVEMENT_SPEED));
    }
}
