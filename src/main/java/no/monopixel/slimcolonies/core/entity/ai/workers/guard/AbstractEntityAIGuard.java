package no.monopixel.slimcolonies.core.entity.ai.workers.guard;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.IGuardBuilding;
import no.monopixel.slimcolonies.api.colony.jobs.ModJobs;
import no.monopixel.slimcolonies.api.colony.permissions.Action;
import no.monopixel.slimcolonies.api.entity.ai.combat.CombatAIStates;
import no.monopixel.slimcolonies.api.entity.ai.combat.threat.IThreatTableEntity;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.AIOneTimeEventTarget;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.AITarget;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.IAIState;
import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.api.entity.citizen.Skill;
import no.monopixel.slimcolonies.api.equipment.registry.EquipmentTypeEntry;
import no.monopixel.slimcolonies.api.util.*;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuildingGuards;
import no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingModules;
import no.monopixel.slimcolonies.core.colony.buildings.modules.EntityListModule;
import no.monopixel.slimcolonies.core.colony.buildings.modules.settings.GuardTaskSetting;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingMiner;
import no.monopixel.slimcolonies.core.colony.jobs.AbstractJobGuard;
import no.monopixel.slimcolonies.core.entity.ai.workers.util.MinerLevel;
import no.monopixel.slimcolonies.core.entity.citizen.EntityCitizen;
import no.monopixel.slimcolonies.core.entity.other.SittingEntity;
import no.monopixel.slimcolonies.core.entity.pathfinding.navigation.EntityNavigationUtils;
import no.monopixel.slimcolonies.core.network.messages.client.SleepingParticleMessage;
import no.monopixel.slimcolonies.core.util.TeleportHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

import static no.monopixel.slimcolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static no.monopixel.slimcolonies.api.research.util.ResearchConstants.FLEEING_SPEED;
import static no.monopixel.slimcolonies.api.research.util.ResearchConstants.RETREAT;
import static no.monopixel.slimcolonies.api.util.constant.Constants.*;
import static no.monopixel.slimcolonies.api.util.constant.GuardConstants.GUARD_FOLLOW_LOSE_RANGE;
import static no.monopixel.slimcolonies.api.util.constant.GuardConstants.GUARD_FOLLOW_TIGHT_RANGE;
import static no.monopixel.slimcolonies.core.colony.buildings.AbstractBuildingGuards.HOSTILE_LIST;

/**
 * Class taking of the abstract guard methods for all fighting AIs.
 *
 * @param <J> the generic job.
 */
public abstract class AbstractEntityAIGuard<J extends AbstractJobGuard<J>, B extends AbstractBuildingGuards> extends AbstractEntityAIFight<J, B>
{
    /**
     * Entities to kill before dumping into chest.
     */
    private static final int ACTIONS_UNTIL_DUMPING = 5;

    /**
     * Max derivation of current position when patrolling.
     */
    private static final int MAX_PATROL_DERIVATION = 80;

    /**
     * Max derivation of current position when following..
     */
    private static final int MAX_FOLLOW_DERIVATION = 30;

    /**
     * Max derivation of current position when guarding.
     */
    private static final int MAX_GUARD_DERIVATION = 10;

    /**
     * The amount of time the guard counts as in combat after last combat action
     */
    protected static final int COMBAT_TIME = 30 * 20;

    /**
     * The current target for our guard.
     */
    protected LivingEntity target = null;

    /**
     * The current blockPos we're patrolling at.
     */
    private BlockPos currentPatrolPoint = null;

    /**
     * The guard building assigned to this job.
     */
    protected final IGuardBuilding buildingGuards;

    /**
     * The interval between sleeping particles
     */
    private static final int PARTICLE_INTERVAL = 30;

    /**
     * Interval between sleep checks
     */
    private static final int SHOULD_SLEEP_INTERVAL = 200;

    /**
     * Interval between guard task updates
     */
    private static final int GUARD_TASK_INTERVAL = 100;

    /**
     * Interval between guard regen updates
     */
    private static final int GUARD_REGEN_INTERVAL = 40;

    /**
     * Amount of regular actions before the action counter is increased
     */
    private static final int ACTION_INCREASE_INTERVAL = 10;

    /**
     * The timer for sleeping.
     */
    private int sleepTimer = 0;

    /**
     * Timer for the wakeup AI.
     */
    protected int wakeTimer = 0;

    /**
     * Timer for fighting, goes down to 0 when hasnt been fighting for a while
     */
    protected int fighttimer = 0;

    /**
     * The sleeping guard we found
     */
    protected WeakReference<EntityCitizen> sleepingGuard = new WeakReference<>(null);

