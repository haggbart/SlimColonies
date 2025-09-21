package com.minecolonies.api.research;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

/**
 * Stub class - research costs have been removed from SlimColonies
 * This class exists only to prevent compilation errors during transition
 */
public class ModResearchCosts
{
    // Empty registry - costs are no longer used
    public static final DeferredRegister<ResearchCostEntry> DEFERRED_REGISTER = null;

    // Stub constants for backwards compatibility
    public static final ResourceLocation SIMPLE_ITEM_COST_ID = new ResourceLocation("minecolonies", "item_simple");
    public static final ResourceLocation LIST_ITEM_COST_ID = new ResourceLocation("minecolonies", "item_list");
    public static final ResourceLocation TAG_ITEM_COST_ID = new ResourceLocation("minecolonies", "item_tag");

    /**
     * Stub class for research cost entries
     */
    public static class ResearchCostEntry
    {
        // Empty stub - not used anymore
    }
}