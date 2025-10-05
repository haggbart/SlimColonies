package no.monopixel.slimcolonies.api.blocks;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.IForgeRegistry;
import no.monopixel.slimcolonies.api.blocks.interfaces.IBlockSlimColonies;

public abstract class AbstractBlockSlimColonies<B extends AbstractBlockSlimColonies<B>> extends Block implements IBlockSlimColonies<B>
{
    public AbstractBlockSlimColonies(final Properties properties)
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
