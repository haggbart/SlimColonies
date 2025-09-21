package com.minecolonies.api.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.ICitizenData;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * Citizen injury treatment handler interface.
 */
public interface ICitizenInjuryHandler
{
    /**
     * To tick the handler.
     */
    void update(final int tickRate);


    /**
     * Write the handler to NBT.
     *
     * @param compound the nbt to write it to.
     */
    void write(final CompoundTag compound);

    /**
     * Read the handler from NBT.
     *
     * @param compound the nbt to read it from.
     */
    void read(final CompoundTag compound);


    /**
     * Cure/treat the citizen.
     */
    void cure();


    /**
     * True when the citizen needs to go to a hospital because its hurt
     * @return
     */
    boolean isHurt();

    /**
     * Whether the citizen sleeps at a hospital
     * @return
     */
    boolean sleepsAtHospital();

    /**
     * Sets a flag that the citizen is now at the hospital.
     */
    void setSleepsAtHospital(final boolean isAtHospital);

}
