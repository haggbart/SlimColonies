package no.monopixel.slimcolonies.api.entity.citizen.citizenhandlers;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import net.minecraft.network.syncher.EntityDataAccessor;
import org.jetbrains.annotations.Nullable;

public interface ICitizenColonyHandler
{
    /**
     * calculate this worker building.
     *
     * @return the building or null if none present.
     */
    @Nullable
    IBuilding getWorkBuilding();

    @Nullable
    IBuilding getHomeBuilding();

    /**
     * Server-specific update for the EntityCitizen.
     *
     * @param colonyID  the colony id.
     * @param citizenID the citizen id.
     */
    void registerWithColony(final int colonyID, final int citizenID);

    /**
     * Update the client side of the citizen entity.
     */
    void updateColonyClient();

    /**
     * Getter for the colony.
     *
     * @return the colony of the citizen or null.
     */
    @Nullable
    IColony getColonyOrRegister();

    /**
     * Getter for the colony id.
     *
     * @return the colony id.
     */
    int getColonyId();

    /**
     * Setter for the colony id.
     *
     * @param colonyId the new colonyId.
     */
    void setColonyId(int colonyId);

    /**
     * Actions when the entity is removed.
     */
    void onCitizenRemoved();

    /**
     * Entity data update callback
     *
     * @param dataAccessor
     */
    void onSyncDataUpdate(EntityDataAccessor<?> dataAccessor);

    boolean registered();

    /**
     * Unsafe colony getter, doesn't run registration.
     * @return the colony.
     */
    IColony getColony();
}
