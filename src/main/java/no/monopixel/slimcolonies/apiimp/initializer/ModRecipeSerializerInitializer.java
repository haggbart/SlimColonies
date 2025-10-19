package no.monopixel.slimcolonies.apiimp.initializer;

import no.monopixel.slimcolonies.api.crafting.ZeroWasteRecipe;
import no.monopixel.slimcolonies.api.crafting.CompostRecipe;
import no.monopixel.slimcolonies.api.crafting.registry.ModRecipeSerializer;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModRecipeSerializerInitializer
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Constants.MOD_ID);
    public static final DeferredRegister<RecipeType<?>>       RECIPE_TYPES      = DeferredRegister.create(Registries.RECIPE_TYPE, Constants.MOD_ID);

    static
    {
        ModRecipeSerializer.CompostRecipeSerializer = RECIPE_SERIALIZER.register("composting", CompostRecipe.Serializer::new);
        ModRecipeSerializer.CompostRecipeType = RECIPE_TYPES.register("composting", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "composting")));

        ModRecipeSerializer.ZeroWasteRecipeSerializer = RECIPE_SERIALIZER.register("zero_waste", ZeroWasteRecipe.Serializer::new);
    }

    private ModRecipeSerializerInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModRecipeSerializerInitializer but this is a Utility class.");
    }
}
