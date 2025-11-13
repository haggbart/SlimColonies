package no.monopixel.slimcolonies.core.colony.buildings.modules;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.colony.buildings.modules.AbstractBuildingModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.IPersistentModule;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Module for managing which ores the miner should prioritize when generating ores.
 */
public class MinerOrePriorityModule extends AbstractBuildingModule implements IPersistentModule
{
    /**
     * Maximum number of ore types per building level.
     */
    public static final int ORES_PER_LEVEL = 3;

    // Ore type tags for building level requirements
    private static final TagKey<Block> TAG_ORES_GOLD     = TagKey.create(ForgeRegistries.Keys.BLOCKS, ResourceLocation.fromNamespaceAndPath("forge", "ores/gold"));
    private static final TagKey<Block> TAG_ORES_LAPIS    = TagKey.create(ForgeRegistries.Keys.BLOCKS, ResourceLocation.fromNamespaceAndPath("forge", "ores/lapis"));
    private static final TagKey<Block> TAG_ORES_REDSTONE = TagKey.create(ForgeRegistries.Keys.BLOCKS, ResourceLocation.fromNamespaceAndPath("forge", "ores/redstone"));
    private static final TagKey<Block> TAG_ORES_DIAMOND  = TagKey.create(ForgeRegistries.Keys.BLOCKS, ResourceLocation.fromNamespaceAndPath("forge", "ores/diamond"));
    private static final TagKey<Block> TAG_ORES_EMERALD  = TagKey.create(ForgeRegistries.Keys.BLOCKS, ResourceLocation.fromNamespaceAndPath("forge", "ores/emerald"));

    private static final String           TAG_PRIORITY_ORES = "priorityOres";
    protected final      Set<ItemStorage> priorityOres      = new HashSet<>();

    public Set<ItemStorage> getPriorityOres()
    {
        return priorityOres;
    }

    public void addPriorityOre(final ItemStack oreStack)
    {
        if (priorityOres.size() >= building.getBuildingLevel() * ORES_PER_LEVEL)
        {
            return;
        }

        if (!isOreValidForBuilding(oreStack, building.getBuildingLevel()))
        {
            return;
        }

        final boolean added = priorityOres.add(new ItemStorage(oreStack));
        if (added)
        {
            markDirty();
        }
    }

    private boolean isInMinableOresList(final ItemStack oreStack)
    {
        for (final ItemStorage storage : IColonyManager.getInstance().getCompatibilityManager().getMinableOres())
        {
            if (ItemStack.isSameItem(storage.getItemStack(), oreStack))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isOreValidForBuilding(final ItemStack oreStack, final int buildingLevel)
    {
        if (!(oreStack.getItem() instanceof BlockItem blockItem))
        {
            return false;
        }

        if (!isInMinableOresList(oreStack))
        {
            return false;
        }

        final BlockState state = blockItem.getBlock().defaultBlockState();

        if (state.is(TAG_ORES_GOLD) && buildingLevel < 2)
        {
            return false;
        }
        if ((state.is(TAG_ORES_LAPIS) || state.is(TAG_ORES_REDSTONE)) && buildingLevel < 3)
        {
            return false;
        }
        if (state.is(TAG_ORES_DIAMOND) && buildingLevel < 4)
        {
            return false;
        }
        if (state.is(TAG_ORES_EMERALD) && buildingLevel < 5)
        {
            return false;
        }

        return true;
    }

    public void removePriorityOre(final ItemStack oreStack)
    {
        final boolean removed = priorityOres.remove(new ItemStorage(oreStack));
        if (removed)
        {
            markDirty();
        }
    }

    public boolean isEmpty()
    {
        return priorityOres.isEmpty();
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        priorityOres.clear();
        final ListTag oreTagList = compound.getList(TAG_PRIORITY_ORES, Tag.TAG_COMPOUND);
        for (int i = 0; i < oreTagList.size(); i++)
        {
            final ItemStack oreStack = ItemStack.of(oreTagList.getCompound(i));
            if (!oreStack.isEmpty())
            {
                priorityOres.add(new ItemStorage(oreStack));
            }
        }
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        @NotNull final ListTag oreTagList = new ListTag();
        for (final ItemStorage ore : priorityOres)
        {
            oreTagList.add(ore.getItemStack().save(new CompoundTag()));
        }
        compound.put(TAG_PRIORITY_ORES, oreTagList);
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(priorityOres.size());
        for (final ItemStorage ore : priorityOres)
        {
            buf.writeItem(ore.getItemStack());
        }
    }
}
