package no.monopixel.slimcolonies.core.colony.buildingextensions;


import no.monopixel.slimcolonies.api.blocks.ModBlocks;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries;
import no.monopixel.slimcolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries.BuildingExtensionEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.FIELD_STATUS;

/**
 * Field class implementation for the plantation
 */
public class FarmField extends AbstractBuildingExtensionModule
{
    /**
     * The max width/length of a field.
     */
    private static final int MAX_RANGE = 5;

    private static final String TAG_SEED         = "seed";
    private static final String TAG_RADIUS       = "radius";
    private static final String TAG_MAX_RANGE    = "maxRange";
    private static final String TAG_STAGE        = "stage";
    private static final String TAG_WATER_CROP   = "isWaterCrop";

    /**
     * The currently selected seed on the field, if any.
     */
    private ItemStack seed = ItemStack.EMPTY;

    /**
     * Cached flag indicating if the current seed is a water crop (like rice).
     * Computed once when seed is set to avoid repeated string operations.
     */
    private boolean isWaterCrop = false;

    /**
     * The size of the field in all four directions
     * in the same order as {@link Direction}:
     * S, W, N, E
     */
    private int[] radii = {MAX_RANGE, MAX_RANGE, MAX_RANGE, MAX_RANGE};

    /**
     * The maximum radius for this field.
     */
    private int maxRadius;

    /**
     * The current status of the field (READY or RESTING based on cooldown)
     */
    private Stage fieldStage = Stage.READY;

    /**
     * Constructor used in NBT deserialization.
     *
     * @param fieldType the type of field.
     * @param position  the position of the field.
     */
    public FarmField(final BuildingExtensionEntry fieldType, final BlockPos position)
    {
        super(fieldType, position);
        this.maxRadius = MAX_RANGE;
    }

    /**
     * Constructor to create new instances
     *
     * @param position the position it is placed in.
     */
    public static FarmField create(final BlockPos position)
    {
        return (FarmField) BuildingExtensionRegistries.farmField.get().produceExtension(position);
    }

    @Override
    public boolean isValidPlacement(final IColony colony)
    {
        BlockState blockState = colony.getWorld().getBlockState(getPosition());
        return blockState.is(ModBlocks.blockScarecrow);
    }

    @Override
    public @NotNull CompoundTag serializeNBT()
    {
        CompoundTag compound = super.serializeNBT();
        compound.put(TAG_SEED, seed.serializeNBT());
        compound.putIntArray(TAG_RADIUS, radii);
        compound.putInt(TAG_MAX_RANGE, maxRadius);
        compound.putString(TAG_STAGE, fieldStage.name());
        compound.putBoolean(TAG_WATER_CROP, isWaterCrop);
        return compound;
    }

    @Override
    public void deserializeNBT(final @NotNull CompoundTag compound)
    {
        super.deserializeNBT(compound);
        setSeed(ItemStack.of(compound.getCompound(TAG_SEED)));  // This will compute isWaterCrop
        radii = compound.getIntArray(TAG_RADIUS);
        maxRadius = compound.getInt(TAG_MAX_RANGE);

        // Backwards compatibility: convert old stage names to new ones
        final String stageName = compound.getString(TAG_STAGE);
        try
        {
            fieldStage = Stage.valueOf(stageName);
        }
        catch (IllegalArgumentException e)
        {
            // Old save data with EMPTY, HOED, or PLANTED - convert to READY
            fieldStage = Stage.READY;
        }

        // For backwards compatibility, recompute if not present in saved data
        // setSeed() already computed it, but this explicit check helps clarity
        if (compound.contains(TAG_WATER_CROP))
        {
            isWaterCrop = compound.getBoolean(TAG_WATER_CROP);
        }
    }

