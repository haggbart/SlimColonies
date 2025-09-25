package no.monopixel.slimcolonies.api.blocks;

import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import no.monopixel.slimcolonies.api.blocks.types.GraveType;

public abstract class AbstractBlockSlimColoniesGrave<B extends AbstractBlockSlimColoniesGrave<B>> extends AbstractBlockMinecolonies<B> implements EntityBlock
{
    public static final EnumProperty<GraveType> VARIANT = EnumProperty.create("variant", GraveType.class);

    /**
     * The position it faces.
     */
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public AbstractBlockSlimColoniesGrave(final Properties properties)
    {
        super(properties.noOcclusion());
    }
}
