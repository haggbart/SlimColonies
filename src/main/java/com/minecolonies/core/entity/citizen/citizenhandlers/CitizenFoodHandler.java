package com.minecolonies.core.entity.citizen.citizenhandlers;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenFoodHandler;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static com.minecolonies.api.util.constant.Constants.TAG_STRING;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_LAST_FOODS;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * The food handler for the citizen.
 */
public class CitizenFoodHandler implements ICitizenFoodHandler
{
    /**
     * Food queue size.
     */
    private static final int FOOD_QUEUE_SIZE = 10;

    /**
     * Assigned citizen data.
     */
    private final ICitizenData  citizenData;

    /**
     * Collection of last food items a citizen has eaten.
     */
    private final EvictingQueue<Item> lastEatenFoods = EvictingQueue.create(FOOD_QUEUE_SIZE);


    /**
     * Create the food handler.
     * @param citizenData of it.
     */
    public CitizenFoodHandler(final ICitizenData citizenData)
    {
        super();
        this.citizenData = citizenData;
    }

    @Override
    public void addLastEaten(final Item item)
    {
        lastEatenFoods.add(item);
        citizenData.markDirty(TICKS_SECOND);
    }

    @Override
    public Item getLastEaten()
    {
        return lastEatenFoods.peek();
    }



    @Override
    public boolean hasFullFoodHistory()
    {
        return lastEatenFoods.size() >= FOOD_QUEUE_SIZE;
    }

    @Override
    public void read(final CompoundTag compound)
    {
        @NotNull final ListTag lastFoodNbt = compound.getList(TAG_LAST_FOODS, TAG_STRING);
        for (int i = 0; i < lastFoodNbt.size(); i++)
        {
            final Item lastFood = BuiltInRegistries.ITEM.get(new ResourceLocation(lastFoodNbt.getString(i)));
            if (lastFood != Items.AIR)
            {
                lastEatenFoods.add(lastFood);
            }
        }
    }

    @Override
    public void write(final CompoundTag compound)
    {
        @NotNull final ListTag lastEatenFoodsNBT = new ListTag();
        for (final Item foodItem : lastEatenFoods)
        {
            lastEatenFoodsNBT.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(foodItem).toString()));
        }
        compound.put(TAG_LAST_FOODS, lastEatenFoodsNBT);
    }


    @Override
    public ImmutableList<Item> getLastEatenFoods()
    {
        return ImmutableList.copyOf(lastEatenFoods);
    }
}
