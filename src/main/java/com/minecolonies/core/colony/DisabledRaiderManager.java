package com.minecolonies.core.colony;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.managers.interfaces.IRaiderManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.List;

/**
 * Disabled implementation of IRaiderManager for SlimColonies
 * All raid functionality has been removed
 */
public class DisabledRaiderManager implements IRaiderManager
{
    @Override
    public boolean canHaveRaiderEvents()
    {
        return false;
    }

    @Override
    public boolean willRaidTonight()
    {
        return false;
    }

    @Override
    public void setCanHaveRaiderEvents(boolean canHave)
    {
        // No-op
    }

    @Override
    public void setRaidNextNight(boolean willRaid, String raidType, boolean allowShips)
    {
        // No-op
    }

    @Override
    public boolean areSpiesEnabled()
    {
        return false;
    }

    @Override
    public void setSpiesEnabled(boolean enabled)
    {
        // No-op
    }

    @Override
    public void raiderEvent()
    {
        // No-op
    }

    @Override
    public RaidSpawnResult raiderEvent(String raidType, boolean forced, boolean allowShips)
    {
        return RaidSpawnResult.CANNOT_RAID;
    }

    @Override
    public BlockPos calculateSpawnLocation()
    {
        return null;
    }

    @Override
    public List<BlockPos> getLastSpawnPoints()
    {
        return List.of();
    }

    @Override
    public int calculateRaiderAmount(int raidLevel)
    {
        return 0;
    }

    @Override
    public boolean isRaided()
    {
        return false;
    }

    @Override
    public void onNightFall()
    {
        // No-op
    }

    @Override
    public int getNightsSinceLastRaid()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setNightsSinceLastRaid(int nightsSinceLastRaid)
    {
        // No-op
    }

    @Override
    public boolean canRaid()
    {
        return false;
    }

    @Override
    public int getColonyRaidLevel()
    {
        return 0;
    }

    @Override
    public BlockPos getRandomBuilding()
    {
        return BlockPos.ZERO;
    }

    @Override
    public double getRaidDifficultyModifier()
    {
        return 0.0;
    }

    @Override
    public void onLostCitizen(ICitizenData citizen)
    {
        // No-op
    }

    @Override
    public void write(CompoundTag compound)
    {
        // No-op
    }

    @Override
    public void read(CompoundTag compound)
    {
        // No-op
    }

    @Override
    public int getLostCitizen()
    {
        return 0;
    }


    @Override
    public void setPassThroughRaid()
    {
        // No-op
    }
}