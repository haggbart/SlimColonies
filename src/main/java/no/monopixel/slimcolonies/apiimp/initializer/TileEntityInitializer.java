package no.monopixel.slimcolonies.apiimp.initializer;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import no.monopixel.slimcolonies.api.blocks.ModBlocks;
import no.monopixel.slimcolonies.api.tileentities.SlimColoniesTileEntities;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.tileentities.*;

public class TileEntityInitializer
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MOD_ID);
    static
    {
        SlimColoniesTileEntities.SCARECROW = BLOCK_ENTITIES.register("scarecrow", () -> BlockEntityType.Builder.of(TileEntityScarecrow::new, ModBlocks.blockScarecrow).build(null));

        SlimColoniesTileEntities.PLANTATION_FIELD =
            BLOCK_ENTITIES.register("plantationfield", () -> BlockEntityType.Builder.of(TileEntityPlantationField::new, ModBlocks.blockPlantationField).build(null));

        SlimColoniesTileEntities.BARREL = BLOCK_ENTITIES.register("barrel", () -> BlockEntityType.Builder.of(TileEntityBarrel::new, ModBlocks.blockBarrel).build(null));

        SlimColoniesTileEntities.BUILDING =
            BLOCK_ENTITIES.register("colonybuilding", () -> BlockEntityType.Builder.of(TileEntityColonyBuilding::new, ModBlocks.getHuts()).build(null));

        SlimColoniesTileEntities.DECO_CONTROLLER = BLOCK_ENTITIES.register("decorationcontroller", () -> BlockEntityType.Builder
            .of(TileEntityDecorationController::new, ModBlocks.blockDecorationPlaceholder)
            .build(null));

        SlimColoniesTileEntities.RACK = BLOCK_ENTITIES.register("rack", () -> BlockEntityType.Builder.of(TileEntityRack::new, ModBlocks.blockRack).build(null));

        SlimColoniesTileEntities.GRAVE = BLOCK_ENTITIES.register("grave", () -> BlockEntityType.Builder.of(TileEntityGrave::new, ModBlocks.blockGrave).build(null));

        SlimColoniesTileEntities.NAMED_GRAVE =
            BLOCK_ENTITIES.register("namedgrave", () -> BlockEntityType.Builder.of(TileEntityNamedGrave::new, ModBlocks.blockNamedGrave).build(null));

        SlimColoniesTileEntities.WAREHOUSE =
            BLOCK_ENTITIES.register("warehouse", () -> BlockEntityType.Builder.of(TileEntityWareHouse::new, ModBlocks.blockHutWareHouse).build(null));

        SlimColoniesTileEntities.COMPOSTED_DIRT =
            BLOCK_ENTITIES.register("composteddirt", () -> BlockEntityType.Builder.of(TileEntityCompostedDirt::new, ModBlocks.blockCompostedDirt)
                .build(null));

        SlimColoniesTileEntities.ENCHANTER =
            BLOCK_ENTITIES.register("enchanter", () -> BlockEntityType.Builder.of(TileEntityEnchanter::new, ModBlocks.blockHutEnchanter).build(null));

        SlimColoniesTileEntities.STASH = BLOCK_ENTITIES.register("stash", () -> BlockEntityType.Builder.of(TileEntityStash::new, ModBlocks.blockStash).build(null));

        SlimColoniesTileEntities.COLONY_FLAG = BLOCK_ENTITIES.register("colony_flag",
            () -> BlockEntityType.Builder.of(TileEntityColonyFlag::new, ModBlocks.blockColonyBanner, ModBlocks.blockColonyWallBanner).build(null));
    }
}
