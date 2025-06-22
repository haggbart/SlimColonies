package com.minecolonies.core.colony.buildings.workerbuildings;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.workorders.IBuilderWorkOrder;
import com.minecolonies.api.colony.workorders.IServerWorkOrder;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.huts.WindowHutBuilderModule;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.core.colony.buildings.modules.settings.BuilderModeSetting;
import com.minecolonies.core.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.core.colony.buildings.modules.settings.StringSetting;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingBuilderView;
import com.minecolonies.core.colony.jobs.JobBuilder;
import com.minecolonies.core.colony.workorders.WorkOrderBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.EquipmentLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_PURGED_MOBS;

/**
 * The builders building.
 */
public class BuildingBuilder extends AbstractBuildingStructureBuilder
{
    /**
     * Settings key for the building mode.
     */
    public static final ISettingKey<StringSetting> MODE = new SettingKey<>(StringSetting.class, new ResourceLocation(Constants.MOD_ID, "mode"));
    public static final ISettingKey<BuilderModeSetting> BUILDING_MODE = new SettingKey<>(BuilderModeSetting.class, new ResourceLocation(Constants.MOD_ID, "buildmode"));

    /**
     * Both setting options.
     */
    public static final String MANUAL_SETTING = "com.minecolonies.core.builder.setting.manual";
    public static final String AUTO_SETTING = "com.minecolonies.core.builder.setting.automatic";

    /**
     * The job description.
     */
    private static final String BUILDER = "builder";

    /**
     * Check if the builder purged mobs already at this day.
     */
    private boolean purgedMobsToday = false;

