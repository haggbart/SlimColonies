package no.monopixel.slimcolonies.core.blocks.schematic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import no.monopixel.slimcolonies.api.blocks.AbstractBlockSlimColonies;
import no.monopixel.slimcolonies.api.util.constant.Constants;

/**
 * This block is a waypoint, which makes citizens path to it.
 */
public class BlockWaypoint extends AbstractBlockSlimColonies<BlockWaypoint>
{
    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 0.0F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockwaypoint";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    /**
     * Constructor for the waypoint. Sets the creative tab, as well as the resistance and the hardness.
     */
    public BlockWaypoint()
    {
        super(Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(BLOCK_HARDNESS, RESISTANCE).noCollission());
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, BLOCK_NAME);
    }
}
