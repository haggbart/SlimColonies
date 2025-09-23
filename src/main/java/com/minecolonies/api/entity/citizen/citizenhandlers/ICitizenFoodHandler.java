package com.minecolonies.api.entity.citizen.citizenhandlers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

import java.util.Queue;

import com.google.common.collect.ImmutableList;

/**
 * Citizen food handler interface.
 */
public interface ICitizenFoodHandler
{

    /**
     * Add last eaten food item.
     * @param item the last eaten food item.
     */
    void addLastEaten(Item item);

    /**
     * Get the last eaten food item.
     * @return the last eaten item.
     */
    Item getLastEaten();



    /**
     * Read from nbt.
     * @param compound to read it from.
     */
    void read(CompoundTag compound);

    /**
     * Write to nbt.
     * @param compound to write it to.
     */
    void write(CompoundTag compound);


    /**
     * If the citizen has a full food history to allow a good analysis.
     * @return true if so.
     */
    boolean hasFullFoodHistory();

    /**
     * Get the list of last eaten food items.
     * @return the last eaten food items.
     */
    ImmutableList<Item> getLastEatenFoods();
}
