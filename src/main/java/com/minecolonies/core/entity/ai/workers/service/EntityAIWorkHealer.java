package com.minecolonies.core.entity.ai.workers.service;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingHospital;
import com.minecolonies.core.colony.jobs.JobHealer;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIInteract;
import com.minecolonies.core.entity.ai.workers.util.Patient;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.network.messages.client.CircleParticleEffectMessage;
import com.minecolonies.core.network.messages.client.StreamParticleEffectMessage;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.StatisticsConstants.CITIZENS_HEALED;


/**
 * Healer AI class.
 */
public class EntityAIWorkHealer extends AbstractEntityAIInteract<JobHealer, BuildingHospital>
{
    /**
     * Base xp gain for the smelter.
     */
    private static final double BASE_XP_GAIN = 2;

    /**
     * How many of each cure item it should try to request at a time.
     */
    private static final int REQUEST_COUNT = 16;

    /**
     * The current patient.
     */
    private Patient currentPatient = null;

    /**
     * Variable to check if the draining is in progress. And at which tick it is.
     */
    private int progressTicks = 0;

    /**
     * Max progress ticks until drainage is complete (per Level).
     */
    private static final int MAX_PROGRESS_TICKS = 30;

    /**
     * Remote patient to treat.
     */
    private ICitizenData remotePatient;

    /**
     * Player to heal.
     */
    private Player playerToHeal;


