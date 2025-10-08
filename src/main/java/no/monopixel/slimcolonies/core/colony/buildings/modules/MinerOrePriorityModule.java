package no.monopixel.slimcolonies.core.colony.buildings.modules;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
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

    private static final String           TAG_PRIORITY_ORES = "priorityOres";
    protected final      Set<ItemStorage> priorityOres      = new HashSet<>();

    public Set<ItemStorage> getPriorityOres()
    {
        return priorityOres;
    }

    /**
     * @return true if added successfully, false if list is full.
     */
    public boolean addPriorityOre(final ItemStack oreStack)
    {
        if (priorityOres.size() >= building.getBuildingLevel() * ORES_PER_LEVEL)
        {
            return false;
        }

        final boolean added = priorityOres.add(new ItemStorage(oreStack));
        if (added)
        {
            markDirty();
        }
        return added;
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
