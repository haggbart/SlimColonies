package no.monopixel.slimcolonies.core.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.world.item.BoneMealItem.applyBonemeal;

/**
 * Class used to handle the compost item.
 */
public class ItemCompost extends AbstractItemSlimColonies
{

    /***
     * Constructor for the ItemCompost
     * @param properties the properties.
     */
    public ItemCompost(final Item.Properties properties)
    {
        super("compost", properties.stacksTo(Constants.STACKSIZE));
    }

    /**
     * Wrapper around {@link net.minecraft.world.item.BoneMealItem#applyBonemeal(ItemStack, Level, BlockPos, Player)}.
     *
     * @param stack  the input item stack.
     * @param level  the input level.
     * @param pos    the input position.
     * @param player the input player.
     * @return true if successfully bone-mealed.
     */
    private static boolean applyCompost(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player)
    {
        return applyBonemeal(stack, level, pos, player);
    }

    @Override
    @NotNull
    public InteractionResult useOn(final UseOnContext ctx)
    {
        if (applyCompost(ctx.getItemInHand(), ctx.getLevel(), ctx.getClickedPos(), ctx.getPlayer()))
        {
            if (!ctx.getLevel().isClientSide)
            {
                ctx.getLevel().levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, ctx.getClickedPos(), 0);
            }

            return InteractionResult.sidedSuccess(ctx.getLevel().isClientSide);
        }
        return InteractionResult.PASS;
    }
}