    /**
     * Constructor for the Cook. Defines the tasks the cook executes.
     *
     * @param job a cook job to use.
     */
    public EntityAIWorkHealer(@NotNull final JobHealer job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 1),
          new AITarget(START_WORKING, DECIDE, 1),
          new AITarget(DECIDE, this::decide, 20),
          new AITarget(CURE, this::cure, 20),
          new AITarget(FREE_CURE, this::freeCure, 20),
          new AITarget(CURE_PLAYER, this::curePlayer, 20),
          new AITarget(REQUEST_CURE, this::requestCure, 20),
          new AITarget(WANDER, this::wander, 20)

        );
        worker.setCanPickUpLoot(true);
    }

    /**
     * Decide what to do next. Check if all patients are up date, else update their states. Then check if there is any patient we can cure or request things for.
     *
     * @return the next state to go to.
     */
    private IAIState decide()
    {
        if (!walkToBuilding())
        {
            return DECIDE;
        }

        final BuildingHospital hospital = building;
        for (final AbstractEntityCitizen citizen : WorldUtil.getEntitiesWithinBuilding(world, AbstractEntityCitizen.class, building,
            cit -> cit.getCitizenData() != null && cit.getCitizenData().getCitizenInjuryHandler().isHurt()))
        {
            hospital.checkOrCreatePatientFile(citizen.getCivilianID());
        }

        for (final Patient patient : hospital.getPatients())
        {
            final ICitizenData data = hospital.getColony().getCitizenManager().getCivilian(patient.getId());
            if (data == null || !data.getEntity().isPresent() || (data.getEntity().isPresent() && !data.getEntity().get().getCitizenData().getCitizenInjuryHandler().isHurt()))
            {
                hospital.removePatientFile(patient);
                continue;
            }
            final EntityCitizen citizen = (EntityCitizen) data.getEntity().get();
            // Check if citizen is injured

            if (patient.getState() == Patient.PatientState.NEW)
            {
                this.currentPatient = patient;
                return REQUEST_CURE;
            }

            if (patient.getState() == Patient.PatientState.REQUESTED)
            {
                // For injuries, no cure items needed - proceed directly to treatment
                if (testRandomCureChance())
                {
                    this.currentPatient = patient;
                    return FREE_CURE;
                }

                this.currentPatient = patient;
                return CURE;
            }

            if (patient.getState() == Patient.PatientState.TREATED)
            {
                // For injuries, no cure items needed in inventory
                this.currentPatient = patient;
                return CURE;
            }
        }

        for (final Player player : WorldUtil.getEntitiesWithinBuilding(world,
          Player.class,
          building,
          player -> player.getHealth() < player.getMaxHealth() - 10 - (2 * building.getBuildingLevel())))
        {
            playerToHeal = player;
            return CURE_PLAYER;
        }

        final ICitizenData data = building.getColony().getCitizenManager().getRandomCitizen();
        if (data.getEntity().isPresent() && data.getEntity().get().getHealth() < 10.0
              && BlockPosUtil.getDistance2D(data.getEntity().get().blockPosition(), building.getPosition()) < building.getBuildingLevel() * 40)
        {
            remotePatient = data;
            return WANDER;
        }
        return DECIDE;
    }

    /**
     * Request the cure for a given patient.
     *
     * @return the next state to go to.
     */
    private IAIState requestCure()
    {
        if (currentPatient == null)
        {
            return DECIDE;
        }

        final ICitizenData data = building.getColony().getCitizenManager().getCivilian(currentPatient.getId());
        if (data == null || !data.getEntity().isPresent() || !data.getEntity().get().getCitizenData().getCitizenInjuryHandler().isHurt())
        {
            currentPatient = null;
            return DECIDE;
        }

        final EntityCitizen citizen = (EntityCitizen) data.getEntity().get();
        if (!walkToSafePos(citizen.blockPosition()))
        {
            return REQUEST_CURE;
        }


        // No cure items needed for injuries - just proceed with treatment
        currentPatient.setState(Patient.PatientState.REQUESTED);
        currentPatient = null;
        return DECIDE;
    }

    /**
     * Give a citizen the cure.
     *
     * @return the next state to go to.
     */
    private IAIState cure()
    {
        if (currentPatient == null)
        {
            return DECIDE;
        }

        final ICitizenData data = building.getColony().getCitizenManager().getCivilian(currentPatient.getId());
        if (data == null || !data.getEntity().isPresent() || !data.getEntity().get().getCitizenData().getCitizenInjuryHandler().isHurt())
        {
            currentPatient = null;
            return DECIDE;
        }

        final EntityCitizen citizen = (EntityCitizen) data.getEntity().get();
        if (!walkToSafePos(data.getEntity().get().blockPosition()))
        {
            return CURE;
        }

        // For injuries, just heal the citizen directly
        citizen.heal(10);
        data.getCitizenInjuryHandler().cure();

        recordTreatmentStats(citizen);
        worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        currentPatient.setState(Patient.PatientState.TREATED);
        currentPatient = null;
        return DECIDE;
    }

    /**
     * Do free cure magic.
     *
     * @return the next state to go to.
     */
    private IAIState freeCure()
    {
        if (currentPatient == null)
        {
            return DECIDE;
        }

        final ICitizenData data = building.getColony().getCitizenManager().getCivilian(currentPatient.getId());
        if (data == null || !data.getEntity().isPresent() || !data.getEntity().get().getCitizenData().getCitizenInjuryHandler().isHurt())
        {
            currentPatient = null;
            return DECIDE;
        }

        final EntityCitizen citizen = (EntityCitizen) data.getEntity().get();
        if (!walkToSafePos(citizen.blockPosition()))
        {
            progressTicks = 0;
            return FREE_CURE;
        }

        progressTicks++;
        if (progressTicks < MAX_PROGRESS_TICKS)
        {
            Network.getNetwork().sendToTrackingEntity(
              new StreamParticleEffectMessage(
                worker.position().add(0, 2, 0),
                citizen.position(),
                ParticleTypes.HEART,
                progressTicks % MAX_PROGRESS_TICKS,
                MAX_PROGRESS_TICKS), worker);

            Network.getNetwork().sendToTrackingEntity(
              new CircleParticleEffectMessage(
                worker.position().add(0, 2, 0),
                ParticleTypes.HEART,
                progressTicks), worker);

            return getState();
        }

        progressTicks = 0;
        recordTreatmentStats(citizen);
        worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        citizen.getCitizenData().getCitizenInjuryHandler().cure();
        currentPatient.setState(Patient.PatientState.TREATED);
        currentPatient = null;
        return DECIDE;
    }

    /**
     * Cure the player.
     *
     * @return the next sate to go to.
     */
    private IAIState curePlayer()
    {
        if (playerToHeal == null)
        {
            return DECIDE;
        }

        if (!walkToUnSafePos(playerToHeal.blockPosition()))
        {
            return getState();
        }

        playerToHeal.heal(playerToHeal.getMaxHealth() - playerToHeal.getHealth() - 5 - building.getBuildingLevel());
        worker.getCitizenExperienceHandler().addExperience(1);

        return DECIDE;
    }

    @Override
    public IAIState getStateAfterPickUp()
    {
        return CURE;
    }

    /**
     * Wander around in the colony from citizen to citizen.
     *
     * @return the next state to go to.
     */
    private IAIState wander()
    {
        if (remotePatient == null || !remotePatient.getEntity().isPresent())
        {
            return DECIDE;
        }

        final EntityCitizen citizen = (EntityCitizen) remotePatient.getEntity().get();
        if (!walkToUnSafePos(remotePatient.getEntity().get().blockPosition()))
        {
            return getState();
        }

        Network.getNetwork().sendToTrackingEntity(
          new CircleParticleEffectMessage(
            remotePatient.getEntity().get().position(),
            ParticleTypes.HEART,
            1), worker);

        citizen.heal(citizen.getMaxHealth() - citizen.getHealth() - 5 - building.getBuildingLevel());
        citizen.markDirty(10);
        worker.getCitizenExperienceHandler().addExperience(1);

        remotePatient = null;

        return START_WORKING;
    }

    /**
     * Check if we can cure a citizen randomly. Currently it is done workerLevel/10 times every hour (at least 1).
     *
     * @return true if so.
     */
    private boolean testRandomCureChance()
    {
        return worker.getRandom().nextInt(60 * 60) <= Math.max(1, getSecondarySkillLevel() / 20);
    }


    @Override
    public Class<BuildingHospital> getExpectedBuildingClass()
    {
        return BuildingHospital.class;
    }

    private void recordTreatmentStats(EntityCitizen citizen)
    {
        // Record injury treatment instead of disease treatment
        worker.getCitizenColonyHandler().getColonyOrRegister().getStatisticsManager().increment(CITIZENS_HEALED, worker.getCitizenColonyHandler().getColonyOrRegister().getDay());
    }
}
