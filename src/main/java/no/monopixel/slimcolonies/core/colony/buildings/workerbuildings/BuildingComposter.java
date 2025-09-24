package no.monopixel.slimcolonies.core.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableList;
import no.monopixel.slimcolonies.api.blocks.ModBlocks;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.modules.settings.ISettingKey;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.modules.ItemListModule;
import no.monopixel.slimcolonies.core.colony.buildings.modules.settings.BoolSetting;
import no.monopixel.slimcolonies.core.colony.buildings.modules.settings.IntSetting;
import no.monopixel.slimcolonies.core.colony.buildings.modules.settings.SettingKey;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static no.monopixel.slimcolonies.core.entity.ai.workers.production.agriculture.EntityAIWorkComposter.COMPOSTABLE_LIST;

public class BuildingComposter extends AbstractBuilding
{
    /**
     * Settings key for the composting mode.
     */
    public static final ISettingKey<BoolSetting> PRODUCE_DIRT = new SettingKey<>(BoolSetting.class, new ResourceLocation(Constants.MOD_ID, "producedirt"));

    /**
     * Key for min remainder at warehouse.
     */
    public static final ISettingKey<IntSetting> MIN = new SettingKey<>(IntSetting.class, new ResourceLocation(Constants.MOD_ID, "warehousemin"));

    /**
     * Description of the job for this building
     */
    private static final String COMPOSTER = "composter";

    /**
     * Maximum building level
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Tag to store the barrel position.
     */
    private static final String TAG_POS = "pos";

    /**
     * Tag to store the barrel list.
     */
    private static final String TAG_BARRELS = "barrels";

    /**
     * List of registered barrels.
     */
    private final List<BlockPos> barrels = new ArrayList<>();

    /**
     * The constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingComposter(@NotNull final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put((stack) -> this.getModuleMatching(ItemListModule.class, m -> m.getId().equals(COMPOSTABLE_LIST)).isItemInList(new ItemStorage(stack)), new Tuple<>(Integer.MAX_VALUE, true));
    }

    /**
     * Return a list of barrels assigned to this hut.
     *
     * @return copy of the list
     */
    public List<BlockPos> getBarrels()
    {
        return ImmutableList.copyOf(barrels);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return COMPOSTER;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final Level world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block == ModBlocks.blockBarrel && !barrels.contains(pos))
        {
            barrels.add(pos);
        }
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        final ListTag compostBinTagList = compound.getList(TAG_BARRELS, Tag.TAG_COMPOUND);
        for (int i = 0; i < compostBinTagList.size(); ++i)
        {
            barrels.add(NbtUtils.readBlockPos(compostBinTagList.getCompound(i).getCompound(TAG_POS)));
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();
        @NotNull final ListTag compostBinTagList = new ListTag();
        for (@NotNull final BlockPos entry : barrels)
        {
            @NotNull final CompoundTag compostBinCompound = new CompoundTag();
            compostBinCompound.put(TAG_POS, NbtUtils.writeBlockPos(entry));
            compostBinTagList.add(compostBinCompound);
        }
        compound.put(TAG_BARRELS, compostBinTagList);
        return compound;
    }
}