    /**
     * Small timer for increasing actions done for continuous actions
     */
    private int regularActionTimer = 0;

    /**
     * The last position a guard did some guard task on
     */
    private BlockPos lastGuardActionPos;

    public AbstractEntityAIGuard(@NotNull final J job)
    {
        super(job);
        super.registerTargets(
            new AITarget(DECIDE, CombatAIStates.NO_TARGET, 1),
            new AITarget(CombatAIStates.NO_TARGET, this::shouldSleep, () -> GUARD_SLEEP, SHOULD_SLEEP_INTERVAL),
            new AITarget(GUARD_SLEEP, this::sleep, 1),
            new AITarget(GUARD_SLEEP, this::sleepParticles, PARTICLE_INTERVAL),
            new AITarget(GUARD_REGEN, this::regen, GUARD_REGEN_INTERVAL),
            new AITarget(GUARD_FLEE, this::flee, 20),
            new AITarget(CombatAIStates.ATTACKING, this::shouldFlee, () -> GUARD_FLEE, GUARD_REGEN_INTERVAL),
            new AITarget(CombatAIStates.NO_TARGET, this::shouldFlee, () -> GUARD_FLEE, GUARD_REGEN_INTERVAL),
            new AITarget(CombatAIStates.NO_TARGET, this::decide, GUARD_TASK_INTERVAL),
            new AITarget(GUARD_WAKE, this::wakeUpGuard, TICKS_SECOND),

            new AITarget(CombatAIStates.ATTACKING, this::inCombat, 8)
        );

        buildingGuards = building;
        lastGuardActionPos = buildingGuards.getPosition();
    }

    /**
     * Updates fight timer during combat
     */
    private IAIState inCombat()
    {
        if (fighttimer <= 0)
        {
            onCombatEnter();
        }

        if (!hasTool())
        {
            return PREPARING;
        }

        fighttimer = COMBAT_TIME;
        return null;
    }

    /**
     * On combat enter
     */
    private void onCombatEnter()
    {
        worker.setCanBeStuck(false);
        worker.getNavigation().getPathingOptions().setCanUseRails(false);
    }

    /**
     * On combat leave
     */
    private void onCombatLeave()
    {
        worker.getNavigation().getPathingOptions().setCanUseRails(((EntityCitizen) worker).canPathOnRails());
        worker.setCanBeStuck(true);
    }

    /**
     * Wake up a nearby sleeping guard
     *
     * @return next state
     */
    private IAIState wakeUpGuard()
    {
        if (sleepingGuard.get() == null || !(sleepingGuard.get().getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard) || !sleepingGuard.get()
            .getCitizenJobHandler()
            .getColonyJob(AbstractJobGuard.class)
            .isAsleep())
        {
            return CombatAIStates.NO_TARGET;
        }

        wakeTimer++;
        if (wakeTimer > 30)
        {
            return CombatAIStates.NO_TARGET;
        }

        final EntityCitizen sleepingCitizen = sleepingGuard.get();

        // Move into range
        if (BlockPosUtil.getDistanceSquared(sleepingCitizen.blockPosition(), worker.blockPosition()) > 2.25)
        {
            walkToUnSafePos(sleepingCitizen.blockPosition());
        }
        else
        {
            worker.swing(InteractionHand.OFF_HAND);
            sleepingCitizen.hurt(world.damageSources().source(DamageSourceKeys.WAKEY, this.worker), 1);
            sleepingCitizen.setLastHurtByMob(worker);
            return CombatAIStates.NO_TARGET;
        }

        return getState();
    }

    /**
     * Whether the guard should fall asleep.
     *
     * @return true if so
     */
    private boolean shouldSleep()
    {
        if (worker.getLastHurtByMob() != null || target != null || fighttimer > 0)
        {
            return false;
        }

        if (!WorldUtil.isDayTime(worker.level))
        {
            return false;
        }

        // Checked every 10 seconds. Base chance: 1 in 40. Higher Adaptability = less sleep needed
        // Level 0: 1/40, Level 10: 1/45, Level 20: 1/50
        if (worker.getRandom().nextInt((int) (worker.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Adaptability) * 0.5) + 40) == 1)
        {
            // Sleep for 2500-3000 ticks
            sleepTimer = worker.getRandom().nextInt(500) + 2500;
            worker.getNavigation().stop();
            SittingEntity.sitDown(worker.blockPosition(), worker, sleepTimer);

            return true;
        }

