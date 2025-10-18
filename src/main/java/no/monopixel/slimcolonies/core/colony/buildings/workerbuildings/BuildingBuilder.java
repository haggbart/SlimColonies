package no.monopixel.slimcolonies.core.colony.buildings.workerbuildings;

import com.ldtteam.blockui.views.BOWindow;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyView;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.modules.settings.ISettingKey;
import no.monopixel.slimcolonies.api.colony.workorders.IBuilderWorkOrder;
import no.monopixel.slimcolonies.api.colony.workorders.IServerWorkOrder;
import no.monopixel.slimcolonies.api.equipment.ModEquipmentTypes;
import no.monopixel.slimcolonies.api.util.ItemStackUtils;
import no.monopixel.slimcolonies.api.util.MessageUtils;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.client.gui.huts.WindowHutBuilderModule;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingModules;
import no.monopixel.slimcolonies.core.colony.buildings.modules.settings.BuilderModeSetting;
import no.monopixel.slimcolonies.core.colony.buildings.modules.settings.SettingKey;
import no.monopixel.slimcolonies.core.colony.buildings.modules.settings.StringSetting;
import no.monopixel.slimcolonies.core.colony.buildings.views.AbstractBuildingBuilderView;
import no.monopixel.slimcolonies.core.colony.jobs.JobBuilder;
import org.jetbrains.annotations.NotNull;

import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_PURGED_MOBS;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.*;

/**
 * The builders building.
 */
public class BuildingBuilder extends AbstractBuildingStructureBuilder
{
    /**
     * Settings key for the building mode.
     */
    public static final ISettingKey<StringSetting>      MODE          = new SettingKey<>(StringSetting.class, new ResourceLocation(Constants.MOD_ID, "mode"));
    public static final ISettingKey<BuilderModeSetting> BUILDING_MODE = new SettingKey<>(BuilderModeSetting.class, new ResourceLocation(Constants.MOD_ID, "buildmode"));

    /**
     * Both setting options.
     */
    public static final String MANUAL_SETTING = "no.monopixel.slimcolonies.core.builder.setting.manual";
    public static final String AUTO_SETTING   = "no.monopixel.slimcolonies.core.builder.setting.automatic";

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

        keepX.put(itemStack -> ItemStackUtils.isEquipmentType(itemStack, ModEquipmentTypes.pickaxe.get()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.isEquipmentType(itemStack, ModEquipmentTypes.shovel.get()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.isEquipmentType(itemStack, ModEquipmentTypes.axe.get()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.isEquipmentType(itemStack, ModEquipmentTypes.hoe.get()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.isEquipmentType(itemStack, ModEquipmentTypes.shears.get()), new Tuple<>(1, true));
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

    /**
     * Sets the work order with the given id as the work order for this buildings citizen.
     *
     * @param orderId the id of the work order to select.
     */
    public void setWorkOrder(int orderId, final NetworkEvent.Context ctxIn)
    {
        final ICitizenData citizen = getModule(BuildingModules.BUILDER_WORK).getFirstCitizen();
        if (citizen == null)
        {
            MessageUtils.format(MESSAGE_WARNING_NO_WORKER_ASSIGNED).sendTo(ctxIn.getSender());
            return;
        }

        IServerWorkOrder wo = getColony().getWorkManager().getWorkOrder(orderId);
        if (!(wo instanceof IBuilderWorkOrder))
        {
            MessageUtils.format(MESSAGE_WARNING_NOTFORBUILDER).sendTo(ctxIn.getSender());
            return;
        }

        if (!wo.getClaimedBy().equals(BlockPos.ZERO))
        {
            MessageUtils.format(MESSAGE_WARNING_ALREADY_CLAIMED).sendTo(ctxIn.getSender());
            return;
        }

        if (citizen.getJob(JobBuilder.class).hasWorkOrder())
        {
            wo.setClaimedBy(getID());
            getColony().getWorkManager().setDirty(true);
            return;
        }

        final IBuilding building = citizen.getWorkBuilding();
        if (((IBuilderWorkOrder) wo).canBuildIgnoringDistance(building, building.getPosition(), building.getBuildingLevel()))
        {
            citizen.getJob(JobBuilder.class).setWorkOrder(wo);
            wo.setClaimedBy(getID());
            getColony().getWorkManager().setDirty(true);
            markDirty();
        }
        else
        {
            MessageUtils.format(MESSAGE_WARNING_CANNOTBUILD).sendTo(ctxIn.getSender());
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
