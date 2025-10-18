package no.monopixel.slimcolonies.core.entity.ai.minimal;

import com.ldtteam.domumornamentum.block.decorative.PanelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.entity.ai.IStateAI;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.CitizenAIState;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.IState;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import no.monopixel.slimcolonies.api.entity.citizen.VisibleCitizenStatus;
import no.monopixel.slimcolonies.api.sounds.EventType;
import no.monopixel.slimcolonies.api.util.CompatibilityUtils;
import no.monopixel.slimcolonies.api.util.SoundUtils;
import no.monopixel.slimcolonies.api.util.WorldUtil;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.modules.AbstractAssignedCitizenModule;
import no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingModules;
import no.monopixel.slimcolonies.core.entity.citizen.EntityCitizen;
import no.monopixel.slimcolonies.core.entity.pathfinding.navigation.EntityNavigationUtils;
import no.monopixel.slimcolonies.core.network.messages.client.SleepingParticleMessage;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static no.monopixel.slimcolonies.api.util.constant.CitizenConstants.RANGE_TO_BE_HOME;
import static no.monopixel.slimcolonies.core.entity.ai.minimal.EntityAISleep.SleepState.*;

/**
 * AI to send Entity to sleep.
 */
public class EntityAISleep implements IStateAI
{
    /**
     * Interval between sleeping particles
     */
    private static final int TICK_INTERVAL = 30;

    /**
     * Chance to play goHomeSound.
     */
    private static final int CHANCE = 33;

    /**
     * Max ticks of putting the citizen to bed.
     */
    private static final int MAX_BED_TICKS = 10;

    /**
     * The citizen.
     */
    private final EntityCitizen citizen;

    /**
     * Bed the citizen is using atm.
     */
    private BlockPos usedBed = null;

    /**
     * Ticks of putting the citizen into bed.
     */
    private int bedTicks = 0;

    public enum SleepState implements IState
    {
        WALKING_HOME,
        FIND_BED,
        SLEEPING;
    }

    /**
     * Initiate the sleep task.
     *
     * @param citizen the citizen which should sleep.
     */
    public EntityAISleep(final EntityCitizen citizen)
    {
        this.citizen = citizen;
        // 100 blocks - 30 seconds - straight line
        citizen.getCitizenAI().addTransition(new TickingTransition<>(CitizenAIState.SLEEP, () -> true, this::checkSleep, 20));

        citizen.getCitizenAI().addTransition(new TickingTransition<>(WALKING_HOME, () -> true, this::walkHome, 30));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(FIND_BED, this::findBed, () -> SLEEPING, 30));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(SLEEPING, () -> true, this::sleep, TICK_INTERVAL));
    }

    /**
     * Checks for sleep
     *
     */
    private IState checkSleep()
    {
        initAI();
        return WALKING_HOME;
    }

    /**
     * Walking to the home/bed position
     *
     */
    private IState walkHome()
    {
        final IBuilding homeBuilding = citizen.getCitizenData().getHomeBuilding();
        if (homeBuilding == null)
        {
            @Nullable final BlockPos homePosition = citizen.getCitizenData().getHomePosition();
            if (homePosition.distSqr(BlockPos.containing(Math.floor(citizen.getX()), citizen.getY(), Math.floor(citizen.getZ()))) <= RANGE_TO_BE_HOME)
            {
                return FIND_BED;
            }
        }
        else if (homeBuilding.isInBuilding(citizen.blockPosition()))
        {
            return FIND_BED;
        }

        citizen.getCitizenData().setVisibleStatus(VisibleCitizenStatus.SLEEP);
        goHome();
        return WALKING_HOME;
    }

    /**
     * Tries to find a fitting bed, or timeouts
     *
     * @return true if continue to sleep
     */
    private boolean findBed()
    {
        if (!citizen.getCitizenSleepHandler().isAsleep() && bedTicks < MAX_BED_TICKS)
        {
            findBedAndTryToSleep();
            return false;
        }
        return true;
    }

    /**
     * On init set his status to sleeping.
     */
    public void initAI()
    {
        usedBed = null;
    }

    private void findBedAndTryToSleep()
    {
        final IColony colony = citizen.getCitizenColonyHandler().getColonyOrRegister();
        if (colony != null && citizen.getCitizenData().getHomeBuilding() instanceof AbstractBuilding hut)
        {
            final BlockPos homePos = citizen.getCitizenData().getHomePosition();
            if (usedBed == null || usedBed == homePos)
            {
                final List<BlockPos> bedList = hut.getModule(BuildingModules.BED).getRegisteredBlocks();
                final int index = hut.getFirstModuleOccurance(AbstractAssignedCitizenModule.class).getAssignedCitizen().indexOf(citizen.getCitizenData());

                if (index < bedList.size())
                {
                    final BlockPos pos = bedList.get(index);
                    if (WorldUtil.isEntityBlockLoaded(citizen.level, pos))
                    {
                        final Level world = citizen.level;
                        final BlockState state = world.getBlockState(pos);
                        final BlockState above = world.getBlockState(pos.above());

                        if (!state.is(BlockTags.BEDS))
                        {
                            hut.getModule(BuildingModules.BED).removeBed(pos);
                        }
                        else if (!state.getValue(BedBlock.OCCUPIED)
                            && state.getValue(BedBlock.PART).equals(BedPart.HEAD)
                            && (above.is(BlockTags.BEDS) || above.getBlock() instanceof PanelBlock || above.getBlock() instanceof TrapDoorBlock || !above.isSolid()))
                        {
                            usedBed = pos;
                            return;
                        }
                    }
                }

                // Fallback to home position if assigned bed is not available
                if (usedBed == null)
                {
                    usedBed = homePos;
                }
            }

            if (EntityNavigationUtils.walkToPosInBuilding(citizen, usedBed, citizen.getCitizenData().getHomeBuilding(), 12))
            {
                bedTicks++;
                if (!citizen.getCitizenSleepHandler().trySleep(usedBed))
                {
                    citizen.getCitizenData().setBedPos(BlockPos.ZERO);
                    usedBed = null;
                }
            }
            else
            {
                bedTicks = 0;
            }
        }
    }

    /**
     * Make sleeping
     */
    private IState sleep()
    {
        if (usedBed != null)
        {
            if (usedBed.distSqr(citizen.blockPosition()) > 3 * 3)
            {
                return WALKING_HOME;
            }
            citizen.setPose(Pose.SLEEPING);
        }
        else
        {
            findBedAndTryToSleep();
        }

        Network.getNetwork().sendToTrackingEntity(new SleepingParticleMessage(citizen.getX(), citizen.getY() + 1.0d, citizen.getZ()), citizen);
        //TODO make sleeping noises here.
        return null;
    }

    /**
     * While going home play a goHome sound for the specific worker by chance.
     */
    private void goHome()
    {
        final IBuilding home = citizen.getCitizenData().getHomeBuilding();
        if (home != null)
        {
            EntityNavigationUtils.walkToBuilding(citizen, home);
        }
        else
        {
            EntityNavigationUtils.walkToPos(citizen, citizen.getCitizenData().getHomePosition(), 4, true);
        }

        final int chance = citizen.getRandom().nextInt(CHANCE);
        if (chance <= 1 && citizen.getCitizenColonyHandler().getWorkBuilding() != null && citizen.getCitizenJobHandler().getColonyJob() != null)
        {
            SoundUtils.playSoundAtCitizenWith(CompatibilityUtils.getWorldFromCitizen(citizen), citizen.blockPosition(), EventType.OFF_TO_BED, citizen.getCitizenData());
            //add further workers as soon as available.
        }
    }
}
