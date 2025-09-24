package no.monopixel.slimcolonies.api.crafting.registry;

import no.monopixel.slimcolonies.api.crafting.ZeroWasteRecipe;
import no.monopixel.slimcolonies.api.crafting.CompostRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.RegistryObject;

/**
 * Holds ref to the mod recipe serializers and recipe types.
 */
public class ModRecipeSerializer
{
    public static RegistryObject<CompostRecipe.Serializer> CompostRecipeSerializer;
    public static RegistryObject<RecipeType<CompostRecipe>>   CompostRecipeType;

    public static RegistryObject<ZeroWasteRecipe.Serializer> ZeroWasteRecipeSerializer;
}