    /**
     * Public constructor of the building, creates an object of the building.
     *
     * @param c the colony.
     * @param l the position.
     */
    public BuildingBuilder(final IColony c, final BlockPos l)
    {
        super(c, l);

        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.pickaxe.get(), TOOL_LEVEL_WOOD_OR_GOLD, getMaxEquipmentLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.shovel.get(), TOOL_LEVEL_WOOD_OR_GOLD, getMaxEquipmentLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.axe.get(), TOOL_LEVEL_WOOD_OR_GOLD, getMaxEquipmentLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.hoe.get(), TOOL_LEVEL_WOOD_OR_GOLD, getMaxEquipmentLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.shears.get(), TOOL_LEVEL_WOOD_OR_GOLD, getMaxEquipmentLevel()), new Tuple<>(1, true));
    }

    /**
     * Getter of the schematic name.
     *
     * @return the schematic name.
     */
    @NotNull
    @Override
    public String getSchematicName()
    {
        return BUILDER;
    }

    @Override
    public void onWakeUp()
    {
        this.purgedMobsToday = false;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        this.purgedMobsToday = compound.getBoolean(TAG_PURGED_MOBS);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();
        compound.putBoolean(TAG_PURGED_MOBS, this.purgedMobsToday);
        return compound;
    }

    /**
     * Set if mobs have been purged by this builder at his hut already today.
     *
     * @param purgedMobsToday true if so.
     */
    public void setPurgedMobsToday(final boolean purgedMobsToday)
    {
        this.purgedMobsToday = purgedMobsToday;
    }

    /**
     * Check if the builder has purged the mobs already.
     *
     * @return true if so.
     */
    public boolean hasPurgedMobsToday()
    {
        return purgedMobsToday;
    }

    /**
     * Checks whether the builder should automatically accept build orders.
     *
     * @return false if he should.
     */
    public boolean getManualMode()
    {
        return getSetting(MODE).getValue().equals(MANUAL_SETTING);
    }

    @Override
    public void searchWorkOrder()
    {
        final ICitizenData citizen = getFirstModuleOccurance(WorkerBuildingModule.class).getFirstCitizen();
        if (citizen == null)
        {
            return;
        }

        final List<IServerWorkOrder> list =
            getColony().getWorkManager().getOrderedList(wo -> (wo instanceof IBuilderWorkOrder && ((IBuilderWorkOrder) wo).canBuild(citizen)), getPosition());
        list.sort((a, b) -> {
            if (a.getWorkOrderType() == WorkOrderType.REMOVE)
            {
                return -1;
            }
            if (b.getWorkOrderType() == WorkOrderType.REMOVE)
            {
                return 1;
            }
            return 0;
        });

        final IServerWorkOrder order = list.stream().filter(w -> w.getClaimedBy().equals(getPosition())).findFirst().orElse(null);
        if (order != null)
        {
            citizen.getJob(JobBuilder.class).setWorkOrder(order);
            order.setClaimedBy(getID());
            return;
        }

        if (getManualMode())
        {
            return;
        }

        for (final IServerWorkOrder wo : list)
        {
            double distanceToBuilder = Double.MAX_VALUE;

            if (wo instanceof WorkOrderBuilding workOrderBuilding && wo.getWorkOrderType() != WorkOrderType.REMOVE && !workOrderBuilding.canBuild(citizen))
            {
                continue;
            }

            for (@NotNull final ICitizenData otherBuilder : getColony().getCitizenManager().getCitizens())
            {
                final JobBuilder job = otherBuilder.getJob(JobBuilder.class);

                if (job == null || otherBuilder.getWorkBuilding() == null || citizen.getId() == otherBuilder.getId())
                {
                    continue;
                }

                if (!job.hasWorkOrder() && wo instanceof WorkOrderBuilding workOrderBuilding && workOrderBuilding.canBuild(otherBuilder))
                {
                    final double distance = otherBuilder.getWorkBuilding().getID().distSqr(wo.getLocation());
                    if (distance < distanceToBuilder)
                    {
                        distanceToBuilder = distance;
                    }
                }
            }

            if (citizen.getWorkBuilding().getID().distSqr(wo.getLocation()) < distanceToBuilder)
            {
                citizen.getJob(JobBuilder.class).setWorkOrder(wo);
                wo.setClaimedBy(getID());
                return;
            }
        }
    }

    /**
     * Sets the work order with the given id as the work order for this buildings citizen.
     *
     * @param orderId the id of the work order to select.
     */
    public void setWorkOrder(int orderId)
    {

        final ICitizenData citizen = getModule(BuildingModules.BUILDER_WORK).getFirstCitizen();
        if (citizen == null)
        {
            Log.getLogger().warn("Attemping to assign work order {} at a hut where there is no worker. Colony: {}, Hut at: {}", orderId, getColony().getID(), getLocation());
            return;
        }

        IServerWorkOrder wo = getColony().getWorkManager().getWorkOrder(orderId);
        if (!(wo instanceof IBuilderWorkOrder))
        {
            Log.getLogger().warn("Attempting to assign work order {} which is not meant for builders. Colony: {}, Hut at: {}", orderId, getColony().getID(), getLocation());
            return;
        }

        if (!wo.getClaimedBy().equals(BlockPos.ZERO))
        {
            Log.getLogger().warn("Attempting to assign work order {} which is already claimed somewhere. Colony: {}, Hut at: {}", orderId, getColony().getID(), getLocation());
            return;
        }

        if (citizen.getJob(JobBuilder.class).hasWorkOrder())
        {
            wo.setClaimedBy(getID());
            getColony().getWorkManager().setDirty(true);
            return;
        }

        if (((IBuilderWorkOrder) wo).canBuild(citizen))
        {
            citizen.getJob(JobBuilder.class).setWorkOrder(wo);
            wo.setClaimedBy(getID());
            getColony().getWorkManager().setDirty(true);
            markDirty();
        }
        else 
        {
            Log.getLogger().warn("Attempting to assign work order {} to a builder who cannot build it. Colony: {}, Hut at: {}", orderId, getColony().getID(), getLocation());
        }
        
    }

    @Override
    public boolean canBeBuiltByBuilder(final int newLevel)
    {
        return getBuildingLevel() + 1 == newLevel;
    }

    @Override
    public boolean canAssignCitizens()
    {
        return true;
    }

    @Override
    public boolean canEat(final ItemStack stack)
    {
        if (requiresResourceForBuilding(stack))
        {
            return false;
        }
        return super.canEat(stack);
    }

    /**
     * Provides a view of the miner building class.
     */
    public static class View extends AbstractBuildingBuilderView
    {
        /**
         * Public constructor of the view, creates an instance of it.
         *
         * @param c the colony.
         * @param l the position.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public BOWindow getWindow()
        {
            return new WindowHutBuilderModule(this);
        }
    }
}
