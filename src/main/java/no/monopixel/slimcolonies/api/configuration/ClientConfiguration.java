package no.monopixel.slimcolonies.api.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Mod client configuration. Loaded clientside, not synced.
 */
public class ClientConfiguration extends AbstractConfiguration
{
    public final ForgeConfigSpec.BooleanValue citizenVoices;
    public final ForgeConfigSpec.BooleanValue neighborbuildingrendering;
    public final ForgeConfigSpec.IntValue neighborbuildingrange;
    public final ForgeConfigSpec.IntValue buildgogglerange;
    public final ForgeConfigSpec.BooleanValue colonyteamborders;
    public final ForgeConfigSpec.BooleanValue holidayFeatures;

    /**
     * Builds client configuration.
     *
     * @param builder config builder
     */
    protected ClientConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "gameplay");
        citizenVoices = defineBoolean(builder, "enablecitizenvoices", true);
        neighborbuildingrendering = defineBoolean(builder, "neighborbuildingrendering", true);
        neighborbuildingrange = defineInteger(builder, "neighborbuildingrange", 4, -2, 16);
        buildgogglerange = defineInteger(builder, "buildgogglerange", 50, 1, 250);
        colonyteamborders = defineBoolean(builder, "colonyteamborders", true);
        holidayFeatures = defineBoolean(builder, "holidayfeatures", true);

        swapToCategory(builder, "pathfinding");

        finishCategory(builder);
    }
}
