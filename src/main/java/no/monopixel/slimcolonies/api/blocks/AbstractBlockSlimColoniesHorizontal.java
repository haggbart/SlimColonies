package no.monopixel.slimcolonies.api.blocks;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraftforge.registries.IForgeRegistry;
import no.monopixel.slimcolonies.api.blocks.interfaces.IBlockSlimColonies;

public abstract class AbstractBlockSlimColoniesHorizontal<B extends AbstractBlockSlimColoniesHorizontal<B>> extends HorizontalDirectionalBlock implements IBlockSlimColonies<B>
{
    public AbstractBlockSlimColoniesHorizontal(final Properties properties)
    {
        super(properties);
    }

    @Override
    public void registerBlockItem(final IForgeRegistry<Item> registry, final Item.Properties properties)
    {
        registry.register(getRegistryName(), new BlockItem(this, properties));
    }

    @Override
    public B registerBlock(final IForgeRegistry<Block> registry)
    {
        registry.register(getRegistryName(), this);
        return (B) this;
    }
}
