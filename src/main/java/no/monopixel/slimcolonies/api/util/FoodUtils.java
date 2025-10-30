package no.monopixel.slimcolonies.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.api.inventory.InventoryCitizen;
import no.monopixel.slimcolonies.core.tileentities.TileEntityRack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static no.monopixel.slimcolonies.api.research.util.ResearchConstants.SATURATION;

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
     *
     * @param stack        the food stack to check.
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
     *
     * @param foodStack     the food to consume.
     * @param itemFood      the food properties of that food.
     * @param researchBonus the bonus from research (0 for no bonus).
     * @return the saturation adjustment to apply when consuming this food.
     */
    public static double getFoodValue(final ItemStack foodStack, @Nullable final FoodProperties itemFood, final double researchBonus)
    {
        if (itemFood == null)
        {
            return 0;
        }

        return itemFood.getNutrition() * (1.0 + researchBonus);
    }

    /**
     * Calculate the actual food value for a citizen consuming a given food.
     *
     * @param foodStack the food to consume.
     * @param citizen   the citizen consuming the food.
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
     *
     * @param inventoryCitizen the inventory to check.
     * @param citizenData      the citizen data the food is for.
     * @param menu             the menu that has to be matched. or null
     * @return the matching inv slot, or -1.
     */
    public static int getBestFoodForCitizen(final InventoryCitizen inventoryCitizen, final ICitizenData citizenData, @Nullable final Set<ItemStorage> menu)
    {
        final List<Integer> validFoodSlots = new ArrayList<>();

        for (int i = 0; i < inventoryCitizen.getSlots(); i++)
        {
            final ItemStorage invStack = new ItemStorage(inventoryCitizen.getStackInSlot(i));
            if ((menu == null || menu.contains(invStack)) && FoodUtils.canEat(invStack.getItemStack(), citizenData.getHomeBuilding(), citizenData.getWorkBuilding()))
            {
                validFoodSlots.add(i);
            }
        }

        if (validFoodSlots.isEmpty())
        {
            return -1;
        }

        return validFoodSlots.get(MathUtils.RANDOM.nextInt(validFoodSlots.size()));
    }

    /**
     * Get the best food for a given citizen from a building's storage.
     *
     * @param citizenData the citizen data the food is for.
     * @param menu        the menu that has to be matched or null.
     * @param building    the building to search for food.
     * @return a random matching food item, or null.
     */
    public static ItemStorage checkForFoodInBuilding(final ICitizenData citizenData, @Nullable final Set<ItemStorage> menu, final IBuilding building)
    {
        final Level world = building.getColony().getWorld();
        final List<ItemStorage> validFoodItems = new ArrayList<>();

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
                            validFoodItems.add(storage);
                        }
                    }
                }
            }
        }

        if (validFoodItems.isEmpty())
        {
            return null;
        }

        return new ItemStorage(validFoodItems.get(MathUtils.RANDOM.nextInt(validFoodItems.size())).getItemStack().copy());
    }

    /**
     * Check if the citizen has a better food option in their inventory than in the building.
     *
     * @param inventoryCitizen the citizen's inventory to check.
     * @param citizenData      the citizen data the food is for.
     * @param menu             the menu that has to be matched or null.
     * @param building         the building to check.
     * @return true if the inventory has a good food option.
     */
    public static boolean hasBestOptionInInv(
        final InventoryCitizen inventoryCitizen,
        final ICitizenData citizenData,
        @Nullable final Set<ItemStorage> menu,
        final IBuilding building)
    {
        // Simply check if the citizen has any food in their inventory that matches the menu
        return getBestFoodForCitizen(inventoryCitizen, citizenData, menu) >= 0;
    }
}
