package no.monopixel.slimcolonies.core.generation.defaults.workers;

import no.monopixel.slimcolonies.api.colony.jobs.ModJobs;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.api.items.ModItems;
import no.monopixel.slimcolonies.core.generation.CustomRecipeProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static no.monopixel.slimcolonies.api.util.constant.BuildingConstants.MODULE_CRAFTING;
import static no.monopixel.slimcolonies.api.util.constant.BuildingConstants.MODULE_SMELTING;

/**
 * Datagen for Baker
 */
public class DefaultBakerCraftingProvider extends CustomRecipeProvider
{
    private static final String BAKER = ModJobs.BAKER_ID.getPath();

    public DefaultBakerCraftingProvider(@NotNull final PackOutput packOutput)
    {
        super(packOutput);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultBakerCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
    {
        final ItemStack waterBottle = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "water_bottle")
            .inputs(List.of(new ItemStorage(new ItemStack(Items.GLASS_BOTTLE))))
            .result(waterBottle)
            .minBuildingLevel(3)
            .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_SMELTING, "bread")
            .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT), 3)))
            .result(new ItemStack(Items.BREAD))
            .intermediate(Blocks.FURNACE)
            .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "cookie")
            .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 2)),
                new ItemStorage(new ItemStack(Items.COCOA_BEANS, 2))))
            .result(new ItemStack(Items.COOKIE, 8))
            .minBuildingLevel(2)
            .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "pumpkin_pie")
            .inputs(List.of(new ItemStorage(new ItemStack(Items.PUMPKIN)),
                new ItemStorage(new ItemStack(Items.SUGAR)),
                new ItemStorage(new ItemStack(Items.EGG))))
            .result(new ItemStack(Items.PUMPKIN_PIE))
            .minBuildingLevel(3)
            .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "cake")
            .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 3)),
                new ItemStorage(new ItemStack(Items.MILK_BUCKET, 3)),
                new ItemStorage(new ItemStack(Items.SUGAR, 2)),
                new ItemStorage(new ItemStack(Items.EGG))))
            .result(new ItemStack(Items.CAKE))
            .minBuildingLevel(4)
            .build(consumer);

        // Intermediate dough items removed - baker now produces final food products directly
    }
}
