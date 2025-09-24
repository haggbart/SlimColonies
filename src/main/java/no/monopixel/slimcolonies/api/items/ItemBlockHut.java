package no.monopixel.slimcolonies.api.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import no.monopixel.slimcolonies.api.IMinecoloniesAPI;
import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.blocks.AbstractColonyBlock;
import no.monopixel.slimcolonies.api.colony.IColonyView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_COLONY_ID;
import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_OTHER_LEVEL;

/**
 * A custom item class for hut blocks.
 */
public class ItemBlockHut extends BlockItem
{
    /**
     * This items block.
     */
    private AbstractColonyBlock<?> block;

    /**
     * Creates a new ItemBlockHut representing the item form of the given {@link AbstractBlockHut}.
     *
     * @param block   the {@link AbstractBlockHut} this item represents.
     * @param builder the item properties to use.
     */
    public ItemBlockHut(AbstractColonyBlock<?> block, Properties builder)
    {
        super(block, builder);
        this.block = block;
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @Nullable final Level world, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flags)
    {
        super.appendHoverText(stack, world, tooltip, flags);

        if (stack.hasTag() && stack.getTag().contains(TAG_OTHER_LEVEL))
        {
            final Component level = Component.literal(String.valueOf(stack.getTag().getInt(TAG_OTHER_LEVEL))).withStyle(ChatFormatting.GOLD);
            tooltip.add(Component.translatable("item.slimcolonies.hut.level", level).withStyle(ChatFormatting.GREEN));
        }
        if (stack.hasTag() && stack.getTag().contains(TAG_COLONY_ID) && world != null)
        {
            // todo: should we store a dimension id as well? because currently this can't tell, so it will sometimes be wrong
            final IColonyView colony = IMinecoloniesAPI.getInstance().getColonyManager().getColonyView(stack.getTag().getInt(TAG_COLONY_ID), world.dimension());
            final Component name = colony != null ? Component.literal(colony.getName()) : Component.translatable("item.slimcolonies.hut.unknowncolony");
            tooltip.add(Component.translatable("item.slimcolonies.hut.colony", name).withStyle(ChatFormatting.ITALIC));
        }
    }
}
