package no.monopixel.slimcolonies.api.blocks;

import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import no.monopixel.slimcolonies.api.blocks.types.RackType;

public abstract class AbstractBlockSlimColoniesRack<B extends AbstractBlockSlimColoniesRack<B>> extends AbstractBlockSlimColonies<B> implements EntityBlock
{
    public static final EnumProperty<RackType> VARIANT = EnumProperty.create("variant", RackType.class);

    /**
     * The position it faces.
     */
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public AbstractBlockSlimColoniesRack(final Properties properties)
    {
        super(properties.noOcclusion());
    }
}
