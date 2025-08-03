package com.minecolonies.core.entity.ai.minimal;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.IStateAI;
import com.minecolonies.api.entity.ai.statemachine.states.CitizenAIState;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingHospital;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import com.minecolonies.core.datalistener.model.Disease;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.entity.pathfinding.navigation.EntityNavigationUtils;
import com.minecolonies.core.network.messages.client.CircleParticleEffectMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

import java.util.List;

import static com.minecolonies.api.util.constant.GuardConstants.BASIC_VOLUME;
import static com.minecolonies.api.util.constant.TranslationConstants.NO_HOSPITAL;
import static com.minecolonies.api.util.constant.TranslationConstants.WAITING_FOR_CURE;
import static com.minecolonies.core.entity.ai.minimal.EntityAISickTask.DiseaseState.*;
import static com.minecolonies.core.entity.citizen.citizenhandlers.CitizenDiseaseHandler.SEEK_DOCTOR_HEALTH;

/**
 * The AI task for citizens to execute when they are supposed to eat.
 */
public class EntityAISickTask implements IStateAI
{
    /**
     * Min distance to hut before pathing to hospital.
     */
    private static final int MIN_DIST_TO_HUT = 5;

    /**
     * Min distance to hospital before trying to find a bed.
     */
    private static final int MIN_DIST_TO_HOSPITAL = 3;

    /**
     * Min distance to the hospital in general.
     */
    private static final long MINIMUM_DISTANCE_TO_HOSPITAL = 10;

    /**
     * Required time to cure.
     */
    private static final int REQUIRED_TIME_TO_CURE = 60;

    /**
     * Attempts to position right in the bed.
     */
    private static final int GOING_TO_BED_ATTEMPTS = 20;

    /**
     * Citizen data.
     */
    private final ICitizenData citizenData;

    /**
     * The waiting ticks.
     */
    private int waitingTicks = 0;

    /**
     * The bed the citizen is sleeping in.
     */
    private BlockPos usedBed;

    /**
     * The different types of AIStates related to being sick.
     */
    public enum DiseaseState implements IState
    {
        CHECK_FOR_CURE,
        GO_TO_HUT,
        SEARCH_HOSPITAL,
        GO_TO_HOSPITAL,
        WAIT_FOR_CURE,
        FIND_EMPTY_BED,
        APPLY_CURE,
        WANDER
    }

    /**
     * The citizen assigned to this task.
     */
    private final EntityCitizen citizen;

    /**
     * Restaurant to which the citizen should path.
     */
    private BlockPos bestHospital;

