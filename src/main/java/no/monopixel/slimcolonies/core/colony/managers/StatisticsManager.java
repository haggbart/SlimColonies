package no.monopixel.slimcolonies.core.colony.managers;

import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import no.monopixel.slimcolonies.api.colony.managers.interfaces.IStatisticsManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.*;

/**
 * Manager for colony related statistics.
 */
public class StatisticsManager implements IStatisticsManager
{
    /**
     * NBT tags.
     */
    private static final String TAG_STAT_MANAGER = "stat_manager";
    private static final String TAG_STAT         = "stat";

    /**
     * The current stats of the colony.
     */
    private final Map<String, Int2IntLinkedOpenHashMap> stats = new HashMap<>();

    /**
     * The modified and not yet sent stats
     */
    private Set<String> dirtyStats = new HashSet<>();

    @Override
    public void increment(final @NotNull String id, final int day)
    {
        incrementBy(id, 1, day);
    }

    @Override
    public void incrementBy(final @NotNull String id, int qty, final int day)
    {
        final Int2IntLinkedOpenHashMap innerMap = stats.computeIfAbsent(id, k -> new Int2IntLinkedOpenHashMap());
        innerMap.addTo(day, qty);
        dirtyStats.add(id);
    }

    @Override
    public @NotNull Map<String, Integer> getStats()
    {
        final Map<String, Integer> result = new HashMap<>();

        for (final Map.Entry<String, Int2IntLinkedOpenHashMap> entry : stats.entrySet())
        {
            int total = 0;
            for (final int count : entry.getValue().values())
            {
                total += count;
            }

            if (total > 0)
            {
                result.put(entry.getKey(), total);
            }
        }

        return result;
    }

    @Override
    public @NotNull Map<String, Integer> getStats(final int startDay, final int endDay)
    {
        final Map<String, Integer> result = new HashMap<>();

        for (final Map.Entry<String, Int2IntLinkedOpenHashMap> entry : stats.entrySet())
        {
            final Int2IntLinkedOpenHashMap dayMap = entry.getValue();
            int count = 0;

            for (int day = startDay; day <= endDay; day++)
            {
                count += dayMap.get(day);
            }

            if (count > 0)
            {
                result.put(entry.getKey(), count);
            }
        }

        return result;
    }

    /**
     * Gets all the current stat entries in this manager.
     *
     * @return a set of entries with the id and the stats map.
     */
    @Override
    public @NotNull Set<Map.Entry<String, Int2IntLinkedOpenHashMap>> getStatEntries()
    {
        return stats.entrySet();
    }

    /**
     * Clear all the statistics, this will remove all the entries from the map
     */
    @Override
    public void clear()
    {
        stats.clear();
        dirtyStats = new HashSet<>();
    }

    @Override
    public void serialize(@NotNull final FriendlyByteBuf buf, final boolean fullSync)
    {
        buf.writeBoolean(fullSync);
        buf.writeVarInt(fullSync ? stats.size() : dirtyStats.size());

        if (fullSync)
        {
            for (final Map.Entry<String, Int2IntLinkedOpenHashMap> dataEntry : stats.entrySet())
            {
                buf.writeUtf(dataEntry.getKey());
                buf.writeVarInt(dataEntry.getValue().size());

                for (final Int2IntMap.Entry valueEntry : dataEntry.getValue().int2IntEntrySet())
                {
                    buf.writeVarInt(valueEntry.getIntKey());
                    buf.writeVarInt(valueEntry.getIntValue());
                }
            }
        }
        else
        {
            for (final String id : dirtyStats)
            {
                var dataEntry = stats.get(id);

                buf.writeUtf(id);
                buf.writeVarInt(1);
                buf.writeVarInt(dataEntry.lastIntKey());
                buf.writeVarInt(dataEntry.get(dataEntry.lastIntKey()));
            }
        }

        if (!dirtyStats.isEmpty())
        {
            dirtyStats = new HashSet<>();
        }
    }

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        final boolean fullSync = buf.readBoolean();
        if (fullSync)
        {
            stats.clear();
        }

        final int statSize = buf.readVarInt();
        for (int i = 0; i < statSize; i++)
        {
            final String id = buf.readUtf();
            final int statEntrySize = buf.readVarInt();

            final Int2IntLinkedOpenHashMap statValues = (fullSync || !stats.containsKey(id)) ? new Int2IntLinkedOpenHashMap(statEntrySize) : stats.get(id);
            for (int j = 0; j < statEntrySize; j++)
            {
                statValues.put(buf.readVarInt(), buf.readVarInt());
            }

            stats.put(id, statValues);
        }
    }

    @Override
    public void writeToNBT(@NotNull final CompoundTag compound)
    {
        final ListTag statManagerNBT = new ListTag();
        for (final Map.Entry<String, Int2IntLinkedOpenHashMap> stat : stats.entrySet())
        {
            final CompoundTag statCompound = new CompoundTag();
            statCompound.putString(TAG_ID, stat.getKey());

            final ListTag statNBT = new ListTag();
            for (final Int2IntMap.Entry dailyStats : stat.getValue().int2IntEntrySet())
            {
                final CompoundTag timeStampTag = new CompoundTag();

                timeStampTag.putInt(TAG_TIME, dailyStats.getIntKey());
                timeStampTag.putInt(TAG_QUANTITY, dailyStats.getIntValue());

                statNBT.add(timeStampTag);
            }

            statCompound.put(TAG_STAT, statNBT);
            statManagerNBT.add(statCompound);
        }

        compound.put(TAG_STAT_MANAGER, statManagerNBT);
    }

    @Override
    public void readFromNBT(@NotNull final CompoundTag compound)
    {
        stats.clear();
        if (compound.contains(TAG_STAT_MANAGER))
        {
            final ListTag statsNbts = compound.getList(TAG_STAT_MANAGER, Tag.TAG_COMPOUND);
            for (int i = 0; i < statsNbts.size(); i++)
            {
                final CompoundTag statCompound = statsNbts.getCompound(i);
                final String id = statCompound.getString(TAG_ID);
                final ListTag timeStampNbts = statCompound.getList(TAG_STAT, Tag.TAG_COMPOUND);
                final Int2IntLinkedOpenHashMap timeStamps = new Int2IntLinkedOpenHashMap();
                for (int j = 0; j < timeStampNbts.size(); j++)
                {
                    final CompoundTag compoundTag = timeStampNbts.getCompound(j);
                    final int day = compoundTag.getInt(TAG_TIME);
                    final int qty = compoundTag.getInt(TAG_QUANTITY);

                    timeStamps.put(day, qty);
                }

                stats.put(id, timeStamps);
            }
        }
    }
}
