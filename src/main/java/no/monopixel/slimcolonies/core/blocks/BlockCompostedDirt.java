package no.monopixel.slimcolonies.core.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.IPlantable;
import no.monopixel.slimcolonies.api.blocks.AbstractBlockSlimColonies;
import no.monopixel.slimcolonies.api.blocks.interfaces.ITickableBlockSlimColonies;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.tileentities.TileEntityCompostedDirt;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Block that if activated with BoneMeal or Compost by an AI will produce flowers by intervals until it deactivates
 */
public class BlockCompostedDirt extends AbstractBlockSlimColonies<BlockCompostedDirt> implements ITickableBlockSlimColonies
{
    private static final String     BLOCK_NAME     = "composted_dirt";
    private static final float      BLOCK_HARDNESS = 5f;
    private static final float      RESISTANCE     = 1f;
    private final static VoxelShape SHAPE          = Shapes.box(0, 0, 0, 1, 1, 1);

    /**
     * The constructor of the block.
     */
    public BlockCompostedDirt()
    {
        super(Properties.of().mapColor(MapColor.DIRT).sound(SoundType.ROOTED_DIRT).strength(BLOCK_HARDNESS, RESISTANCE).sound(SoundType.GRAVEL));
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, BLOCK_NAME);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos blockPos, @NotNull final BlockState blockState)
    {
        return new TileEntityCompostedDirt(blockPos, blockState);
    }

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context)
    {
        return SHAPE;
    }

    @Override
    public boolean canSustainPlant(
        @NotNull final BlockState state,
        @NotNull final BlockGetter world,
        final BlockPos pos,
        @NotNull final Direction facing,
        final IPlantable plantable)
    {
        return true;
    }
}
