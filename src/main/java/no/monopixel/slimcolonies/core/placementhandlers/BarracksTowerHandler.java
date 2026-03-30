package no.monopixel.slimcolonies.core.placementhandlers;

import com.ldtteam.structurize.placement.IPlacementContext;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.util.BlockUtils;
import no.monopixel.slimcolonies.api.blocks.ModBlocks;
import no.monopixel.slimcolonies.api.util.Log;
import no.monopixel.slimcolonies.api.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers.handleTileEntityPlacement;

/**
 * Barrackstower handler.
 */
@SuppressWarnings("removal")
public class BarracksTowerHandler implements IPlacementHandler
{
    @Override
    public boolean canHandle(@NotNull final Level world, @NotNull final BlockPos pos, @NotNull final BlockState blockState)
    {
        return blockState.getBlock() == ModBlocks.blockHutBarracksTower;
    }

    @Override
    public List<ItemStack> getRequiredItems(
        @NotNull final Level world,
        @NotNull final BlockPos pos,
        @NotNull final BlockState blockState,
        @Nullable final CompoundTag tileEntityData,
        @NotNull final IPlacementContext placementContext)
    {
        return Collections.emptyList();
    }

    @Override
    public ActionProcessingResult handle(
        @NotNull final Level world,
        @NotNull final BlockPos pos,
        @NotNull final BlockState blockState,
        @Nullable final CompoundTag tileEntityData,
        @NotNull final IPlacementContext placementContext)
    {
        if (world.getBlockState(pos).equals(blockState))
        {
            return ActionProcessingResult.PASS;
        }

        if (!WorldUtil.setBlockState(world, pos, blockState, com.ldtteam.structurize.api.util.constant.Constants.UPDATE_FLAG))
        {
            return ActionProcessingResult.PASS;
        }

        if (tileEntityData != null)
        {
            try
            {
                handleTileEntityPlacement(tileEntityData, world, pos, placementContext.getRotationMirror());
                blockState.getBlock().setPlacedBy(world, pos, blockState, null, BlockUtils.getItemStackFromBlockState(blockState));
            }
            catch (final Exception ex)
            {
                Log.getLogger().warn("Unable to place TileEntity");
            }
        }

        return ActionProcessingResult.SUCCESS;
    }

    @Override
    public boolean doesWorldStateMatchBlueprintState(final BlockState worldState, final BlockState blueprintState, @Nullable final Tuple<BlockEntity, CompoundTag> blockEntityData, @NotNull final IPlacementContext placementContext)
    {
        return worldState.equals(blueprintState);
    }
}
