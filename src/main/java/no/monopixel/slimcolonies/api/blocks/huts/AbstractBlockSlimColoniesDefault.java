package no.monopixel.slimcolonies.api.blocks.huts;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraftforge.registries.IForgeRegistry;
import no.monopixel.slimcolonies.api.blocks.AbstractBlockSlimColoniesContainer;

public abstract class AbstractBlockSlimColoniesDefault<B extends AbstractBlockSlimColoniesDefault<B>> extends AbstractBlockSlimColoniesContainer<B>
{
    /**
     * The position it faces.
     */
    public static final DirectionProperty FACING           = HorizontalDirectionalBlock.FACING;
    /**
     * Hardness of the block.
     */
    public static final float             HARDNESS         = 10F;
    /**
     * Resistance of the block.
     */
    public static final float             RESISTANCE       = 10F;
    /**
     * Start of the collision box at y.
     */
    public static final double            BOTTOM_COLLISION = 0.0;
    /**
     * Start of the collision box at x and z.
     */
    public static final double            START_COLLISION  = 0.1;
    /**
     * End of the collision box.
     */
    public static final double            END_COLLISION    = 0.9;
    /**
     * Height of the collision box.
     */
    public static final double            HEIGHT_COLLISION = 2.2;
    /**
     * Registry name for this block.
     */
    public static final String            REGISTRY_NAME    = "blockhutfield";

    public AbstractBlockSlimColoniesDefault(final Properties properties)
    {
        super(properties);
    }

    @Override
    public B registerBlock(final IForgeRegistry<Block> registry)
    {
        registry.register(getRegistryName(), this);
        return (B) this;
    }

    @Override
    public void registerBlockItem(final IForgeRegistry<Item> registry, final Item.Properties properties)
    {
        registry.register(getRegistryName(), new BlockItem(this, properties));
    }
}
