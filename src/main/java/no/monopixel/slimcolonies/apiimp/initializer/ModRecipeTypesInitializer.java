package no.monopixel.slimcolonies.apiimp.initializer;

import no.monopixel.slimcolonies.api.crafting.ClassicRecipe;
import no.monopixel.slimcolonies.api.crafting.ModRecipeTypes;
import no.monopixel.slimcolonies.api.crafting.MultiOutputRecipe;
import no.monopixel.slimcolonies.api.crafting.registry.RecipeTypeEntry;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

public final class ModRecipeTypesInitializer
{
    public final static DeferredRegister<RecipeTypeEntry> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "recipetypeentries"), Constants.MOD_ID);

    private ModRecipeTypesInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModRecipeTypesInitializer but this is a Utility class.");
    }

    static
    {
        ModRecipeTypes.Classic = DEFERRED_REGISTER.register(ModRecipeTypes.CLASSIC_ID.getPath(), () -> new RecipeTypeEntry.Builder()
                                .setRecipeTypeProducer(ClassicRecipe::new)
                                .setRegistryName(ModRecipeTypes.CLASSIC_ID)
                                .createRecipeTypeEntry());

        ModRecipeTypes.MultiOutput = DEFERRED_REGISTER.register(ModRecipeTypes.MULTI_OUTPUT_ID.getPath(), () -> new RecipeTypeEntry.Builder()
                                .setRecipeTypeProducer(MultiOutputRecipe::new)
                                .setRegistryName(ModRecipeTypes.MULTI_OUTPUT_ID)
                                .createRecipeTypeEntry());
    }
}
