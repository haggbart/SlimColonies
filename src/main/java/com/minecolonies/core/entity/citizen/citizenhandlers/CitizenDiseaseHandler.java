package com.minecolonies.core.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenDiseaseHandler;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.StatisticsConstants.CITIZENS_HEALED;

/**
 * Handler taking care of citizen injuries and hospital treatment.
 */
public class CitizenDiseaseHandler implements ICitizenDiseaseHandler
{
    /**
     * Health at which citizens seek a doctor.
     */
    public static final double SEEK_DOCTOR_HEALTH = 6.0;

    /**
     * Number of ticks after treatment a citizen has immunity from needing treatment. 90 Minutes currently.
     */
    private static final int IMMUNITY_TIME = 20 * 60 * 90;

    /**
     * The citizen assigned to this manager.
     */
    private final ICitizenData citizenData;

    /**
     * Special immunity time after being treated.
     */
    private int immunityTicks = 0;

    /**
     * Whether the citizen sleeps at the hospital
     */
    private boolean sleepsAtHospital = false;

    /**
     * Constructor for the experience handler.
     *
     * @param citizen the citizen owning the handler.
     */
    public CitizenDiseaseHandler(final ICitizenData citizen)
    {
        this.citizenData = citizen;
    }

    /**
     * Called in the citizen every few ticks to update immunity status. Called every 60 ticks
     */
    @Override
    public void update(final int tickRate)
    {
        if (immunityTicks > 0)
        {
            immunityTicks -= tickRate;
        }
    }



    @Override
    public boolean isHurt()
    {
        return citizenData.getEntity().isPresent() && !(citizenData.getJob() instanceof AbstractJobGuard) && citizenData.getEntity().get().getHealth() < SEEK_DOCTOR_HEALTH
            && citizenData.getSaturation() > LOW_SATURATION;
    }


    @Override
    public void write(final CompoundTag compound)
    {
        CompoundTag treatmentTag = new CompoundTag();
        treatmentTag.putInt(TAG_IMMUNITY, immunityTicks);
        compound.put(TAG_DISEASE, treatmentTag);
    }

    @Override
    public void read(final CompoundTag compound)
    {
        if (!compound.contains(TAG_DISEASE, Tag.TAG_COMPOUND))
        {
            return;
        }

        CompoundTag treatmentTag = compound.getCompound(TAG_DISEASE);
        this.immunityTicks = treatmentTag.getInt(TAG_IMMUNITY);
    }


    @Override
    public void cure()
    {
        sleepsAtHospital = false;
        if (citizenData.isAsleep() && citizenData.getEntity().isPresent())
        {
            citizenData.getEntity().get().stopSleeping();
            // Full immunity time for proper hospital treatment
            immunityTicks = IMMUNITY_TIME;
        }
        else
        {
            // Less immunity time if not treated in bed, but still have immunity time.
            immunityTicks = IMMUNITY_TIME / 2;
        }

        citizenData.getColony().getStatisticsManager().increment(CITIZENS_HEALED, citizenData.getColony().getDay());
        citizenData.markDirty(0);
    }

    @Override
    public boolean sleepsAtHospital()
    {
        return sleepsAtHospital;
    }

    @Override
    public void setSleepsAtHospital(final boolean isAtHospital)
    {
        sleepsAtHospital = isAtHospital;
    }
}
