package no.monopixel.slimcolonies.core;

import com.ldtteam.structurize.storage.SurvivalBlueprintHandlers;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.TagManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import no.monopixel.slimcolonies.api.SlimColoniesAPIProxy;
import no.monopixel.slimcolonies.api.advancements.AdvancementTriggers;
import no.monopixel.slimcolonies.api.colony.IChunkmanagerCapability;
import no.monopixel.slimcolonies.api.colony.IColonyTagCapability;
import no.monopixel.slimcolonies.api.configuration.Configuration;
import no.monopixel.slimcolonies.api.crafting.CountedIngredient;
import no.monopixel.slimcolonies.api.creativetab.ModCreativeTabs;
import no.monopixel.slimcolonies.api.enchants.ModEnchants;
import no.monopixel.slimcolonies.api.entity.ModEntities;
import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.api.equipment.ModEquipmentTypes;
import no.monopixel.slimcolonies.api.items.ModTags;
import no.monopixel.slimcolonies.api.loot.ModLootConditions;
import no.monopixel.slimcolonies.api.sounds.ModSoundEvents;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.api.util.constant.SchematicTagConstants;
import no.monopixel.slimcolonies.apiimp.ClientSlimColoniesAPIImpl;
import no.monopixel.slimcolonies.apiimp.CommonSlimColoniesAPIImpl;
import no.monopixel.slimcolonies.apiimp.initializer.*;
import no.monopixel.slimcolonies.core.blocks.BlockPlantationField;
import no.monopixel.slimcolonies.core.blocks.huts.BlockHutGateHouse;
import no.monopixel.slimcolonies.core.colony.IColonyManagerCapability;
import no.monopixel.slimcolonies.core.colony.requestsystem.init.RequestSystemInitializer;
import no.monopixel.slimcolonies.core.colony.requestsystem.init.StandardFactoryControllerInitializer;
import no.monopixel.slimcolonies.core.event.*;
import no.monopixel.slimcolonies.core.loot.SupplyLoot;
import no.monopixel.slimcolonies.core.placementhandlers.PlacementHandlerInitializer;
import no.monopixel.slimcolonies.core.placementhandlers.main.SuppliesHandler;
import no.monopixel.slimcolonies.core.placementhandlers.main.SurvivalHandler;
import no.monopixel.slimcolonies.core.recipes.FoodIngredient;
import no.monopixel.slimcolonies.core.recipes.PlantIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static no.monopixel.slimcolonies.api.util.constant.SchematicTagConstants.*;

@Mod(Constants.MOD_ID)
public class SlimColonies
{
    public static final Capability<IChunkmanagerCapability> CHUNK_STORAGE_UPDATE_CAP = CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<IColonyManagerCapability> COLONY_MANAGER_CAP = CapabilityManager.get(new CapabilityToken<>() {});

    /**
     * The config instance.
     */
    private static Configuration config;

    public SlimColonies()
    {
        ModEquipmentTypes.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        TileEntityInitializer.BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModEnchants.ENCHANTMENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModContainerInitializers.CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModBuildingsInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModBuildingExtensionsInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModGuardTypesInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModColonyEventDescriptionTypeInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModResearchRequirementInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModRecipeSerializerInitializer.RECIPE_SERIALIZER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModRecipeSerializerInitializer.RECIPE_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModCraftingTypesInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModJobsInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModRecipeTypesInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModSoundEvents.SOUND_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModInteractionsInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModResearchEffectInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModLootConditions.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        SupplyLoot.GLM.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModQuestInitializer.DEFERRED_REGISTER_OBJECTIVE.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModQuestInitializer.DEFERRED_REGISTER_TRIGGER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModQuestInitializer.DEFERRED_REGISTER_REWARD.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModQuestInitializer.DEFERRED_REGISTER_ANSWER_RESULT.register(FMLJavaModLoadingContext.get().getModEventBus());

        ModCreativeTabs.TAB_REG.register(FMLJavaModLoadingContext.get().getModEventBus());

        ModEnchantInitializer.init();

        LanguageHandler.loadLangPath("assets/slimcolonies/lang/%s.json"); // hotfix config comments, it's ugly bcs it's gonna be replaced
        config = new Configuration();

        Consumer<TagsUpdatedEvent> onTagsLoaded = (event) -> ModTags.tagsLoaded = true;
        MinecraftForge.EVENT_BUS.addListener(onTagsLoaded);

        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(EventHandler.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(FMLEventHandler.class);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(ClientEventHandler.class));
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(DataPackSyncEventHandler.ServerEvents.class);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(DataPackSyncEventHandler.ClientEvents.class));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            TagManager.registerGlobalTagOption(TAG_WORK);
            TagManager.registerGlobalTagOption(TAG_SIT_IN);
            TagManager.registerGlobalTagOption(TAG_SIT_OUT);
            TagManager.registerGlobalTagOption(TAG_STAND_IN);
            TagManager.registerGlobalTagOption(TAG_STAND_OUT);
            TagManager.registerGlobalTagOption(BUILDING_SIGN);

