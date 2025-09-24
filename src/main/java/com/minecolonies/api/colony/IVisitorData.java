package com.minecolonies.api.colony;

import net.minecraft.core.BlockPos;

import java.util.UUID;

/**
 * Data for colony visitors, based on citizendata
 */
public interface IVisitorData extends ICitizenData
{

    /**
     * The position the visitor is sitting on
     *
     * @return sitting pos
     */
    BlockPos getSittingPosition();

    /**
     * Sets the sitting position
     *
     * @param pos sitting pos
     */
    void setSittingPosition(final BlockPos pos);
}
