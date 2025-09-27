package no.monopixel.slimcolonies.api.colony.managers.interfaces;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.colonyEvents.IColonyEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Interface for the event manager, the event manager deals with all colony related events, such as raid events.
 */
public interface IEventManager
{
    /**
     * Adds an event
     *
     * @param colonyEvent event to add
     */
    void addEvent(IColonyEvent colonyEvent);

    /**
     * Gets and takes the next open event ID.
     *
     * @return int ID
     */
    int getAndTakeNextEventID();

    /**
     * Update function, which is called from the colony every 500 ticks. Used to update event states/remove them if needed. Forwarded to events aswell to allow them tick based
     * logic
     *
     * @param colony the colony to tick.
     */
    void onColonyTick(@NotNull IColony colony);

    /**
     * Gets an event by its id.
     *
     * @param ID event ID to get
     * @return event
     */
    IColonyEvent getEventByID(int ID);

    /**
     * Returns the full event Map
     *
     * @return the map of events per colony.
     */
    Map<Integer, IColonyEvent> getEvents();

    /**
     * Reads the eventManager nbt and creates events from it
     *
     * @param compound the compound to read from.
     */
    void readFromNBT(@NotNull CompoundTag compound);

    /**
     * Write the eventmanager and all events to NBT
     *
     * @param compound the compound to write to.
     */
    void writeToNBT(@NotNull CompoundTag compound);

}