            TagManager.registerSpecificTagOption(TAG_GATE, b -> b instanceof BlockHutGateHouse);
            TagManager.registerSpecificTagOption(TAG_KNIGHT, b -> b instanceof BlockHutGateHouse);
            TagManager.registerSpecificTagOption(TAG_ARCHER, b -> b instanceof BlockHutGateHouse);

            for (final String fieldTag : SchematicTagConstants.getPlantationTags())
            {
                TagManager.registerSpecificTagOption(fieldTag, b -> b instanceof BlockPlantationField);
            }
        });

        Mod.EventBusSubscriber.Bus.MOD.bus().get().addListener(GatherDataHandler::dataGeneratorSetup);

        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(this.getClass());
        Mod.EventBusSubscriber.Bus.MOD.bus().get().register(this.getClass());
        Mod.EventBusSubscriber.Bus.MOD.bus().get().register(ClientRegistryHandler.class);
        Mod.EventBusSubscriber.Bus.MOD.bus().get().register(ModCreativeTabs.class);

        InteractionValidatorInitializer.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SlimColoniesAPIProxy.getInstance().setApiInstance(new ClientSlimColoniesAPIImpl()));
        DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> SlimColoniesAPIProxy.getInstance().setApiInstance(new CommonSlimColoniesAPIImpl()));

        SurvivalBlueprintHandlers.registerHandler(new SurvivalHandler());
        SurvivalBlueprintHandlers.registerHandler(new SuppliesHandler());
    }

    @SubscribeEvent
    public static void registerNewRegistries(final NewRegistryEvent event)
    {
        SlimColoniesAPIProxy.getInstance().onRegistryNewRegistry(event);
    }

    /**
     * Event handler for forge pre init event.
     *
     * @param event the forge pre init event.
     */
    @SubscribeEvent
    public static void preInit(@NotNull final FMLCommonSetupEvent event)
    {
        Network.getNetwork().registerCommonMessages();

        AdvancementTriggers.preInit();

        StandardFactoryControllerInitializer.onPreInit();

        event.enqueueWork(ModLootConditions::init);
        event.enqueueWork(ModTags::init);
    }

    @SubscribeEvent
    public static void registerCaps(final RegisterCapabilitiesEvent event)
    {
        event.register(IColonyTagCapability.class);
        event.register(IChunkmanagerCapability.class);
        event.register(IColonyManagerCapability.class);
    }

    @SubscribeEvent
    public static void createEntityAttribute(final EntityAttributeCreationEvent event)
    {
        event.put(ModEntities.CITIZEN, AbstractEntityCitizen.getDefaultAttributes().build());
        event.put(ModEntities.VISITOR, AbstractEntityCitizen.getDefaultAttributes().build());
    }

    @SubscribeEvent
    public static void registerRecipeSerializers(final RegisterEvent event)
    {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS))
        {
            CraftingHelper.register(CountedIngredient.ID, CountedIngredient.Serializer.getInstance());
            CraftingHelper.register(FoodIngredient.ID, FoodIngredient.Serializer.getInstance());
            CraftingHelper.register(PlantIngredient.ID, PlantIngredient.Serializer.getInstance());
        }
    }

    /**
     * Called when MC loading is about to finish.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onLoadComplete(final FMLLoadCompleteEvent event)
    {
        PlacementHandlerInitializer.initHandlers();
        RequestSystemInitializer.onPostInit();
    }

    /**
     * Get the config handler.
     *
     * @return the config handler.
     */
    public static Configuration getConfig()
    {
        return config;
    }
}
