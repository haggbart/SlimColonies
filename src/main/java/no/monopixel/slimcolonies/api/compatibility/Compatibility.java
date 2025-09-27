package no.monopixel.slimcolonies.api.compatibility;

import no.monopixel.slimcolonies.api.compatibility.resourcefulbees.IBeehiveCompat;
import no.monopixel.slimcolonies.api.compatibility.tinkers.SlimeTreeProxy;
import no.monopixel.slimcolonies.api.compatibility.tinkers.TinkersToolProxy;
import no.monopixel.slimcolonies.api.equipment.registry.EquipmentTypeEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This class is to store the methods that call the methods to check for miscellaneous compatibility problems.
 */
public final class Compatibility
{

    private Compatibility()
    {
        throw new IllegalAccessError("Utility class");
    }

    public static IJeiProxy jeiProxy = new IJeiProxy() {};
    public static IBeehiveCompat beeHiveCompat = new IBeehiveCompat() {};
    public static SlimeTreeProxy   tinkersSlimeCompat = new SlimeTreeProxy();
    public static TinkersToolProxy tinkersCompat      = new TinkersToolProxy();

    /**
     * This method checks to see if STACK is able to mine anything. It goes through all compatibility checks.
     *
     * @param stack the item in question.
     * @param tool  the name of the tool.
     * @return boolean whether the stack can mine or not.
     */
    public static boolean getMiningLevelCompatibility(@Nullable final ItemStack stack, @Nullable final String tool)
    {
        return !tinkersCompat.checkTinkersBroken(stack);
    }

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
     * This method checks if block is slime leaf.
     *
     * @param block the block.
     * @return if the block is a slime leaf.
     */
    public static boolean isSlimeLeaf(@NotNull final Block block)
    {
        return tinkersSlimeCompat.checkForTinkersSlimeLeaves(block);
    }

    /**
     * This method checks if block is slime sapling.
     *
     * @param block the block.
     * @return if the block is a slime sapling.
     */
    public static boolean isSlimeSapling(@NotNull final Block block)
    {
        return tinkersSlimeCompat.checkForTinkersSlimeSapling(block);
    }

    /**
     * This method checks if block is slime dirt.
     *
     * @param block the block.
     * @return if the block is slime dirt.
     */
    public static boolean isSlimeDirtOrGrass(@NotNull final Block block)
    {
        return tinkersSlimeCompat.checkForTinkersSlimeDirtOrGrass(block);
    }

    /**
     * Get the Slime leaf variant.
     *
     * @param leaf the leaf.
     * @return the variant.
     */
    public static int getLeafVariant(@NotNull final BlockState leaf)
    {
        return tinkersSlimeCompat.getTinkersLeafVariant(leaf);
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
     * @param stack the stack to check for.
     * @param toolType the tool type.
     * @return true if so.
     */
    public static boolean isTinkersTool(@Nullable final ItemStack stack, final EquipmentTypeEntry toolType) { return tinkersCompat.isTinkersTool(stack, toolType); }

    /**
     * Calculate the actual attack damage of the tinkers weapon.
     *
     * @param stack the stack.
     * @return the attack damage.
     */
    public static double getAttackDamage(@NotNull final ItemStack stack)
    {
        return tinkersCompat.getAttackDamage(stack);
    }

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












    /**
     * Get comps from a hive at the given position
     *
     * @param pos    TE pos
     * @param world  world
     * @param amount comb amount
     * @return list of drops
     */
    public static List<ItemStack> getCombsFromHive(BlockPos pos, Level world, int amount)
    {
        return beeHiveCompat.getCombsFromHive(pos, world, amount);
    }
}
