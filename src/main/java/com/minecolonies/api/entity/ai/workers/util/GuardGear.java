package com.minecolonies.api.entity.ai.workers.util;

import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;

import java.util.function.Predicate;

/**
 * Class to hold information about required item for the guard.
 */
public class GuardGear implements Predicate<ItemStack>
{

    /**
     * Item type that is required.
     */
    private final EquipmentSlot type;

    /**
     * Tool type that is needed.
     */
    private final EquipmentTypeEntry itemNeeded;

    /**
     * Create a classification for equipment.
     *
     * @param item item that is being required.
     * @param type item type for the required item.
     */
    public GuardGear(final EquipmentTypeEntry item, final EquipmentSlot type)
    {
        this.type = type;
        this.itemNeeded = item;
    }

    /**
     * Create a classification for a tool level.
     *
     * @param item               item that is being required.
     * @param type               item type for the required item.
     * @param citizenLevelRange  level range required to demand item.
     * @param buildingLevelRange level range that the item will be required.
     */
    @Deprecated
    public GuardGear(
      final EquipmentTypeEntry item, final EquipmentSlot type,
      final Tuple<Integer, Integer> citizenLevelRange,
      final Tuple<Integer, Integer> buildingLevelRange)
    {
        this(item, type);
    }

    /**
     * Create a classification for a tool level.
     *
     * @param item               item that is being required.
     * @param type               item type for the required item.
     * @param minArmorLevel      the min armor level.
     * @param maxArmorLevel      the max armor level.
     * @param citizenLevelRange  level range required to demand item.
     * @param buildingLevelRange level range that the item will be required.
     */
    @Deprecated
    public GuardGear(
      final EquipmentTypeEntry item, final EquipmentSlot type,
      final int minArmorLevel,
      final int maxArmorLevel, final Tuple<Integer, Integer> citizenLevelRange,
      final Tuple<Integer, Integer> buildingLevelRange)
    {
        this(item, type);
    }

    /**
     * @return type of the item
     */
    public EquipmentSlot getType()
    {
        return type;
    }

    /**
     * @return return the tool type that is needed
     */
    public EquipmentTypeEntry getItemNeeded()
    {
        return itemNeeded;
    }

    @Override
    public boolean test(final ItemStack stack)
    {
        return
          (ItemStackUtils.hasEquipmentLevel(stack, itemNeeded, 0, Integer.MAX_VALUE) && stack.getItem() instanceof ArmorItem
             && ((ArmorItem) stack.getItem()).getEquipmentSlot() == getType())
            || (stack.getItem() instanceof SwordItem && getType() == EquipmentSlot.MAINHAND)
            || (stack.getItem() instanceof ShieldItem && getType() == EquipmentSlot.OFFHAND);
    }
}
