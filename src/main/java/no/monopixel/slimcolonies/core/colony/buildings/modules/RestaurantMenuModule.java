package no.monopixel.slimcolonies.core.colony.buildings.modules;

import com.google.common.reflect.TypeToken;
import no.monopixel.slimcolonies.api.MinecoloniesAPIProxy;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.*;
import no.monopixel.slimcolonies.api.colony.buildings.modules.AbstractBuildingModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.IAltersRequiredItems;
import no.monopixel.slimcolonies.api.colony.buildings.modules.IPersistentModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.ITickingModule;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.IRequest;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.RequestState;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.MinimumStack;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.Stack;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.api.crafting.RecipeStorage;
import com.minecolonies.api.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import no.monopixel.slimcolonies.api.util.*;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;


/**
 * Minimum stock module.
 */
public class RestaurantMenuModule extends AbstractBuildingModule implements IPersistentModule, ITickingModule, IAltersRequiredItems
{
    /**
     * Minimum stock it can hold per level.
     */
    public static final int STOCK_PER_LEVEL = 5;

    /**
     * The minimum stock tag.
     */
    private static final String TAG_MENU = "menu";

    /**
     * The minimum stock.
     */
    protected final Set<ItemStorage> menu = new HashSet<>();

    /**
     * Whether the worker here can cook.
     */
    private final boolean                      canCook;

    /**
     * Get max stock calculation.
     */
    private final Function<IBuilding, Integer> expectedStock;

    /**
     * Get the restaurant menu.
     * @return the menu.
     */
    public Set<ItemStorage> getMenu()
    {
        return menu;
    }

    /**
     * Create a restaurant menu module.
     * @param canCook whether the worker here can cook.
     */
    public RestaurantMenuModule(final boolean canCook, final Function<IBuilding, Integer> expectedStock )
    {
        this.canCook = canCook;
        this.expectedStock = expectedStock;
    }

    /**
     * Add a new menu item.
     * @param itemStack the menu item to add.
     */
    public void addMenuItem(final ItemStack itemStack)
    {
        if (menu.size() >= building.getBuildingLevel() * STOCK_PER_LEVEL)
        {
            return;
        }

        menu.add(new ItemStorage(itemStack));
        markDirty();
    }

    /**
     * Remove a menu item.
     * @param itemStack the menu item to remove.
     */
    public void removeMenuItem(final ItemStack itemStack)
    {
        menu.remove(new ItemStorage(itemStack));

        final Collection<IToken<?>> list = building.getOpenRequestsByRequestableType().getOrDefault(TypeToken.of(Stack.class), new ArrayList<>());
        final IToken<?> token = getMatchingRequest(itemStack, list);
        if (token != null)
        {
            building.getColony().getRequestManager().updateRequestState(token, RequestState.CANCELLED);
        }
        markDirty();
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        if (WorldUtil.isBlockLoaded(colony.getWorld(), building.getPosition()))
        {
            final Collection<IToken<?>> list = building.getOpenRequestsByRequestableType().getOrDefault(TypeToken.of(MinimumStack.class), new ArrayList<>());

            for (final ItemStorage menuItem : menu)
            {
                final ItemStack originalStack = menuItem.getItemStack().copy();
                if (originalStack.isEmpty())
                {
                    continue;
                }
                ItemStack requestStack = originalStack;
                ItemStack rawStack = ItemStack.EMPTY;
                if (canCook && MinecoloniesAPIProxy.getInstance().getFurnaceRecipes().getFirstSmeltingRecipeByResult(menuItem) instanceof RecipeStorage recipeStorage)
                {
                    // Smelting Recipes only got 1 input. Request sometimes the input if this is a smeltable.
                    rawStack = recipeStorage.getInput().get(0).getItemStack().copy();
                }

                final int target = originalStack.getMaxStackSize() * getExpectedStock();
                final int count = InventoryUtils.hasBuildingEnoughElseCount(this.building, new ItemStorage(originalStack, true), target);
                final int rawCount = rawStack.isEmpty() ? 0 : InventoryUtils.hasBuildingEnoughElseCount(this.building, new ItemStorage(rawStack, true), target);
                final int delta = target - count - rawCount;
                if (MathUtils.RANDOM.nextBoolean() && !rawStack.isEmpty())
                {
                    requestStack = rawStack.copy();
                }
                final IToken<?> request = getMatchingRequest(requestStack, list);
                if (delta > target / 4)
                {
                    if (request == null)
                    {
                        final int qty = Math.min(16, Math.min(requestStack.getMaxStackSize(), delta));
                        final MinimumStack stack = new MinimumStack(requestStack, false, true, ItemStackUtils.EMPTY, qty, 1);

                        stack.setCanBeResolvedByBuilding(false);
                        building.createRequest(stack, true);
                    }
                }
                else if (request != null && delta <= 0)
                {
                    building.getColony().getRequestManager().updateRequestState(request, RequestState.CANCELLED);
                }
            }
        }
    }

    /**
     * Get the request from the list that matches this stack.
     *
     * @param stack the stack to search for in the requests.
     * @param list  the list of requests.
     * @return the token of the matching request or null.
     */
    private IToken<?> getMatchingRequest(final ItemStack stack, final Collection<IToken<?>> list)
    {
        for (final IToken<?> token : list)
        {
            final IRequest<?> iRequest = building.getColony().getRequestManager().getRequestForToken(token);
            if (iRequest != null && iRequest.getRequest() instanceof Stack && ItemStackUtils.compareItemStacksIgnoreStackSize(((Stack) iRequest.getRequest()).getStack(), stack))
            {
                return token;
            }
        }
        return null;
    }

    /**
     * Get the max stock in stacks per menu item.
     * @return the max stock.
     */
    public int getExpectedStock()
    {
        return expectedStock.apply(building);
    }

    @Override
    public void alterItemsToBeKept(final TriConsumer<Predicate<ItemStack>, Integer, Boolean> consumer)
    {
        for (final ItemStorage menuItem : menu)
        {
            consumer.accept(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, menuItem.getItemStack(), false, true), menuItem.getItemStack().getMaxStackSize() * getExpectedStock(), false);
            if (canCook && MinecoloniesAPIProxy.getInstance().getFurnaceRecipes().getFirstSmeltingRecipeByResult(menuItem) instanceof RecipeStorage recipeStorage)
            {
                final ItemStack smeltStack = recipeStorage.getInput().get(0).getItemStack();
                consumer.accept(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, smeltStack, false, true), smeltStack.getMaxStackSize() * getExpectedStock(), false);
            }
        }
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        menu.clear();
        final ListTag minimumStockTagList = compound.getList(TAG_MENU, Tag.TAG_COMPOUND);
        for (int i = 0; i < minimumStockTagList.size(); i++)
        {
            final ItemStack itemStack = ItemStack.of(minimumStockTagList.getCompound(i));
            if (FoodUtils.EDIBLE.test(itemStack))
            {
                menu.add(new ItemStorage(itemStack));
            }
        }
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        @NotNull final ListTag minimumStockTagList = new ListTag();
        for (final ItemStorage menuItem : menu)
        {
            minimumStockTagList.add(menuItem.getItemStack().save(new CompoundTag()));
        }
        compound.put(TAG_MENU, minimumStockTagList);
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(menu.size());
        for (final ItemStorage menuItem : menu)
        {
            buf.writeItem(menuItem.getItemStack());
        }
    }
}
