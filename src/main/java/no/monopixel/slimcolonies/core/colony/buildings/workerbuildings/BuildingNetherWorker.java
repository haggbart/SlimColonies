package no.monopixel.slimcolonies.core.colony.buildings.workerbuildings;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.crafting.IRecipeStorage;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.api.equipment.ModEquipmentTypes;
import no.monopixel.slimcolonies.api.util.ItemStackUtils;
import no.monopixel.slimcolonies.api.util.WorldUtil;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingModules;
import no.monopixel.slimcolonies.core.colony.buildings.modules.MinimumStockModule;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static no.monopixel.slimcolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static no.monopixel.slimcolonies.api.util.constant.Constants.STACKSIZE;

public class BuildingNetherWorker extends AbstractBuilding
{
    /**
     * Constant name for the Netherworker building
     */
    private static final String NETHER_WORKER = "netherworker";

    /**
     * How many trips we've run this period
     */
    private static final String TAG_CURRENT_TRIPS = "current_trips";

    /**
     * The tag for storing the last trip time to NBT
     */
    private static final String TAG_LAST_TRIP_TIME = "last_trip_time";

    /**
     * How many trips we can make per period by default
     */
    private static final int MAX_PER_PERIOD = 1;

    /**
     * Cooldown period in ticks (15 minutes = 18000L ticks at 20 ticks/second)
     */
    private static final long COOLDOWN_TICKS = 18000L;

    /**
     * Game time (in ticks) when the last trip was completed
     */
    private long lastTripTime = -COOLDOWN_TICKS; // Initialize to allow immediate first trip

    /**
     * How many trips we've done in the current period
     */
    private int currentTrips = 0;

    public BuildingNetherWorker(@NotNull IColony colony, BlockPos pos)
    {
        super(colony, pos);

        keepX.put(itemStack -> ItemStackUtils.isEquipmentType(itemStack, ModEquipmentTypes.axe.get()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.isEquipmentType(itemStack, ModEquipmentTypes.pickaxe.get()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.isEquipmentType(itemStack, ModEquipmentTypes.shovel.get()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.isEquipmentType(itemStack, ModEquipmentTypes.sword.get()), new Tuple<>(1, true));

        keepX.put(itemStack -> itemStack.getItem() instanceof FlintAndSteelItem, new Tuple<>(1, true));

        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
            && itemStack.getItem() instanceof ArmorItem
            && ((ArmorItem) itemStack.getItem()).getEquipmentSlot() == EquipmentSlot.HEAD, new Tuple<>(1, true));
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
            && itemStack.getItem() instanceof ArmorItem
            && ((ArmorItem) itemStack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST, new Tuple<>(1, true));
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
            && itemStack.getItem() instanceof ArmorItem
            && ((ArmorItem) itemStack.getItem()).getEquipmentSlot() == EquipmentSlot.LEGS, new Tuple<>(1, true));
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
            && itemStack.getItem() instanceof ArmorItem
            && ((ArmorItem) itemStack.getItem()).getEquipmentSlot() == EquipmentSlot.FEET, new Tuple<>(1, true));
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return NETHER_WORKER;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        if (compound.contains(TAG_CURRENT_TRIPS))
        {
            this.currentTrips = compound.getInt(TAG_CURRENT_TRIPS);
        }

        if (compound.contains(TAG_LAST_TRIP_TIME))
        {
            this.lastTripTime = compound.getLong(TAG_LAST_TRIP_TIME);
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();

        compound.putInt(TAG_CURRENT_TRIPS, this.currentTrips);
        compound.putLong(TAG_LAST_TRIP_TIME, this.lastTripTime);

        return compound;
    }

    @Override
    public int buildingRequiresCertainAmountOfItem(final ItemStack stack, final List<ItemStorage> localAlreadyKept, final boolean inventory, final JobEntry jobEntry)
    {
        if (stack.isEmpty())
        {
            return 0;
        }

        if (inventory && getFirstModuleOccurance(MinimumStockModule.class).isStocked(stack))
        {
            return stack.getCount();
        }

        // Keep portal materials from recipe
        IRecipeStorage rs = getFirstModuleOccurance(BuildingNetherWorker.CraftingModule.class).getFirstRecipe(ItemStack::isEmpty);
        if (rs != null)
        {
            final ItemStorage kept = new ItemStorage(stack);
            boolean containsItem = rs.getInput().contains(kept);
            int keptCount = localAlreadyKept.stream().filter(storage -> storage.equals(kept)).mapToInt(ItemStorage::getAmount).sum();
            if (containsItem && (keptCount < STACKSIZE || !inventory))
            {
                if (localAlreadyKept.contains(kept))
                {
                    kept.setAmount(localAlreadyKept.remove(localAlreadyKept.indexOf(kept)).getAmount());
                }
                localAlreadyKept.add(kept);
                return 0;
            }
        }

        // Keep all food from menu
        final ItemStorage foodStorage = new ItemStorage(stack);
        if (getModule(BuildingModules.NETHERMINER_MENU).getMenu().contains(foodStorage))
        {
            return 0;
        }

        return super.buildingRequiresCertainAmountOfItem(stack, localAlreadyKept, inventory, jobEntry);
    }

    /**
     * Check to see if it's valid to do a trip by checking the cooldown timer
     *
     * @return true if the worker can go to the nether
     */
    public boolean isReadyForTrip()
    {
        final long currentTime = colony.getWorld().getGameTime();
        final long timeSinceLastTrip = currentTime - lastTripTime;

        // Reset trip counter if cooldown has expired
        if (timeSinceLastTrip >= COOLDOWN_TICKS)
        {
            this.currentTrips = 0;
        }

        return this.currentTrips < getMaxPerPeriod();
    }

    /**
     * Let the building know we're doing a trip
     */
    public void recordTrip()
    {
        this.currentTrips++;
        this.lastTripTime = colony.getWorld().getGameTime();
    }

    /**
     * Get the tagged location that the worker should walk to in the portal.
     * This should be a 'air block' in the portal that can directly be checked to see if the portal is open
     *
     * @return the block above the tag, null if not available
     */
    public BlockPos getPortalLocation()
    {
        BlockPos portalLocation = getFirstLocationFromTag("portal");
        if (portalLocation != null)
        {
            return portalLocation.above();
        }
        return null;
    }

    /**
     * Get the max per period, potentially modified by research
     *
     * @return max trips per cooldown period
     */
    public static int getMaxPerPeriod()
    {
        return MAX_PER_PERIOD;
    }

    @Override
    public void onPlacement()
    {
        super.onPlacement();
        final Level world = colony.getWorld();
        if (WorldUtil.isNetherType(world))
        {
            final Block block = world.getBlockState(this.getPosition()).getBlock();
            block.destroy(world, getPosition(), world.getBlockState(getPosition()));
        }
    }

    public static class CraftingModule extends AbstractCraftingBuildingModule.Custom
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public CraftingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
        }
    }
}
