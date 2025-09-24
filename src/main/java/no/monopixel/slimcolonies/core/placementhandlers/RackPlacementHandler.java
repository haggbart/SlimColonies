package no.monopixel.slimcolonies.core.placementhandlers;

import com.ldtteam.structurize.api.util.ItemStackUtils;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.PlacementSettings;
import no.monopixel.slimcolonies.api.blocks.ModBlocks;
import no.monopixel.slimcolonies.core.blocks.BlockMinecoloniesRack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers.handleTileEntityPlacement;
import static no.monopixel.slimcolonies.api.util.constant.Constants.UPDATE_FLAG;

@SuppressWarnings("removal")
public class RackPlacementHandler implements IPlacementHandler
{
    @Override
    public boolean canHandle(@NotNull final Level world, @NotNull final BlockPos pos, @NotNull final BlockState blockState)
    {
        return blockState.getBlock() instanceof BlockMinecoloniesRack;
    }

    @Override
    public ActionProcessingResult handle(
        @NotNull final Level world,
        @NotNull final BlockPos pos,
        @NotNull final BlockState blockState,
        @Nullable final CompoundTag tileEntityData,
        final boolean complete,
        final BlockPos centerPos,
        final PlacementSettings settings)
    {
        if (world.getBlockState(pos).getBlock() == ModBlocks.blockRack)
        {
            return ActionProcessingResult.SUCCESS;
        }

        world.setBlock(pos, blockState, UPDATE_FLAG);
        if (tileEntityData != null)
        {
            handleTileEntityPlacement(tileEntityData, world, pos, settings);
        }
        return ActionProcessingResult.SUCCESS;
    }

    @Override
    public List<ItemStack> getRequiredItems(
        @NotNull final Level world,
        @NotNull final BlockPos pos,
        @NotNull final BlockState blockState,
        @Nullable final CompoundTag tileEntityData,
        final boolean complete)
    {
        final List<ItemStack> itemList = new ArrayList<>();
        if (world.getBlockState(pos).getBlock() == ModBlocks.blockRack && !complete)
        {
            return itemList;
        }

        itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
        for (final ItemStack stack : PlacementHandlers.getItemsFromTileEntity(tileEntityData, blockState))
        {
            if (!ItemStackUtils.isEmpty(stack))
            {
                itemList.add(stack);
            }
        }
        return itemList;
    }
}
