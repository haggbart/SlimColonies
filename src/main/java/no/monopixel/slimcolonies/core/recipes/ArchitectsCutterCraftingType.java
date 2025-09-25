package no.monopixel.slimcolonies.core.recipes;

import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.ldtteam.domumornamentum.recipe.ModRecipeTypes;
import com.ldtteam.domumornamentum.recipe.architectscutter.ArchitectsCutterRecipe;
import no.monopixel.slimcolonies.api.crafting.GenericRecipe;
import no.monopixel.slimcolonies.api.crafting.IGenericRecipe;
import no.monopixel.slimcolonies.api.crafting.ModCraftingTypes;
import no.monopixel.slimcolonies.api.crafting.RecipeCraftingType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ArchitectsCutterCraftingType extends RecipeCraftingType<Container, ArchitectsCutterRecipe>
{
    public ArchitectsCutterCraftingType()
    {
        super(ModCraftingTypes.ARCHITECTS_CUTTER_ID, ModRecipeTypes.ARCHITECTS_CUTTER.get(), null);
    }

    @Override
    public @NotNull List<IGenericRecipe> findRecipes(@NotNull RecipeManager recipeManager, @Nullable Level world)
    {
        final Random rnd = new Random();
        final List<IGenericRecipe> recipes = new ArrayList<>();
        for (final ArchitectsCutterRecipe recipe : recipeManager.getAllRecipesFor(ModRecipeTypes.ARCHITECTS_CUTTER.get()))
        {
            // cutter recipes don't implement getIngredients(), so we have to work around it
            final Block generatedBlock = ForgeRegistries.BLOCKS.getValue(recipe.getBlockName());

            if (!(generatedBlock instanceof final IMateriallyTexturedBlock materiallyTexturedBlock))
                continue;

            final List<List<ItemStack>> inputs = new ArrayList<>();
            for (final IMateriallyTexturedBlockComponent component : materiallyTexturedBlock.getComponents())
            {
                final List<Block> blocks = ForgeRegistries.BLOCKS.tags().getTag(component.getValidSkins()).stream()
                        .collect(Collectors.toCollection(ArrayList::new));
                Collections.shuffle(blocks, rnd);
                inputs.add(blocks.stream().map(ItemStack::new).collect(Collectors.toList()));
            }

            final ItemStack output = recipe.getResultItem(world.registryAccess()).copy();
            output.setCount(Math.max(recipe.getCount(), inputs.size()));

            // resultItem usually doesn't have textureData, but we need it to properly match the creative tab
            if (!output.getOrCreateTag().contains("textureData"))
            {
                assert output.getTag() != null;
                output.getTag().put("textureData", new CompoundTag());
            }

            recipes.add(GenericRecipe.builder()
                    .withRecipeId(recipe.getId())
                    .withOutput(output)
                    .withInputs(inputs)
                    .withGridSize(3)
                    .build());
        }

        return recipes;
    }
}
