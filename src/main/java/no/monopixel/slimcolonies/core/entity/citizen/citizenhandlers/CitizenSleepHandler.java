package no.monopixel.slimcolonies.core.entity.citizen.citizenhandlers;

import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.interactionhandling.ChatPriority;
import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.api.entity.citizen.citizenhandlers.ICitizenSleepHandler;
import no.monopixel.slimcolonies.api.util.BlockPosUtil;
import no.monopixel.slimcolonies.api.util.EntityUtils;
import no.monopixel.slimcolonies.api.util.WorldUtil;
import no.monopixel.slimcolonies.core.colony.interactionhandling.SimpleNotificationInteraction;
import no.monopixel.slimcolonies.core.colony.interactionhandling.StandardInteraction;
import no.monopixel.slimcolonies.core.colony.jobs.JobMiner;
import no.monopixel.slimcolonies.core.util.citizenutils.CitizenItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;

import static no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen.DATA_BED_POS;
import static no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen.DATA_IS_ASLEEP;
import static no.monopixel.slimcolonies.api.research.util.ResearchConstants.WORK_LONGER;
import static no.monopixel.slimcolonies.api.util.constant.CitizenConstants.NIGHT;
import static no.monopixel.slimcolonies.api.util.constant.Constants.HALF_BLOCK;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.COREMOD_ENTITY_CITIZEN_SLEEPING;

/**
 * Handles the sleep of the citizen.
 */
public class CitizenSleepHandler implements ICitizenSleepHandler
{
    /**
     * The additional weight for Y diff
     */
    private static final double Y_DIFF_WEIGHT = 1.5;

    /**
     * The rough time traveling one block takes, in ticks
     */
    private static final double TIME_PER_BLOCK           = 6;
    private static final double MAX_NO_COMPLAIN_DISTANCE = 160;

    /**
     * The citizen assigned to this manager.
     */
    private final AbstractEntityCitizen citizen;

    /**
     * Constructor for the experience handler.
     *
     * @param citizen the citizen owning the handler.
     */
    public CitizenSleepHandler(final AbstractEntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    /**
     * Is the citizen a sleep?
     *
     * @return true when a sleep.
     */
    @Override
    public boolean isAsleep()
    {
        if (citizen.getCitizenData() != null)
        {
            return citizen.getCitizenData().isAsleep();
        }

        return citizen.getEntityData().get(DATA_IS_ASLEEP);
    }

    /**
     * Sets if the citizen is a sleep. Caution: Use trySleep(BlockPos) for better control
     *
     * @param isAsleep True to make the citizen sleep.
     */
    private void setIsAsleep(final boolean isAsleep)
    {
        if (citizen.getCitizenData() != null)
        {
            citizen.getCitizenData().setAsleep(isAsleep);
        }
        citizen.getEntityData().set(DATA_IS_ASLEEP, isAsleep);
    }

    /**
     * Attempts a sleep interaction with the citizen and the given bed.
     *
     * @param bedLocation The possible location to sleep.
     */
    @Override
    public boolean trySleep(final BlockPos bedLocation)
    {
        final BlockState state = WorldUtil.isEntityBlockLoaded(citizen.level, bedLocation) ? citizen.level.getBlockState(bedLocation) : null;
        final boolean isBed = state != null && state.getBlock().isBed(state, citizen.level, bedLocation, citizen);

        if (!isBed)
        {
            return false;
        }


        citizen.setPose(Pose.SLEEPING);
        citizen.getNavigation().stop();

        final double zOffset = state.getValue(BedBlock.FACING).getAxis() == Direction.Axis.Z && citizen.getCitizenData().isChild() ? 0 : HALF_BLOCK;
        final double xOffset = state.getValue(BedBlock.FACING).getAxis() == Direction.Axis.X && citizen.getCitizenData().isChild() ? 0 : HALF_BLOCK;

        citizen.setPos(((double) bedLocation.getX() + xOffset),
            (double) bedLocation.getY() + 0.6875D,
            ((double) bedLocation.getZ() + zOffset));
        citizen.setSleepingPos(bedLocation);

        citizen.setDeltaMovement(Vec3.ZERO);
        citizen.hasImpulse = true;

        //Remove item while citizen is asleep.
        CitizenItemUtils.removeHeldItem(citizen);

        setIsAsleep(true);

        citizen.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(COREMOD_ENTITY_CITIZEN_SLEEPING), ChatPriority.HIDDEN));

        if (citizen.getCitizenData() != null)
        {
            citizen.getCitizenData().setBedPos(bedLocation);
        }
        citizen.getEntityData().set(DATA_BED_POS, bedLocation);

        citizen.getCitizenData().getColony().getCitizenManager().onCitizenSleep();