    /**
     * Instantiates this task.
     *
     * @param citizen the citizen.
     */
    public EntityAISickTask(final EntityCitizen citizen)
    {
        super();
        this.citizen = citizen;
        this.citizenData = citizen.getCitizenData();

        citizen.getCitizenAI().addTransition(new TickingTransition<>(CitizenAIState.SICK, this::isSick, () -> CHECK_FOR_CURE, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(CHECK_FOR_CURE, () -> true, this::checkForCure, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(WANDER, () -> true, this::wander, 200));

        citizen.getCitizenAI().addTransition(new TickingTransition<>(CHECK_FOR_CURE, () -> true, this::checkForCure, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(GO_TO_HUT, () -> true, this::goToHut, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(SEARCH_HOSPITAL, () -> true, this::searchHospital, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(GO_TO_HOSPITAL, () -> true, this::goToHospital, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(WAIT_FOR_CURE, () -> true, this::waitForCure, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(APPLY_CURE, () -> true, this::applyCure, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(FIND_EMPTY_BED, () -> true, this::findEmptyBed, 20));
    }

    private boolean isSick()
    {
        if (citizen.getCitizenData().getCitizenDiseaseHandler().isSick()
            || citizen.getCitizenData().getCitizenDiseaseHandler().isHurt())
        {
            reset();
            return true;
        }

        return false;
    }

    /**
     * Do a bit of wandering.
     *
     * @return start over.
     */
    public DiseaseState wander()
    {
        EntityNavigationUtils.walkToRandomPos(citizen, 10, 0.6D);
        return CHECK_FOR_CURE;
    }

    /**
     * Find an empty bed to ly in.
     *
     * @return the next state to go to.
     */
    private DiseaseState findEmptyBed()
    {
        // Finding bed
        if (usedBed == null && citizen.getCitizenData() != null)
        {
            this.usedBed = citizen.getCitizenData().getBedPos();
            if (citizen.getCitizenData().getBedPos().equals(BlockPos.ZERO))
            {
                this.usedBed = null;
            }
        }

        final BlockPos hospitalPos = citizen.getCitizenColonyHandler().getColonyOrRegister().getBuildingManager().getBestBuilding(citizen, BuildingHospital.class);
        final IColony colony = citizen.getCitizenColonyHandler().getColonyOrRegister();
        final IBuilding hospital = colony.getBuildingManager().getBuilding(hospitalPos);

        if (hospital instanceof BuildingHospital)
        {
            if (usedBed != null && !((BuildingHospital) hospital).getBedList().contains(usedBed))
            {
                usedBed = null;
            }

            if (usedBed == null)
            {
                for (final BlockPos pos : ((BuildingHospital) hospital).getBedList())
                {
                    final Level world = citizen.level;
                    BlockState state = world.getBlockState(pos);
                    if (state.is(BlockTags.BEDS)
                          && !state.getValue(BedBlock.OCCUPIED)
                          && state.getValue(BedBlock.PART).equals(BedPart.HEAD)
                          && world.isEmptyBlock(pos.above()))
                    {
                        citizen.getCitizenData().getCitizenDiseaseHandler().setSleepsAtHospital(true);
                        usedBed = pos;
                        ((BuildingHospital) hospital).registerPatient(usedBed, citizen.getCivilianID());
                        return FIND_EMPTY_BED;
                    }
                }

                if (usedBed == null)
                {
                    return WAIT_FOR_CURE;
                }
            }

            if (EntityNavigationUtils.walkToPosInBuilding(citizen, usedBed, hospital, 3))
            {
                waitingTicks++;
                if (!citizen.getCitizenSleepHandler().trySleep(usedBed))
                {
                    ((BuildingHospital) hospital).registerPatient(usedBed, 0);
                    citizen.getCitizenData().setBedPos(BlockPos.ZERO);
                    usedBed = null;
                }
            }
        }

        if (waitingTicks > GOING_TO_BED_ATTEMPTS)
        {
            waitingTicks = 0;
            return WAIT_FOR_CURE;
        }
        return FIND_EMPTY_BED;
    }

    /**
     * Actual action of eating.
     *
     * @return the next state to go to, if successful idle.
     */
    private IState applyCure()
    {
        if (checkForCure() != APPLY_CURE)
        {
            return CHECK_FOR_CURE;
        }

        final Disease disease = citizen.getCitizenData().getCitizenDiseaseHandler().getDisease();
        if (disease == null)
        {
            return CitizenAIState.IDLE;
        }

        final List<ItemStorage> list = disease.cureItems();
        if (!list.isEmpty())
        {
            citizen.setItemInHand(InteractionHand.MAIN_HAND, list.get(citizen.getRandom().nextInt(list.size())).getItemStack());
        }

        citizen.swing(InteractionHand.MAIN_HAND);
        citizen.playSound(SoundEvents.NOTE_BLOCK_HARP.get(), (float) BASIC_VOLUME, (float) SoundUtils.getRandomPentatonic(citizen.getRandom()));
        Network.getNetwork().sendToTrackingEntity(
          new CircleParticleEffectMessage(
            citizen.position().add(0, 2, 0),
            ParticleTypes.HAPPY_VILLAGER,
            waitingTicks), citizen);


        waitingTicks++;
        if (waitingTicks < REQUIRED_TIME_TO_CURE)
        {
            return APPLY_CURE;
        }

        cure();
        return CitizenAIState.IDLE;
    }

    /**
     * Cure the citizen.
     */
    private void cure()
    {
        final Disease disease = citizen.getCitizenData().getCitizenDiseaseHandler().getDisease();
        if (disease != null)
        {
            for (final ItemStorage cure : disease.cureItems())
            {
                final int slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(citizen, Disease.hasCureItem(cure));
                if (slot != -1)
                {
                    citizenData.getInventory().extractItem(slot, 1, false);
                }
            }
        }

        if (usedBed != null)
        {
            final BlockPos hospitalPos = citizen.getCitizenColonyHandler().getColonyOrRegister().getBuildingManager().getBestBuilding(citizen, BuildingHospital.class);
            final IColony colony = citizen.getCitizenColonyHandler().getColonyOrRegister();
            final IBuilding hospital = colony.getBuildingManager().getBuilding(hospitalPos);
            ((BuildingHospital) hospital).registerPatient(usedBed, 0);
            usedBed = null;
            citizen.getCitizenData().setBedPos(BlockPos.ZERO);
        }
        citizen.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        citizen.getCitizenData().getCitizenDiseaseHandler().cure();
        citizen.setHealth(citizen.getMaxHealth());
        reset();
    }

    /**
     * Stay in bed while waiting to be cured.
     *
     * @return the next state to go to.
     */
    private IState waitForCure()
    {
        final IColony colony = citizenData.getColony();
        bestHospital = colony.getBuildingManager().getBestBuilding(citizen, BuildingHospital.class);

        if (bestHospital == null)
        {
            return SEARCH_HOSPITAL;
        }

        final IState state = checkForCure();
        if (state == APPLY_CURE)
        {
            return APPLY_CURE;
        }
        else if (state == CitizenAIState.IDLE)
        {
            reset();
            return CitizenAIState.IDLE;
        }

        if (citizen.getRandom().nextInt(60*60*2) < 1)
        {
            cure();
            return CitizenAIState.IDLE;
        }

        if (citizen.getCitizenSleepHandler().isAsleep())
        {
            final BlockPos hospital = colony.getBuildingManager().getBestBuilding(citizen, BuildingHospital.class);
            if (hospital != null)
            {
                final IBuilding building = colony.getBuildingManager().getBuilding(hospital);
                if (building instanceof BuildingHospital && !((BuildingHospital) building).getBedList().contains(citizen.getCitizenSleepHandler().getBedLocation()))
                {
                    citizen.getCitizenSleepHandler().onWakeUp();
                }
            }
        }

        if (!citizen.getCitizenSleepHandler().isAsleep() && BlockPosUtil.getDistance2D(bestHospital, citizen.blockPosition()) > MINIMUM_DISTANCE_TO_HOSPITAL)
        {
            return GO_TO_HOSPITAL;
        }

        if (!citizen.getCitizenSleepHandler().isAsleep())
        {
            return FIND_EMPTY_BED;
        }

        return WAIT_FOR_CURE;
    }

    /**
     * Go to the hut to move to the hospital from there.
     *
     * @return the next state to go to.
     */
    private IState goToHut()
    {
        final IBuilding buildingWorker = citizenData.getWorkBuilding();
        citizen.getCitizenData().getCitizenDiseaseHandler().setSleepsAtHospital(false);

        if (buildingWorker == null)
        {
            return SEARCH_HOSPITAL;
        }

        if (citizen.getCitizenSleepHandler().isAsleep() || EntityNavigationUtils.walkToBuilding(citizen, buildingWorker))
        {
            return SEARCH_HOSPITAL;
        }
        return GO_TO_HUT;
    }

    /**
     * Go to the previously found placeToPath to get cure.
     *
     * @return the next state to go to.
     */
    private IState goToHospital()
    {
        citizen.getCitizenData().getCitizenDiseaseHandler().setSleepsAtHospital(false);
        if (bestHospital == null)
        {
            return SEARCH_HOSPITAL;
        }

        if (citizen.getCitizenSleepHandler().isAsleep() || EntityNavigationUtils.walkToPos(citizen, bestHospital, MIN_DIST_TO_HOSPITAL, true))
        {
            return WAIT_FOR_CURE;
        }
        return SEARCH_HOSPITAL;
    }

    /**
     * Search for a placeToPath within the colony of the citizen.
     *
     * @return the next state to go to.
     */
    private IState searchHospital()
    {
        final IColony colony = citizenData.getColony();
        final Disease disease = citizen.getCitizenData().getCitizenDiseaseHandler().getDisease();
        bestHospital = colony.getBuildingManager().getBestBuilding(citizen, BuildingHospital.class);

        if (bestHospital == null)
        {
            if (disease == null)
            {
                return CitizenAIState.IDLE;
            }
            citizenData.triggerInteraction(new StandardInteraction(Component.translatable(NO_HOSPITAL, disease.name(), disease.getCureString()),
              Component.translatable(NO_HOSPITAL),
              ChatPriority.BLOCKING));
            return WANDER;
        }
        else if (disease != null)
        {
            citizenData.triggerInteraction(new StandardInteraction(Component.translatable(WAITING_FOR_CURE, disease.name(), disease.getCureString()),
              Component.translatable(WAITING_FOR_CURE),
              ChatPriority.BLOCKING));
        }

        return GO_TO_HOSPITAL;
    }

    /**
     * Checks if the citizen has the cure in the inventory and makes a decision based on that.
     *
     * @return the next state to go to.
     */
    private IState checkForCure()
    {
        final Disease disease = citizen.getCitizenData().getCitizenDiseaseHandler().getDisease();
        if (disease == null)
        {
            if (citizen.getHealth() > SEEK_DOCTOR_HEALTH)
            {
                reset();
                return CitizenAIState.IDLE;
            }
            return GO_TO_HUT;
        }
        for (final ItemStorage cure : disease.cureItems())
        {
            final int slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(citizen, Disease.hasCureItem(cure));
            if (slot == -1)
            {
                if (citizen.getCitizenData().getCitizenDiseaseHandler().isSick())
                {
                    return GO_TO_HUT;
                }

                reset();
                return CitizenAIState.IDLE;
            }
        }
        return APPLY_CURE;
    }

    /**
     * Resets the state of the AI.
     */
    private void reset()
    {
        waitingTicks = 0;
        citizen.releaseUsingItem();
        citizen.stopUsingItem();
        citizen.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        bestHospital = null;
        citizen.getCitizenData().getCitizenDiseaseHandler().setSleepsAtHospital(false);
    }

    // TODO: Citizen AI should set status icons
    public void start()
    {
        citizen.getCitizenData().setVisibleStatus(VisibleCitizenStatus.SICK);
    }
}
