package no.monopixel.slimcolonies.api.colony.workorders;

import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.*;

/**
 * Types of workorders
 */
public enum WorkOrderType
{
    BUILD(COREMOD_ENTITY_BUILDER_BUILD_COMPLETE),
    UPGRADE(COREMOD_ENTITY_BUILDER_BUILD_COMPLETE),
    REPAIR(COREMOD_ENTITY_BUILDER_REPAIRING_COMPLETE),
    REMOVE(COREMOD_ENTITY_BUILDER_DECONSTRUCTION_COMPLETE);

    /**
     * Translation message for completion
     */
    private final String completionMessageID;

    WorkOrderType(final String completionMessage)
    {
        this.completionMessageID = completionMessage;
    }

    /**
     * Translation constant for the message type
     *
     * @return
     */
    public String getCompletionMessageID()
    {
        return completionMessageID;
    }
}
