package no.monopixel.slimcolonies.core.entity.visitor;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import no.monopixel.slimcolonies.api.colony.*;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.permissions.Action;
import no.monopixel.slimcolonies.api.colony.requestsystem.StandardFactoryController;
import no.monopixel.slimcolonies.api.colony.requestsystem.location.ILocation;
import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.api.entity.citizen.citizenhandlers.*;
import no.monopixel.slimcolonies.api.inventory.InventoryCitizen;
import no.monopixel.slimcolonies.api.inventory.container.ContainerCitizenInventory;
import no.monopixel.slimcolonies.api.util.*;
import no.monopixel.slimcolonies.api.util.MessageUtils.MessagePriority;
import no.monopixel.slimcolonies.api.util.constant.TypeConstants;
import no.monopixel.slimcolonies.core.MineColonies;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.client.gui.WindowInteraction;
import no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingModules;
import no.monopixel.slimcolonies.core.colony.buildings.modules.TavernBuildingModule;
import no.monopixel.slimcolonies.core.entity.ai.minimal.EntityAIInteractToggleAble;
import no.monopixel.slimcolonies.core.entity.ai.minimal.LookAtEntityGoal;
import no.monopixel.slimcolonies.core.entity.ai.minimal.LookAtEntityInteractGoal;
import no.monopixel.slimcolonies.core.entity.ai.visitor.EntityAIVisitor;
import no.monopixel.slimcolonies.core.entity.citizen.EntityCitizen;
import no.monopixel.slimcolonies.core.entity.citizen.citizenhandlers.CitizenExperienceHandler;
import no.monopixel.slimcolonies.core.entity.citizen.citizenhandlers.CitizenInventoryHandler;
import no.monopixel.slimcolonies.core.entity.citizen.citizenhandlers.CitizenJobHandler;
import no.monopixel.slimcolonies.core.entity.citizen.citizenhandlers.CitizenSleepHandler;
import no.monopixel.slimcolonies.core.entity.pathfinding.navigation.MovementHandler;
import no.monopixel.slimcolonies.core.network.messages.client.ItemParticleEffectMessage;
import no.monopixel.slimcolonies.core.network.messages.server.colony.OpenInventoryMessage;
import no.monopixel.slimcolonies.core.util.citizenutils.CitizenItemUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static no.monopixel.slimcolonies.api.util.ItemStackUtils.ISFOOD;
import static no.monopixel.slimcolonies.api.util.constant.CitizenConstants.TICKS_20;
import static no.monopixel.slimcolonies.api.util.constant.Constants.*;
import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_CITIZEN;
import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_COLONY_ID;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.MESSAGE_INFO_COLONY_VISITOR_DIED;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.MESSAGE_INTERACTION_VISITOR_FOOD;
import static no.monopixel.slimcolonies.core.entity.ai.minimal.EntityAIInteractToggleAble.*;

/**
 * Visitor citizen entity
 */
public class VisitorCitizen extends AbstractEntityCitizen
{
    /**
     * The citizen experience handler
     */
    private ICitizenExperienceHandler citizenExperienceHandler;

    /**
     * It's citizen Id.
     */
    private int          citizenId = 0;
    /**
     * Reference to the data representation inside the colony.
     */
    @Nullable
    private ICitizenData citizenData;

    /**
     * The citizen inv handler.
     */
    private ICitizenInventoryHandler citizenInventoryHandler;

    /**
     * The citizen colony handler.
     */
    private ICitizenColonyHandler citizenColonyHandler;
    /**
     * The citizen job handler.
     */
    private ICitizenJobHandler    citizenJobHandler;

    /**
     * The citizen sleep handler.
     */
    private ICitizenSleepHandler citizenSleepHandler;

    /**
     * Citizen data view.
     */
    private ICitizenDataView citizenDataView;

    /**
     * The location used for requests
     */
    private ILocation location = null;

    /**
     * Constructor for a new citizen typed entity.
     *
     * @param type  the Entity type.
     * @param world the world.
     */
    public VisitorCitizen(final EntityType<? extends PathfinderMob> type, final Level world)
    {
        super(type, world);
        this.citizenInventoryHandler = new CitizenInventoryHandler(this);
        this.citizenColonyHandler = new VisitorColonyHandler(this);
        this.citizenJobHandler = new CitizenJobHandler(this);
        this.citizenSleepHandler = new CitizenSleepHandler(this);
        this.citizenExperienceHandler = new CitizenExperienceHandler(this);

        this.moveControl = new MovementHandler(this);
        this.setPersistenceRequired();
        this.setCustomNameVisible(MineColonies.getConfig().getServer().alwaysRenderNameTag.get());
        initTasks();
    }

