package no.monopixel.slimcolonies.api.entity.ai.workers.util;

import no.monopixel.slimcolonies.api.equipment.registry.EquipmentTypeEntry;
import no.monopixel.slimcolonies.api.util.ItemStackUtils;
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
          (ItemStackUtils.hasEquipmentLevel(stack, itemNeeded) && stack.getItem() instanceof ArmorItem
             && ((ArmorItem) stack.getItem()).getEquipmentSlot() == getType())
            || (stack.getItem() instanceof SwordItem && getType() == EquipmentSlot.MAINHAND)
            || (stack.getItem() instanceof ShieldItem && getType() == EquipmentSlot.OFFHAND);
    }
}
