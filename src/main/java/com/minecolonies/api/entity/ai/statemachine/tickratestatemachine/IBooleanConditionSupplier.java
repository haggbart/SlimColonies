package com.minecolonies.api.entity.ai.statemachine.tickratestatemachine;

import java.io.Serializable;

/**
 * Serializable version of a boolean supplier for AI transitions, used for name generation
 */
@FunctionalInterface
public interface IBooleanConditionSupplier extends Serializable
{
    /**
     * Gets a result.
     *
     * @return a result
     */
    boolean getAsBoolean();
}
