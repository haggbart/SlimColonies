package no.monopixel.slimcolonies.api.crafting;

import no.monopixel.slimcolonies.api.crafting.registry.RecipeTypeEntry;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipeTypes
{

    public static final ResourceLocation CLASSIC_ID    = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "classic");
    public static final ResourceLocation MULTI_OUTPUT_ID    = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "multi_output");

    public static RegistryObject<RecipeTypeEntry> Classic;
    public static RegistryObject<RecipeTypeEntry> MultiOutput;

    private ModRecipeTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModJobs but this is a Utility class.");
    }
}
