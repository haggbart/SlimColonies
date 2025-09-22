package com.minecolonies.api.util;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenFoodHandler;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.core.tileentities.TileEntityRack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Predicate;

import static com.minecolonies.api.research.util.ResearchConstants.SATURATION;

/**
 * Food specific util functions.
 */
public class FoodUtils
{
    /**
     * Predicate describing food which can be eaten (is not raw).
     */
    public static final Predicate<ItemStack> EDIBLE = itemStack -> ItemStackUtils.ISFOOD.test(itemStack) && !ItemStackUtils.ISCOOKABLE.test(itemStack);

    /**
     * Check if the given stack can be eaten by citizens.
     * @param stack the food stack to check.
     * @param homeBuilding the citizen's home building (unused now).
     * @param workBuilding the citizen's work building.
     * @return true if the food can be eaten.
     */
    public static boolean canEat(final ItemStack stack, final IBuilding homeBuilding, final IBuilding workBuilding)
    {
        if (!EDIBLE.test(stack))
        {
            return false;
        }

        return workBuilding == null || workBuilding.canEat(stack);
    }

    /**
     * Calculate the actual food value for a citizen consuming a given food.
     * @param foodStack the food to consume.
     * @param itemFood the food properties of that food.
     * @param researchBonus the bonus from research (0 for no bonus).
     * @return the saturation adjustment to apply when consuming this food.
     */
    public static double getFoodValue(final ItemStack foodStack, @Nullable final FoodProperties itemFood, final double researchBonus)
    {
        if (itemFood == null)
        {
            return 0;
        }

        // All food now gives full nutrition value
        return itemFood.getNutrition() / 1.2 * (1.0 + researchBonus);
    }

    /**
     * Calculate the actual food value for a citizen consuming a given food.
     * @param foodStack the food to consume.
     * @param citizen the citizen consuming the food.
     * @return the saturation adjustment to apply when consuming this food.
     */
    public static double getFoodValue(final ItemStack foodStack, final AbstractEntityCitizen citizen)
    {
        final FoodProperties itemFood = foodStack.getItem().getFoodProperties(foodStack, citizen);
        final double researchBonus = citizen.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(SATURATION);
        return getFoodValue(foodStack, itemFood, researchBonus);
    }

    /**
     * Get the best food for a given citizen from a given inventory and return the index where it is.
     * @param inventoryCitizen the inventory to check.
     * @param citizenData the citizen data the food is for.
     * @param menu the menu that has to be matched. or null
     * @return the matching inv slot, or -1.
     */
    public static int getBestFoodForCitizen(final InventoryCitizen inventoryCitizen, final ICitizenData citizenData, @Nullable final Set<ItemStorage> menu)
    {
        // Find the first edible food item that matches the menu (if any)
        for (int i = 0; i < inventoryCitizen.getSlots(); i++)
        {
            final ItemStorage invStack = new ItemStorage(inventoryCitizen.getStackInSlot(i));
            if ((menu == null || menu.contains(invStack)) && FoodUtils.canEat(invStack.getItemStack(), citizenData.getHomeBuilding(), citizenData.getWorkBuilding()))
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * Get the best food for a given citizen from a building's storage.
     * @param citizenData the citizen data the food is for.
     * @param menu the menu that has to be matched or null.
     * @param building the building to search for food.
     * @return the first matching food item, or null.
     */
    public static ItemStorage checkForFoodInBuilding(final ICitizenData citizenData, @Nullable final Set<ItemStorage> menu, final IBuilding building)
    {
        final Level world = building.getColony().getWorld();

        for (final BlockPos pos : building.getContainers())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TileEntityRack rackEntity)
                {
                    for (final ItemStorage storage : rackEntity.getAllContent().keySet())
                    {
                        if ((menu == null || menu.contains(storage)) && FoodUtils.canEat(storage.getItemStack(), citizenData.getHomeBuilding(), citizenData.getWorkBuilding()))
                        {
                            return new ItemStorage(storage.getItemStack().copy());
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Check if the citizen has a better food option in their inventory than in the building.
     * @param inventoryCitizen the citizen's inventory to check.
     * @param citizenData the citizen data the food is for.
     * @param menu the menu that has to be matched or null.
     * @param building the building to check.
     * @return true if the inventory has a good food option.
     */
    public static boolean hasBestOptionInInv(final InventoryCitizen inventoryCitizen, final ICitizenData citizenData, @Nullable final Set<ItemStorage> menu, final IBuilding building)
    {
        // Simply check if the citizen has any food in their inventory that matches the menu
        return getBestFoodForCitizen(inventoryCitizen, citizenData, menu) >= 0;
    }
}
