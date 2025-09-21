package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventTypeRegistryEntry;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

/**
 * Initializer for colony event types, register new event types here.
 */
public final class ModColonyEventTypeInitializer
{
    public final static DeferredRegister<ColonyEventTypeRegistryEntry> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "colonyeventtypes"), Constants.MOD_ID);

    private ModColonyEventTypeInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModColonyEventTypeInitializer but this is a Utility class.");
    }

    static
    {
        // All raid event registrations removed for SlimColonies
    }
}