    @Override
    public void serialize(final @NotNull FriendlyByteBuf buf)
    {
        super.serialize(buf);
        buf.writeItem(getSeed());
        buf.writeVarIntArray(radii);
        buf.writeInt(maxRadius);
        buf.writeEnum(fieldStage);
        buf.writeBoolean(isWaterCrop);
    }

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        super.deserialize(buf);
        setSeed(buf.readItem());  // This will compute isWaterCrop
        radii = buf.readVarIntArray();
        maxRadius = buf.readInt();
        fieldStage = buf.readEnum(Stage.class);
        isWaterCrop = buf.readBoolean();
    }

    /**
     * Get the current seed on the field.
     *
     * @return the current seed.
     */
    @NotNull
    public ItemStack getSeed()
    {
        seed.setCount(1);
        return seed;
    }

    /**
     * Updates the seed in the field.
     *
     * @param seed the new seed
     */
    public void setSeed(final ItemStack seed)
    {
        this.seed = seed.copy();
        this.seed.setCount(1);
        this.isWaterCrop = computeIsWaterCrop(seed);
    }

    /**
     * Check if the current seed is a water crop.
     * This is cached when setSeed() is called.
     *
     * @return true if the seed is a water crop (like rice)
     */
    public boolean isWaterCrop()
    {
        return this.isWaterCrop;
    }

    /**
     * Computes whether a seed is a water crop (like rice) that needs water to grow.
     * Called once when the seed is set to cache the result.
     *
     * @param seed the seed to check
     * @return true if it's a water crop
     */
    private boolean computeIsWaterCrop(final ItemStack seed)
    {
        if (seed == null || seed.isEmpty())
        {
            return false;
        }

        final ResourceLocation itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(seed.getItem());
        if (itemId == null)
        {
            return false;
        }

        // Check if it's rice or other water-based crops
        final String itemPath = itemId.toString();
        return itemPath.contains("rice");
    }

    /**
     * Move the field into the new state.
     */
    public void nextState()
    {
        if (getFieldStage().ordinal() + 1 >= Stage.values().length)
        {
            setFieldStage(Stage.values()[0]);
            return;
        }
        setFieldStage(Stage.values()[getFieldStage().ordinal() + 1]);
    }

    /**
     * Get the current stage the field is in.
     *
     * @return the stage of the field.
     */
    public Stage getFieldStage()
    {
        return this.fieldStage;
    }

    /**
     * Sets the current stage of the field.
     *
     * @param fieldStage the stage of the field.
     */
    public void setFieldStage(final Stage fieldStage)
    {
        this.fieldStage = fieldStage;
    }

    /**
     * Get the max range for this field.
     *
     * @return the maximum range.
     */
    public int getMaxRadius()
    {
        return maxRadius;
    }

    /**
     * @param direction the direction to get the range for
     * @return the radius
     */
    public int getRadius(Direction direction)
    {
        return radii[direction.get2DDataValue()];
    }

    /**
     * @param direction the direction for the radius
     * @param radius    the number of blocks from the scarecrow that the farmer will work with
     */
    public void setRadius(Direction direction, int radius)
    {
        this.radii[direction.get2DDataValue()] = Math.min(radius, maxRadius);
    }

    /**
     * Checks if a certain position is part of the field. Complies with the definition of field block.
     *
     * @param world    the world object.
     * @param position the position.
     * @return true if it is.
     */
    public boolean isNoPartOfField(@NotNull final Level world, @NotNull final BlockPos position)
    {
        return world.isEmptyBlock(position) || isValidDelimiter(world.getBlockState(position.above()).getBlock());
    }

    /**
     * Check if a block is a valid delimiter of the field.
     *
     * @param block the block to analyze.
     * @return true if so.
     */
    private static boolean isValidDelimiter(final Block block)
    {
        return block instanceof FenceBlock || block instanceof FenceGateBlock || block instanceof WallBlock;
    }

    /**
     * Describes the status of the field based on cooldown state.
     */
    public enum Stage
    {
        READY(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/iron_hoe.png")),
        RESTING(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/clock_00.png"));

        protected final ResourceLocation stageIcon;

        private Stage(ResourceLocation stageIcon)
        {
            this.stageIcon = stageIcon;
        }

        /**
         * Gets the status icon of the current stage in the farm field's progress.
         *
         * @return the status icon of the current stage.
         */
        public ResourceLocation getStageIcon()
        {
            return stageIcon;
        }

        /**
         * Gets the translatable text of the current stage in the farm field's progress.
         *
         * @return the translatable text of the current stage.
         */
        public Component getStageText()
        {
            return Component.translatable(FIELD_STATUS + "." + name().toLowerCase(Locale.ROOT));
        }


        /**
         * Get the next stage in the field's progression.
         *
         * @return the next Stage, or the first Stage if the current one is the last.
         */
        public Stage getNextStage()
        {
            if (ordinal() + 1 >= values().length)
            {
                return values()[0];
            }
            return values()[ordinal() + 1];
        }
    }
}
