package no.monopixel.slimcolonies.core.generation.defaults.workers;

import no.monopixel.slimcolonies.core.generation.SimpleLootTableProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.NotNull;

import static no.monopixel.slimcolonies.api.util.constant.Constants.MOD_ID;

/**
 * Datagen for generic recipe loot.  (This could be done in the individual crafter gens, but they're potentially
 * useful across multiple, and there's not very many of them.)
 */
public class DefaultRecipeLootProvider extends SimpleLootTableProvider
{
    public static final ResourceLocation LOOT_TABLE_GLASS_BOTTLE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "recipes/glass_bottle");
    public static final ResourceLocation LOOT_TABLE_GRAVEL = ResourceLocation.fromNamespaceAndPath(MOD_ID, "recipes/gravel");

    public DefaultRecipeLootProvider(@NotNull final PackOutput packOutput)
    {
        super(packOutput);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultRecipeLootProvider";
    }

    @Override
    protected void registerTables(@NotNull final LootTableRegistrar registrar)
    {
        registrar.register(LOOT_TABLE_GLASS_BOTTLE, LootContextParamSets.ALL_PARAMS, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(EmptyLootItem.emptyItem().setWeight(100).setQuality(-1))
                        .add(LootItem.lootTableItem(Items.GLASS_BOTTLE).setWeight(0).setQuality(1))));

        registrar.register(LOOT_TABLE_GRAVEL, LootContextParamSets.ALL_PARAMS, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(EmptyLootItem.emptyItem().setWeight(90))
                        .add(LootItem.lootTableItem(Items.FLINT).setWeight(10))));
    }
}
