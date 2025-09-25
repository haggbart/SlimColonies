package no.monopixel.slimcolonies.api.blocks;

import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * Abstract class for minecolonies named graves.
 */
public abstract class AbstractBlockSlimColoniesNamedGrave<B extends AbstractBlockSlimColoniesNamedGrave<B>> extends AbstractBlockMinecolonies<B> implements EntityBlock
{
    /**
     * The direction the block is facing.
     */
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public AbstractBlockSlimColoniesNamedGrave(final Properties properties)
    {
        super(properties.noOcclusion());
    }
}