    private void initTasks()
    {
        int priority = 0;
        this.goalSelector.addGoal(priority, new FloatGoal(this));
        this.goalSelector.addGoal(++priority, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(priority, new EntityAIInteractToggleAble(this, FENCE_TOGGLE, TRAP_TOGGLE, DOOR_TOGGLE));
        this.goalSelector.addGoal(++priority, new LookAtEntityInteractGoal(this, Player.class, WATCH_CLOSEST2, 0.2F));
        this.goalSelector.addGoal(++priority, new LookAtEntityInteractGoal(this, EntityCitizen.class, WATCH_CLOSEST2_FAR, WATCH_CLOSEST2_FAR_CHANCE));
        this.goalSelector.addGoal(++priority, new LookAtEntityGoal(this, LivingEntity.class, WATCH_CLOSEST));
        new EntityAIVisitor(this);
    }

    @Override
    public ILocation getLocation()
    {
        if (location == null)
        {
            location = StandardFactoryController.getInstance().getNewInstance(TypeConstants.ILOCATION, this);
        }
        return location;
    }

    @Override
    public boolean hurt(@NotNull final DamageSource damageSource, final float damage)
    {
        if (!(damageSource.getEntity() instanceof EntityCitizen) && super.hurt(damageSource, damage))
        {
            if (damageSource.getEntity() instanceof LivingEntity && damage > 1.01f)
            {
                final IBuilding home = getCitizenData().getHomeBuilding();
                if (home != null && home.hasModule(BuildingModules.TAVERN_VISITOR))
                {
                    final TavernBuildingModule module = home.getModule(BuildingModules.TAVERN_VISITOR);
                    for (final Integer id : module.getExternalCitizens())
                    {
                        ICitizenData data = citizenColonyHandler.getColonyOrRegister().getVisitorManager().getCivilian(id);
                        if (data != null && data.getEntity().isPresent() && data.getEntity().get().getLastHurtByMob() == null)
                        {
                            data.getEntity().get().setLastHurtByMob((LivingEntity) damageSource.getEntity());
                        }
                    }
                }

                final Entity sourceEntity = damageSource.getEntity();
                if (sourceEntity instanceof Player)
                {
                    if (sourceEntity instanceof ServerPlayer)
                    {
                        return damage <= 1 || getCitizenColonyHandler().getColonyOrRegister().getPermissions().hasPermission((Player) sourceEntity, Action.HURT_VISITOR);
                    }
                    else
                    {
                        final IColonyView colonyView = IColonyManager.getInstance().getColonyView(getCitizenColonyHandler().getColonyId(), level.dimension());
                        return damage <= 1 || colonyView == null || colonyView.getPermissions().hasPermission((Player) sourceEntity, Action.HURT_VISITOR);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public ICitizenData getCitizenData()
    {
        return citizenData;
    }

    @Override
    public ICivilianData getCivilianData()
    {
        return citizenData;
    }

    @Override
    public void setCivilianData(@Nullable final ICivilianData data)
    {
        if (data != null && data instanceof IVisitorData)
        {
            this.citizenData = (IVisitorData) data;
            data.initEntityValues();
        }
    }

    /**
     * Return this citizens inventory.
     *
     * @return the inventory this citizen has.
     */
    @Override
    @NotNull
    public InventoryCitizen getInventoryCitizen()
    {
        return getCitizenData().getInventory();
    }

    @Override
    @NotNull
    public IItemHandler getItemHandlerCitizen()
    {
        return getInventoryCitizen();
    }

    /**
     * Mark the citizen dirty to synch the data with the client.
     */
    @Override
    public void markDirty(final int time)
    {
        if (citizenData != null)
        {
            citizenData.markDirty(time);
        }
    }

    @Override
    public void setIsChild(final boolean isChild)
    {

    }

    @Override
    public void playMoveAwaySound()
    {

    }

    @Override
    public void decreaseSaturationForAction()
    {
        if (citizenData != null)
        {
            citizenData.decreaseSaturation(citizenColonyHandler.getPerBuildingFoodCost());
            citizenData.markDirty(20 * 20);
        }
    }

    /**
     * Decrease the saturation of the citizen for 1 action.
     */
    @Override
    public void decreaseSaturationForContinuousAction()
    {
        if (citizenData != null)
        {
            citizenData.decreaseSaturation(citizenColonyHandler.getPerBuildingFoodCost() / 100.0);
            citizenData.markDirty(20 * 60 * 2);
        }
    }

    /**
     * Getter for the citizen id.
     *
     * @return the id.
     */
    @Override
    public int getCivilianID()
    {
        return citizenId;
    }

    /**
     * Setter for the citizen id.
     *
     * @param id the id to set.
     */
    @Override
    public void setCitizenId(final int id)
    {
        this.citizenId = id;
    }

    @Override
    public ICitizenExperienceHandler getCitizenExperienceHandler()
    {
        return citizenExperienceHandler;
    }

    @Override
    public ICitizenInventoryHandler getCitizenInventoryHandler()
    {
        return citizenInventoryHandler;
    }

    @Override
    public void setCitizenInventoryHandler(final ICitizenInventoryHandler citizenInventoryHandler)
    {
        this.citizenInventoryHandler = citizenInventoryHandler;
    }

    @Override
    public ICitizenColonyHandler getCitizenColonyHandler()
    {
        return citizenColonyHandler;
    }

    @Override
    public void setCitizenColonyHandler(final ICitizenColonyHandler citizenColonyHandler)
    {
        this.citizenColonyHandler = citizenColonyHandler;
    }

    @Override
    public ICitizenJobHandler getCitizenJobHandler()
    {
        return citizenJobHandler;
    }

    @Override
    public ICitizenSleepHandler getCitizenSleepHandler()
    {
        return citizenSleepHandler;
    }

    @Override
    public float getRotationYaw()
    {
        return getYRot();
    }

    @Override
    public float getRotationPitch()
    {
        return getXRot();
    }

    @Override
    public boolean isDead()
    {
        return !isAlive();
    }

    @Override
    public void setCitizenSleepHandler(final ICitizenSleepHandler citizenSleepHandler)
    {

    }

    @Override
    public void setCitizenJobHandler(final ICitizenJobHandler citizenJobHandler)
    {
        this.citizenJobHandler = citizenJobHandler;
    }

    @Override
    public void setCitizenExperienceHandler(final ICitizenExperienceHandler citizenExperienceHandler)
    {
        this.citizenExperienceHandler = citizenExperienceHandler;
    }

    @Override
    public void callForHelp(final Entity attacker, final int guardHelpRange)
    {

    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int id, final Inventory playerInventory, final Player playerEntity)
    {
        return new ContainerCitizenInventory(id, playerInventory, citizenColonyHandler.getColonyId(), citizenId);
    }

    /**
     * Called when a player tries to interact with a citizen.
     *
     * @param player which interacts with the citizen.
     * @return If citizen should interact or not.
     */
    @Override
    public InteractionResult checkAndHandleImportantInteractions(final Player player, @NotNull final InteractionHand hand)
    {
        final IColonyView iColonyView = IColonyManager.getInstance().getColonyView(citizenColonyHandler.getColonyId(), player.level.dimension());
        if (iColonyView != null && !iColonyView.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            return InteractionResult.FAIL;
        }

        if (!ItemStackUtils.isEmpty(player.getItemInHand(hand)) && player.getItemInHand(hand).getItem() instanceof NameTagItem)
        {
            return super.checkAndHandleImportantInteractions(player, hand);
        }

        final InteractionResult result = directPlayerInteraction(player, hand);
        if (result != null)
        {
            return result;
        }

        if (CompatibilityUtils.getWorldFromCitizen(this).isClientSide)
        {
            if (player.isShiftKeyDown())
            {
                Network.getNetwork().sendToServer(new OpenInventoryMessage(iColonyView, this.getName().getString(), this.getId()));
            }
            else
            {
                final ICitizenDataView citizenDataView = getCitizenDataView();
                if (citizenDataView != null)
                {
                    new WindowInteraction(citizenDataView).open();
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Direct interaction on right click
     *
     * @param player
     * @param hand
     * @return
     */
    private InteractionResult directPlayerInteraction(final Player player, final InteractionHand hand)
    {
        final ItemStack usedStack = player.getItemInHand(hand);
        if (ISFOOD.test(usedStack))
        {
            if (!level.isClientSide())
            {
                playSound(SoundEvents.GENERIC_EAT, 1.5f, (float) SoundUtils.getRandomPitch(getRandom()));
                Network.getNetwork().sendToTrackingEntity(new ItemParticleEffectMessage(usedStack, getX(), getY(), getZ(), getXRot(), getYRot(), getEyeHeight()), this);
                ItemStackUtils.consumeFood(usedStack, this, player.getInventory());
                MessageUtils.forCitizen(this, MESSAGE_INTERACTION_VISITOR_FOOD).sendTo(player);
            }
            return InteractionResult.CONSUME;
        }
        return null;
    }

    @Override
    public ICitizenDataView getCitizenDataView()
    {
        if (this.citizenDataView == null)
        {
            citizenColonyHandler.updateColonyClient();
            if (citizenColonyHandler.getColonyId() != 0 && citizenId != 0)
            {
                final IColonyView colonyView = IColonyManager.getInstance().getColonyView(citizenColonyHandler.getColonyId(), level.dimension());
                if (colonyView != null)
                {
                    this.citizenDataView = colonyView.getVisitor(citizenId);
                    return this.citizenDataView;
                }
            }
        }
        else
        {
            return this.citizenDataView;
        }

        return null;
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        entityData.define(DATA_COLONY_ID, citizenColonyHandler == null ? 0 : citizenColonyHandler.getColonyId());
        entityData.define(DATA_CITIZEN_ID, citizenId);
    }

    @Override
    public void aiStep()
    {
        super.aiStep();

        if (lastHurtByPlayerTime > 0)
        {
            markDirty(0);
        }

        if (CompatibilityUtils.getWorldFromCitizen(this).isClientSide)
        {
            citizenColonyHandler.updateColonyClient();
            if (citizenColonyHandler.getColonyId() != 0 && citizenId != 0 && getOffsetTicks() % TICKS_20 == 0)
            {
                final IColonyView colonyView = IColonyManager.getInstance().getColonyView(citizenColonyHandler.getColonyId(), level.dimension());
                if (colonyView != null)
                {
                    this.citizenDataView = colonyView.getVisitor(citizenId);
                    getEntityData().set(DATA_STYLE, colonyView.getTextureStyleId());
                }
            }
        }
        else
        {
            citizenColonyHandler.registerWithColony(citizenColonyHandler.getColonyId(), citizenId);
            if (tickCount % 500 == 0)
            {
                this.setCustomNameVisible(MineColonies.getConfig().getServer().alwaysRenderNameTag.get());
            }
        }
    }

    @Override
    public void addAdditionalSaveData(final CompoundTag compound)
    {
        super.addAdditionalSaveData(compound);

        compound.putInt(TAG_COLONY_ID, citizenColonyHandler.getColonyId());
        if (citizenData != null)
        {
            compound.putInt(TAG_CITIZEN, citizenData.getId());
        }
    }

    @Override
    public void readAdditionalSaveData(final CompoundTag compound)
    {
        super.readAdditionalSaveData(compound);

        if (compound.contains(TAG_COLONY_ID))
        {
            citizenColonyHandler.setColonyId(compound.getInt(TAG_COLONY_ID));
            if (compound.contains(TAG_CITIZEN))
            {
                citizenId = compound.getInt(TAG_CITIZEN);
            }
        }
    }

    @Override
    public void die(DamageSource cause)
    {
        super.die(cause);
        if (!level.isClientSide())
        {
            IColony colony = getCitizenColonyHandler().getColonyOrRegister();
            if (colony != null && getCitizenData() != null)
            {
                colony.getVisitorManager().removeCivilian(getCitizenData());
                if (getCitizenData().getHomeBuilding() instanceof TavernBuildingModule)
                {
                    TavernBuildingModule tavern = (TavernBuildingModule) getCitizenData().getHomeBuilding();
                    tavern.setNoVisitorTime(level.getRandom().nextInt(5000) + 30000);
                }

                final String deathLocation = BlockPosUtil.getString(blockPosition());

                MessageUtils.format(MESSAGE_INFO_COLONY_VISITOR_DIED, getCitizenData().getName(), cause.getMsgId(), deathLocation)
                    .withPriority(MessagePriority.DANGER)
                    .sendTo(colony)
                    .forManagers();
            }
        }
    }

    @Override
    protected void dropEquipment()
    {
        //Drop actual inventory
        for (int i = 0; i < getInventoryCitizen().getSlots(); i++)
        {
            final ItemStack itemstack = getCitizenData().getInventory().getStackInSlot(i);
            if (ItemStackUtils.getSize(itemstack) > 0)
            {
                CitizenItemUtils.entityDropItem(this, itemstack);
            }
        }
    }

    @Override
    public void queueSound(final @NotNull SoundEvent soundEvent, final BlockPos pos, final int length, final int repetitions)
    {

    }

    @Override
    public void queueSound(final @NotNull SoundEvent soundEvent, final BlockPos pos, final int length, final int repetitions, final float volume, final float pitch)
    {

    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor)
    {
        super.onSyncedDataUpdated(dataAccessor);
        if (citizenColonyHandler != null)
        {
            citizenColonyHandler.onSyncDataUpdate(dataAccessor);
        }
    }

    @Override
    public void setRemoved(final RemovalReason reason)
    {
        citizenColonyHandler.onCitizenRemoved();
        super.setRemoved(reason);
    }

    @Override
    public int getTeamId()
    {
        return citizenColonyHandler.getColonyId();
    }
}
