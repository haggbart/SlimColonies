package no.monopixel.slimcolonies.api.compatibility;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import no.monopixel.slimcolonies.api.compatibility.tinkers.SlimeTreeProxy;
import no.monopixel.slimcolonies.api.compatibility.tinkers.TinkersToolProxy;
import no.monopixel.slimcolonies.api.equipment.registry.EquipmentTypeEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class is to store the methods that call the methods to check for miscellaneous compatibility problems.
 */
public final class Compatibility
{

    private Compatibility()
    {
        throw new IllegalAccessError("Utility class");
    }

    public static IJeiProxy        jeiProxy           = new IJeiProxy() {};
    public static SlimeTreeProxy   tinkersSlimeCompat = new SlimeTreeProxy();
    public static TinkersToolProxy tinkersCompat      = new TinkersToolProxy();

    /**
     * This method checks if block is slime block.
     *
     * @param block the block.
     * @return if the block is a slime block.
     */
    public static boolean isSlimeBlock(@NotNull final Block block)
    {
        return tinkersSlimeCompat.checkForTinkersSlimeBlock(block);
    }

    /**
     * Check if a certain itemstack is a tinkers weapon.
     *
     * @param stack the stack to check for.
     * @return true if so.
     */
    public static boolean isTinkersWeapon(@NotNull final ItemStack stack)
    {
        return tinkersCompat.isTinkersWeapon(stack);
    }

    /**
     * Check if a certain item stack is a tinkers tool of the given tool type.
     *
     * @param stack    the stack to check for.
     * @param toolType the tool type.
     * @return true if so.
     */
    public static boolean isTinkersTool(@Nullable final ItemStack stack, final EquipmentTypeEntry toolType) {return tinkersCompat.isTinkersTool(stack, toolType);}

    /**
     * Calculate the tool level of the stack.
     *
     * @param stack the stack.
     * @return the tool level
     */
    public static int getToolLevel(@NotNull final ItemStack stack)
    {
        return tinkersCompat.getToolLevel(stack);
    }

}