        return true;
    }

    /**
     * Called when the citizen wakes up.
     */
    @Override
    public void onWakeUp()
    {
        notifyCitizenHandlersOfWakeUp();

        //Only do this if he really sleeps
        if (isAsleep())
        {
            spawnCitizenFromBed();
        }

        citizen.setPose(Pose.STANDING);
        citizen.clearSleepingPos();
        setIsAsleep(false);
    }

    private void notifyCitizenHandlersOfWakeUp()
    {
        if (citizen.getCitizenColonyHandler().getWorkBuilding() != null)
        {
            citizen.getCitizenColonyHandler().getWorkBuilding().onWakeUp();
        }
        if (citizen.getCitizenJobHandler().getColonyJob() != null)
        {
            citizen.getCitizenJobHandler().getColonyJob().onWakeUp();
        }

        final IBuilding homeBuilding = citizen.getCitizenColonyHandler().getHomeBuilding();
        if (homeBuilding != null)
        {
            homeBuilding.onWakeUp();
        }
    }

    private void spawnCitizenFromBed()
    {
        final BlockPos spawn;
        final BlockState bedState = getBedLocation().equals(BlockPos.ZERO) ? null : citizen.level.getBlockState(getBedLocation());
        if (!getBedLocation().equals(BlockPos.ZERO) && bedState.is(BlockTags.BEDS))
        {
            if (bedState.getValue(BedBlock.PART) == BedPart.HEAD)
            {
                final BlockPos relPos = getBedLocation().relative(bedState.getValue(BedBlock.FACING).getOpposite());
                final BlockState lowerState = citizen.level.getBlockState(relPos);
                if (lowerState.is(BlockTags.BEDS) && lowerState.getValue(BedBlock.PART) == BedPart.FOOT)
                {
                    spawn = EntityUtils.getSpawnPoint(citizen.level, relPos);
                }
                else
                {
                    spawn = EntityUtils.getSpawnPoint(citizen.level, getBedLocation());
                }
            }
            else
            {
                spawn = EntityUtils.getSpawnPoint(citizen.level, getBedLocation());
            }
        }
        else
        {
            spawn = citizen.blockPosition();
        }

        if (spawn != null && !spawn.equals(BlockPos.ZERO))
        {
            citizen.setPos(spawn.getX() + HALF_BLOCK, spawn.getY(), spawn.getZ() + HALF_BLOCK);
        }

        setIsAsleep(false);
        if (citizen.getCitizenData() != null)
        {
            citizen.getCitizenData().setBedPos(new BlockPos(0, 0, 0));
        }
        citizen.getEntityData().set(DATA_BED_POS, new BlockPos(0, 0, 0));
    }

    /**
     * Get the bed location of the citizen.
     *
     * @return the bed location.
     */
    @Override
    public BlockPos getBedLocation()
    {
        return citizen.getEntityData().get(DATA_BED_POS);
    }

    @Override
    public boolean shouldGoSleep()
    {
        final BlockPos homePos = citizen.getCitizenData().getHomePosition();
        BlockPos citizenPos = citizen.blockPosition();
        if (homePos == null)
        {
            return false;
        }

        int additionalDist = 0;

        if (citizen.isInvisible())
        {
            return false;
        }

        // Additional distance for miners
        if (citizen.getCitizenData().getJob() instanceof JobMiner && citizen.getCitizenData().getWorkBuilding().getPosition().getY() - 20 > citizenPos.getY())
        {
            final BlockPos workPos = citizen.getCitizenData().getWorkBuilding().getID();
            additionalDist = (int) BlockPosUtil.getDistance2D(citizenPos, workPos) + Math.abs(citizenPos.getY() - workPos.getY()) * 3;
            citizenPos = workPos;
        }

        // Calc distance with some y weight
        final int xDiff = Math.abs(homePos.getX() - citizenPos.getX());
        final int zDiff = Math.abs(homePos.getZ() - citizenPos.getZ());
        final int yDiff = (int) (Math.abs(homePos.getY() - citizenPos.getY()) * Y_DIFF_WEIGHT);

        final double timeNeeded = (Math.sqrt(xDiff * xDiff + zDiff * zDiff + yDiff * yDiff) + additionalDist) * TIME_PER_BLOCK;

        // Estimated arrival is 1hour past night
        final double timeLeft = (citizen.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(WORK_LONGER) == 0
            ? NIGHT : NIGHT + citizen.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(WORK_LONGER) * 1000) - (
            citizen.level.getDayTime() % 24000);
        if (timeLeft <= 0 || (timeLeft - timeNeeded <= 0))
        {
            if (citizen.getCitizenData().getWorkBuilding() != null)
            {
                final double workHomeDistance = Math.sqrt(BlockPosUtil.getDistanceSquared(homePos, citizen.getCitizenData().getWorkBuilding().getID()));
                if (workHomeDistance > MAX_NO_COMPLAIN_DISTANCE)
                {
                    citizen.getCitizenData()
                        .triggerInteraction(new SimpleNotificationInteraction(Component.translatable("no.monopixel.slimcolonies.coremod.gui.chat.hometoofar"),
                            ChatPriority.IMPORTANT));
                }
            }
            return true;
        }

        return false;
    }
}
