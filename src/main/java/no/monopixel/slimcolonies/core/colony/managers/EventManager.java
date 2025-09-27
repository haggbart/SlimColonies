package no.monopixel.slimcolonies.core.colony.managers;

import no.monopixel.slimcolonies.api.MinecoloniesAPIProxy;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.colonyEvents.IColonyEvent;
import no.monopixel.slimcolonies.api.colony.colonyEvents.registry.ColonyEventTypeRegistryEntry;
import no.monopixel.slimcolonies.api.colony.managers.interfaces.IEventManager;
import no.monopixel.slimcolonies.api.util.Log;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static no.monopixel.slimcolonies.api.colony.colonyEvents.EventStatus.*;
import static no.monopixel.slimcolonies.api.util.constant.Constants.MOD_ID;
import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_NAME;

/**
 * Manager for all colony related events.
 */
public class EventManager implements IEventManager
{
    /**
     * NBT tags
     */
    private static final String TAG_EVENT_ID        = "event_currentID";
    private static final String TAG_EVENT_MANAGER   = "event_manager";
    private static final String TAG_EVENT_LIST      = "events_list";

    /**
     * The current event ID this colony is at, unique for each event
     */
    private int currentEventID = 1;

    /**
     * Colony reference
     */
    private final IColony colony;

    /**
     * The current events on the colony, with unique ID and event.
     */
    private final Map<Integer, IColonyEvent> events = new HashMap<>();

    /**
     * The related structure manager, which takes care of structures for the events.
     */

    public EventManager(final IColony colony)
    {
        this.colony = colony;
    }

    @Override
    public void addEvent(final IColonyEvent colonyEvent)
    {
        if (colonyEvent.getID() == 0)
        {
            Log.getLogger().warn("missing ID for event:" + colonyEvent.getEventTypeID().getPath());
            return;
        }
        events.put(colonyEvent.getID(), colonyEvent);
        colony.markDirty();
    }

    /**
     * Increments the id, and returns the taken id.
     *
     * @return the next event Id.
     */
    @Override
    public int getAndTakeNextEventID()
    {
        if (currentEventID > Integer.MAX_VALUE - 100)
        {
            currentEventID = 1;
        }

        currentEventID++;
        colony.markDirty();
        return currentEventID - 1;
    }


    /**
     * Gets an event by its ID
     *
     * @param ID the id of the event.
     * @return the event or null.
     */
    @Override
    public IColonyEvent getEventByID(final int ID)
    {
        return events.get(ID);
    }

    /**
     * Updates the current events.
     *
     * @param colony the colony being ticked.
     */
    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        final Iterator<IColonyEvent> iterator = events.values().iterator();
        while (iterator.hasNext())
        {
            final IColonyEvent event = iterator.next();

            if (event.getStatus() == DONE)
            {
                event.onFinish();
                colony.markDirty();
                iterator.remove();
            }
            else if (event.getStatus() == STARTING)
            {
                event.onStart();
            }
            else if (event.getStatus() == CANCELED)
            {
                colony.markDirty();
                iterator.remove();
            }
            else
            {
                event.onUpdate();
            }
        }
    }

    @Override
    public Map<Integer, IColonyEvent> getEvents()
    {
        return events;
    }

    @Override
    public void readFromNBT(@NotNull final CompoundTag compound)
    {
        if (compound.contains(TAG_EVENT_MANAGER))
        {
            final CompoundTag eventManagerNBT = compound.getCompound(TAG_EVENT_MANAGER);
            final ListTag eventListNBT = eventManagerNBT.getList(TAG_EVENT_LIST, Tag.TAG_COMPOUND);
            for (final Tag base : eventListNBT)
            {
                final CompoundTag tagCompound = (CompoundTag) base;
                final ResourceLocation eventTypeID = new ResourceLocation(MOD_ID, tagCompound.getString(TAG_NAME));

                final ColonyEventTypeRegistryEntry registryEntry = MinecoloniesAPIProxy.getInstance().getColonyEventRegistry().getValue(eventTypeID);
                if (registryEntry == null)
                {
                    Log.getLogger().warn("Event is missing registryEntry!:" + eventTypeID.getPath());
                    continue;
                }

                final IColonyEvent colonyEvent = registryEntry.deserializeEvent(colony, tagCompound);
                events.put(colonyEvent.getID(), colonyEvent);
            }

            currentEventID = eventManagerNBT.getInt(TAG_EVENT_ID);
        }
    }

    @Override
    public void writeToNBT(@NotNull final CompoundTag compound)
    {
        final CompoundTag eventManagerNBT = new CompoundTag();
        final ListTag eventListNBT = new ListTag();
        for (final IColonyEvent event : events.values())
        {
            final CompoundTag eventNBT = event.serializeNBT();
            eventNBT.putString(TAG_NAME, event.getEventTypeID().getPath());
            eventListNBT.add(eventNBT);
        }

        eventManagerNBT.putInt(TAG_EVENT_ID, currentEventID);
        eventManagerNBT.put(TAG_EVENT_LIST, eventListNBT);
        compound.put(TAG_EVENT_MANAGER, eventManagerNBT);
    }

}
