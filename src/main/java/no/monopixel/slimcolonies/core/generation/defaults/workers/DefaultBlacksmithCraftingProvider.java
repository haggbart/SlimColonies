package no.monopixel.slimcolonies.core.generation.defaults.workers;

import no.monopixel.slimcolonies.api.colony.jobs.ModJobs;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.api.items.ModItems;
import no.monopixel.slimcolonies.api.research.util.ResearchConstants;
import no.monopixel.slimcolonies.core.generation.CustomRecipeProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static no.monopixel.slimcolonies.api.util.constant.BuildingConstants.MODULE_CRAFTING;

/**
 * Datagen for Blacksmith
 */
public class DefaultBlacksmithCraftingProvider extends CustomRecipeProvider
{
    private static final String BLACKSMITH = ModJobs.BLACKSMITH_ID.getPath();

    public DefaultBlacksmithCraftingProvider(@NotNull final PackOutput packOutput)
    {
        super(packOutput);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultBlacksmithCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
    {
        netherite(consumer, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD);
        netherite(consumer, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE);
        netherite(consumer, Items.DIAMOND_AXE, Items.NETHERITE_AXE);
        netherite(consumer, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL);
        netherite(consumer, Items.DIAMOND_HOE, Items.NETHERITE_HOE);
        netherite(consumer, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET);
        netherite(consumer, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE);
        netherite(consumer, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS);
        netherite(consumer, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS);

        CustomRecipeBuilder.create(BLACKSMITH, MODULE_CRAFTING,
                ForgeRegistries.ITEMS.getKey(ModItems.assistantHammer_Iron).getPath())
            .inputs(List.of(new ItemStorage(new ItemStack(Items.IRON_INGOT, 5)),
                new ItemStorage(new ItemStack(Items.STICK))))
            .result(new ItemStack(ModItems.assistantHammer_Iron))
            .minResearchId(ResearchConstants.BUILDERS_ASSISTANT_HAMMER)
            .showTooltip(true)
            .build(consumer);

        CustomRecipeBuilder.create(BLACKSMITH, MODULE_CRAFTING,
                ForgeRegistries.ITEMS.getKey(ModItems.assistantHammer_Diamond).getPath())
            .inputs(List.of(new ItemStorage(new ItemStack(Items.DIAMOND, 5)),
                new ItemStorage(new ItemStack(Items.STICK))))
            .result(new ItemStack(ModItems.assistantHammer_Diamond))
            .minResearchId(ResearchConstants.BUILDERS_ASSISTANT_HAMMER)
            .showTooltip(true)
            .build(consumer);
    }

    private void netherite(@NotNull final Consumer<FinishedRecipe> consumer,
                           @NotNull final ItemLike input,
                           @NotNull final ItemLike output)
    {
        CustomRecipeBuilder.create(BLACKSMITH, MODULE_CRAFTING,
                        ForgeRegistries.ITEMS.getKey(output.asItem()).getPath())
                .inputs(List.of(new ItemStorage(new ItemStack(input)),
                        new ItemStorage(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)),
                        new ItemStorage(new ItemStack(Items.NETHERITE_INGOT)),
                        new ItemStorage(new ItemStack(Items.DIAMOND, 7)),
                        new ItemStorage(new ItemStack(Items.NETHERRACK))))
                .result(new ItemStack(output))
                .secondaryOutputs(Collections.singletonList(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)))
                .minBuildingLevel(4)
                .build(consumer);
    }
}