        return false;
    }

    /**
     * Emits sleeping particles and regens hp when asleep
     *
     * @return the next state to go into
     */
    private IAIState sleepParticles()
    {
        Network.getNetwork().sendToTrackingEntity(new SleepingParticleMessage(worker.getX(), worker.getY() + 2.0d, worker.getZ()), worker);

        if (worker.getHealth() < worker.getMaxHealth())
        {
            worker.setHealth(worker.getHealth() + 0.5f);
        }

        return null;
    }

    /**
     * Sleep activity
     *
     * @return the next state to go into
     */
    private IAIState sleep()
    {
        if (worker.getLastHurtByMob() != null || (sleepTimer -= getTickRate()) < 0)
        {
            stopSleeping();
            ((EntityCitizen) worker).getThreatTable().removeCurrentTarget();
            worker.setLastHurtByMob(null);
            return CombatAIStates.NO_TARGET;
        }

        worker.getLookControl()
            .setLookAt(worker.getX() + worker.getDirection().getStepX(),
                worker.getY() + worker.getDirection().getStepY(),
                worker.getZ() + worker.getDirection().getStepZ(),
                0f,
                30f);
        ((LookHandler) worker.getLookControl()).setLookAtCooldown(sleepTimer);
        return null;
    }

    /**
     * Stops the guard from sleeping
     */
    private void stopSleeping()
    {
        if (getState() == GUARD_SLEEP)
        {
            worker.stopRiding();
            worker.setPos(worker.getX(), worker.getY() + 1, worker.getZ());
            worker.getCitizenExperienceHandler().addExperience(1);
            ((LookHandler) worker.getLookControl()).setLookAtCooldown(2);
        }
    }

    /**
     * Whether the guard should flee
     *
     * @return
     */
    private boolean shouldFlee()
    {
        if (buildingGuards.shallRetrieveOnLowHealth() && worker.getHealth() < ((int) worker.getMaxHealth() * 0.2D) && worker.distanceToSqr(building.getID().getCenter()) > 20)
        {
            return worker.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(RETREAT) > 0;
        }

        return false;
    }

    /**
     * Regen at the building and continue when more than half health.
     *
     * @return next state to go to.
     */
    private IAIState regen()
    {
        if (((EntityCitizen) worker).getThreatTable().getTargetMob() != null && ((EntityCitizen) worker).getThreatTable().getTargetMob().distanceTo(worker) < 10)
        {
            return CombatAIStates.ATTACKING;
        }

        if (worker.getHealth() < ((int) worker.getMaxHealth() * 0.75D) && buildingGuards.shallRetrieveOnLowHealth())
        {
            if (!worker.hasEffect(MobEffects.REGENERATION))
            {
                worker.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200));
            }
            return GUARD_REGEN;
        }

        return START_WORKING;
    }

    /**
     * Flee to the building.
     *
     * @return next state to go to.
     */
    private IAIState flee()
    {
        if (!worker.hasEffect(MobEffects.MOVEMENT_SPEED))
        {
            final double effect = worker.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(FLEEING_SPEED);
            if (effect > 0)
            {
                worker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, (int) (0 + effect)));
            }
        }

        if (!walkToBuilding())
        {
            return GUARD_FLEE;
        }

        return GUARD_REGEN;
    }

    /**
     * Guard at a specific position.
     *
     * @return the next state to run into.
     */
    private IAIState guard()
    {
        guardMovement();
        return getState();
    }

    /**
     * Movement when guarding
     */
    public void guardMovement()
    {
        walkToSafePos(buildingGuards.getGuardPos(worker));
    }

    /**
     * Follow a player.
     *
     * @return the next state to run into.
     */
    private IAIState follow()
    {
        if (BlockPosUtil.getDistance2D(worker.blockPosition(), buildingGuards.getPositionToFollow()) > MAX_FOLLOW_DERIVATION)
        {
            TeleportHelper.teleportCitizen(worker, worker.getCommandSenderWorld(), buildingGuards.getPositionToFollow());
            return null;
        }

        walkToUnSafePos(buildingGuards.getPositionToFollow(), buildingGuards.isTightGrouping() ? GUARD_FOLLOW_TIGHT_RANGE : GUARD_FOLLOW_LOSE_RANGE);
        return null;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return ACTIONS_UNTIL_DUMPING * building.getBuildingLevelEquivalent();
    }

    @Override
    protected IAIState startWorkingAtOwnBuilding()
    {
        if (buildingGuards != null)
        {
            buildingGuards.setTempNextPatrolPoint(buildingGuards.getPosition());
        }
        return DECIDE;
    }

    /**
     * Patrol between a list of patrol points.
     *
     * @return the next patrol point to go to.
     */
    public IAIState patrol()
    {
        if (buildingGuards.requiresManualTarget())
        {
            if (currentPatrolPoint == null || walkToSafePos(currentPatrolPoint))
            {
                currentPatrolPoint = null;
                if (!EntityNavigationUtils.walkToRandomPos(worker, 20, 1.0))
                {
                    return getState();
                }

                if (worker.getRandom().nextInt(5) <= 1)
                {
                    currentPatrolPoint = buildingGuards.getColony().getBuildingManager().getRandomBuilding(b -> true);
                    if (currentPatrolPoint != null)
                    {
                        walkToSafePos(currentPatrolPoint);
                    }
                }
            }
        }
        else
        {
            currentPatrolPoint = buildingGuards.getNextPatrolTarget(false);
            if (currentPatrolPoint != null && (walkToSafePos(currentPatrolPoint)))
            {
                setCurrentDelay(10);
                buildingGuards.arrivedAtPatrolPoint(worker);
            }
        }
        return null;
    }

    /**
     * Patrol between all completed nodes in the assigned mine
     *
     * @return the next point to patrol to
     */
    public IAIState patrolMine()
    {
        if (buildingGuards.getMinePos() == null)
        {
            return PREPARING;
        }
        if (currentPatrolPoint == null || walkToSafePos(currentPatrolPoint))
        {
            final IBuilding building = buildingGuards.getColony().getBuildingManager().getBuilding(buildingGuards.getMinePos());
            if (building != null)
            {
                if (building instanceof BuildingMiner)
                {
                    final BuildingMiner buildingMiner = (BuildingMiner) building;
                    final MinerLevel level = buildingMiner.getModule(BuildingModules.MINER_LEVELS).getCurrentLevel();
                    if (level == null)
                    {
                        setNextPatrolTarget(buildingMiner.getPosition());
                    }
                    else
                    {
                        setNextPatrolTarget(level.getRandomCompletedNode(buildingMiner));
                    }
                }
                else
                {
                    buildingGuards.getModule(BuildingModules.GUARD_SETTINGS).getSetting(AbstractBuildingGuards.GUARD_TASK).set(GuardTaskSetting.PATROL);
                }
            }
            else
            {
                buildingGuards.getModule(BuildingModules.GUARD_SETTINGS).getSetting(AbstractBuildingGuards.GUARD_TASK).set(GuardTaskSetting.PATROL);
            }
        }
        return null;
    }

    /**
     * Sets the next patrol target.
     *
     * @param target the next patrol target.
     */
    private void setNextPatrolTarget(final BlockPos target)
    {
        currentPatrolPoint = target;
    }

    /**
     * Check if the worker has the required tool to fight.
     *
     * @return true if so.
     */
    public boolean hasTool()
    {
        for (final EquipmentTypeEntry toolType : toolsNeeded)
        {
            if (!InventoryUtils.hasItemHandlerEquipmentWithLevel(getInventory(), toolType, 0, Integer.MAX_VALUE))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Assigning the guard to help a citizen.
     *
     * @param attacker the citizens attacker.
     */
    public void startHelpCitizen(final LivingEntity attacker)
    {
        if (canHelp(attacker.blockPosition()))
        {
            ((IThreatTableEntity) worker).getThreatTable().addThreat(attacker, 20);
            registerTarget(new AIOneTimeEventTarget(CombatAIStates.ATTACKING));
        }
    }

    /**
     * Check if we can help a citizen
     *
     * @param pos
     * @return true if not fighting/helping already
     */
    public boolean canHelp(final BlockPos pos)
    {
        if ((getState() == CombatAIStates.NO_TARGET || getState() == GUARD_SLEEP) && canBeInterrupted())
        {
            if (buildingGuards.getTask().equals(GuardTaskSetting.GUARD) && !isWithinPersecutionDistance(pos, getPersecutionDistance()))
            {
                return false;
            }

            // Stop sleeping when someone called for help
            stopSleeping();
            return true;
        }
        return false;
    }

    /**
     * Decide what we should do next! Ticked once every GUARD_TASK_INTERVAL Ticks
     *
     * @return the next IAIState.
     */
    protected IAIState decide()
    {

        if (regularActionTimer++ > ACTION_INCREASE_INTERVAL)
        {
            incrementActionsDone();
            regularActionTimer = 0;
        }

        if (worker.getRandom().nextDouble() < 0.05)
        {
            equipInventoryArmor();
        }

        if (!hasTool())
        {
            return PREPARING;
        }

        if (fighttimer > 0)
        {
            fighttimer -= GUARD_TASK_INTERVAL;
            if (fighttimer <= 0)
            {
                onCombatLeave();
            }
        }
        else
        {
            worker.stopUsingItem();
            lastGuardActionPos = worker.blockPosition();
        }

        if (buildingGuards.getTask().equals(GuardTaskSetting.FOLLOW))
        {
            worker.addEffect(new MobEffectInstance(GLOW_EFFECT, GLOW_EFFECT_DURATION, GLOW_EFFECT_MULTIPLIER, false, false));
        }
        else
        {
            worker.removeEffect(GLOW_EFFECT);
        }


        return switch (buildingGuards.getTask())
        {
            case GuardTaskSetting.PATROL -> patrol();
            case GuardTaskSetting.GUARD -> guard();
            case GuardTaskSetting.FOLLOW -> follow();
            case GuardTaskSetting.PATROL_MINE -> patrolMine();
            default -> PREPARING;
        };
    }

    /**
     * Check if a position is within the allowed persecution distance.
     *
     * @param entityPos the position to check.
     * @return true if so.
     */
    public boolean isWithinPersecutionDistance(final BlockPos entityPos, final double attackRange)
    {
        return BlockPosUtil.getDistanceSquared(getTaskReferencePoint(), entityPos) <= Math.pow(getPersecutionDistance() + attackRange, 2);
    }

    /**
     * Get the reference point from which the guard comes.
     *
     * @return the position depending ont he task.
     */
    private BlockPos getTaskReferencePoint()
    {
        switch (buildingGuards.getTask())
        {
            case GuardTaskSetting.PATROL:
            case GuardTaskSetting.PATROL_MINE:
                return lastGuardActionPos;
            case GuardTaskSetting.FOLLOW:
                return buildingGuards.getPositionToFollow();
            default:
                return buildingGuards.getGuardPos(worker);
        }
    }

    /**
     * Returns the block distance at which a guard should chase his target
     *
     * @return the block distance at which a guard should chase his target
     */
    private int getPersecutionDistance()
    {
        switch (buildingGuards.getTask())
        {
            case GuardTaskSetting.PATROL:
                return MAX_PATROL_DERIVATION;
            case GuardTaskSetting.PATROL_MINE:
            case GuardTaskSetting.FOLLOW:
                return MAX_FOLLOW_DERIVATION;
            default:
                return MAX_GUARD_DERIVATION + (getModuleForJob().getJobEntry() == ModJobs.knight.get() ? 20 : 0);
        }
    }

    @Override
    public boolean canBeInterrupted()
    {
        if (fighttimer > 0 || getState() == CombatAIStates.ATTACKING || worker.getLastAttacker() != null || buildingGuards.getTask()
            .equals(GuardTaskSetting.FOLLOW))
        {
            return false;
        }
        return super.canBeInterrupted();
    }

    /**
     * Set the citizen to wakeup
     *
     */
    public void setWakeCitizen(final EntityCitizen citizen)
    {
        sleepingGuard = new WeakReference<>(citizen);
        wakeTimer = 0;
        registerTarget(new AIOneTimeEventTarget(GUARD_WAKE));
    }

    @Override
    public Class<B> getExpectedBuildingClass()
    {
        return (Class<B>) AbstractBuildingGuards.class;
    }

    /**
     * Check whether the target is attackable
     *
     */
    public static boolean isAttackableTarget(final AbstractEntityCitizen user, final LivingEntity entity)
    {
        if (IColonyManager.getInstance().getCompatibilityManager().getAllMonsters().contains(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType())) && !user.getCitizenData()
            .getWorkBuilding()
            .getModuleMatching(
                EntityListModule.class,
                m -> m.getId()
                    .equals(
                        HOSTILE_LIST))
            .isEntityInList(
                ForgeRegistries.ENTITY_TYPES.getKey(
                    entity.getType())))
        {
            return true;
        }

        final IColony colony = user.getCitizenColonyHandler().getColonyOrRegister();
        if (colony == null)
        {
            return false;
        }

        // Players
        if (entity instanceof Player && (colony.getPermissions().hasPermission((Player) entity, Action.GUARDS_ATTACK)
            || colony.isValidAttackingPlayer((Player) entity)))
        {
            return true;
        }

        // Other colonies guard citizen attacking the colony
        if (entity instanceof EntityCitizen otherCitizen && otherCitizen.getCitizenColonyHandler().getColonyId() != colony.getID()
            && colony.isValidAttackingGuard((AbstractEntityCitizen) entity))
        {
            return true;
        }

        return false;
    }
}
