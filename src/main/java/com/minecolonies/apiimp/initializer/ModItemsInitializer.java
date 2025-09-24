package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.items.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

import static com.minecolonies.api.blocks.decorative.AbstractBlockGate.IRON_GATE;
import static com.minecolonies.api.blocks.decorative.AbstractBlockGate.WOODEN_GATE;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModItemsInitializer
{

    private ModItemsInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModItemsInitializer but this is a Utility class.");
    }

    @SubscribeEvent
    public static void registerItems(RegisterEvent event)
    {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.ITEMS))
        {
            ModItemsInitializer.init(event.getForgeRegistry());

            registerCompostItems();
        }
    }

    /**
     * Initates all the blocks. At the correct time.
     *
     * @param registry the registry.
     */
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static void init(final IForgeRegistry<Item> registry)
    {
        ModItems.scepterLumberjack = new ItemScepterLumberjack(new Item.Properties());
        ModItems.supplyChest = new ItemSupplyChestDeployer(new Item.Properties());
        ModItems.permTool = new ItemScepterPermission(new Item.Properties());
        ModItems.scepterGuard = new ItemScepterGuard(new Item.Properties());
        ModItems.assistantHammer_Gold = new ItemAssistantHammer("assistanthammer_gold", new Item.Properties().durability(200), 1);
        ModItems.assistantHammer_Iron = new ItemAssistantHammer("assistanthammer_iron", new Item.Properties().durability(400), 2);
        ModItems.assistantHammer_Diamond = new ItemAssistantHammer("assistanthammer_diamond", new Item.Properties().durability(1000), 3);
        ModItems.bannerRallyGuards = new ItemBannerRallyGuards(new Item.Properties());
        ModItems.supplyCamp = new ItemSupplyCampDeployer(new Item.Properties());
        ModItems.ancientTome = new ItemAncientTome(new Item.Properties());
        ModItems.clipboard = new ItemClipboard(new Item.Properties());
        ModItems.compost = new ItemCompost(new Item.Properties());
        ModItems.resourceScroll = new ItemResourceScroll(new Item.Properties());
        ModItems.scepterBeekeeper = new ItemScepterBeekeeper(new Item.Properties());
        ModItems.questLog = new ItemQuestLog(new Item.Properties());

        ModItems.breadDough = new ItemBreadDough(new Item.Properties());
        ModItems.cookieDough = new ItemCookieDough(new Item.Properties());
        ModItems.cakeBatter = new ItemCakeBatter(new Item.Properties());
        ModItems.rawPumpkinPie = new ItemRawPumpkinPie(new Item.Properties());

        ModItems.adventureToken = new ItemAdventureToken(new Item.Properties());

        ModItems.scrollColonyTP = new ItemScrollColonyTP(new Item.Properties().stacksTo(16));
        registry.register(new ResourceLocation(Constants.MOD_ID, "scroll_tp"), ModItems.scrollColonyTP);

        ModItems.scrollColonyAreaTP = new ItemScrollColonyAreaTP(new Item.Properties().stacksTo(16));
        registry.register(new ResourceLocation(Constants.MOD_ID, "scroll_area_tp"), ModItems.scrollColonyAreaTP);

        ModItems.scrollBuff = new ItemScrollBuff(new Item.Properties().stacksTo(16));
        registry.register(new ResourceLocation(Constants.MOD_ID, "scroll_buff"), ModItems.scrollBuff);

        ModItems.scrollHighLight = new ItemScrollHighlight(new Item.Properties().stacksTo(16));
        registry.register(new ResourceLocation(Constants.MOD_ID, "scroll_highlight"), ModItems.scrollHighLight);

        ModItems.irongate = new ItemGate(IRON_GATE, ModBlocks.blockIronGate, new Item.Properties());
        ModItems.woodgate = new ItemGate(WOODEN_GATE, ModBlocks.blockWoodenGate, new Item.Properties());

        ModItems.flagBanner = new ItemColonyFlagBanner("colony_banner", new Item.Properties());

        ModItems.sifterMeshString = new ItemSifterMesh("sifter_mesh_string", new Item.Properties().durability(500).setNoRepair());
        ModItems.sifterMeshFlint = new ItemSifterMesh("sifter_mesh_flint", new Item.Properties().durability(1000).setNoRepair());
        ModItems.sifterMeshIron = new ItemSifterMesh("sifter_mesh_iron", new Item.Properties().durability(1500).setNoRepair());
        ModItems.sifterMeshDiamond = new ItemSifterMesh("sifter_mesh_diamond", new Item.Properties().durability(2000).setNoRepair());

        ModItems.buildGoggles = new ItemBuildGoggles("build_goggles", new Item.Properties());
        ModItems.scanAnalyzer = new ItemScanAnalyzer("scan_analyzer", new Item.Properties());
        ModItems.colonyMap = new ItemColonyMap(new Item.Properties());


        // Only keeping basic dough items - all other food items removed

        // Large bottle system removed for simplicity

        registry.register(new ResourceLocation(Constants.MOD_ID, "supplychestdeployer"), ModItems.supplyChest);
        registry.register(new ResourceLocation(Constants.MOD_ID, "scan_analyzer"), ModItems.scanAnalyzer);
        registry.register(new ResourceLocation(Constants.MOD_ID, "scepterpermission"), ModItems.permTool);
        registry.register(new ResourceLocation(Constants.MOD_ID, "scepterguard"), ModItems.scepterGuard);
        registry.register(new ResourceLocation(Constants.MOD_ID, "assistanthammer_gold"), ModItems.assistantHammer_Gold);
        registry.register(new ResourceLocation(Constants.MOD_ID, "assistanthammer_iron"), ModItems.assistantHammer_Iron);
        registry.register(new ResourceLocation(Constants.MOD_ID, "assistanthammer_diamond"), ModItems.assistantHammer_Diamond);
        registry.register(new ResourceLocation(Constants.MOD_ID, "banner_rally_guards"), ModItems.bannerRallyGuards);
        registry.register(new ResourceLocation(Constants.MOD_ID, "supplycampdeployer"), ModItems.supplyCamp);
        registry.register(new ResourceLocation(Constants.MOD_ID, "ancienttome"), ModItems.ancientTome);
        registry.register(new ResourceLocation(Constants.MOD_ID, "clipboard"), ModItems.clipboard);
        registry.register(new ResourceLocation(Constants.MOD_ID, "compost"), ModItems.compost);
        registry.register(new ResourceLocation(Constants.MOD_ID, "resourcescroll"), ModItems.resourceScroll);
        registry.register(new ResourceLocation(Constants.MOD_ID, "scepterlumberjack"), ModItems.scepterLumberjack);
        registry.register(new ResourceLocation(Constants.MOD_ID, "scepterbeekeeper"), ModItems.scepterBeekeeper);
        registry.register(new ResourceLocation(Constants.MOD_ID, "questlog"), ModItems.questLog);
        registry.register(new ResourceLocation(Constants.MOD_ID, "colonymap"), ModItems.colonyMap);

        registry.register(new ResourceLocation(Constants.MOD_ID, "bread_dough"), ModItems.breadDough);
        registry.register(new ResourceLocation(Constants.MOD_ID, "cookie_dough"), ModItems.cookieDough);
        registry.register(new ResourceLocation(Constants.MOD_ID, "cake_batter"), ModItems.cakeBatter);
        registry.register(new ResourceLocation(Constants.MOD_ID, "raw_pumpkin_pie"), ModItems.rawPumpkinPie);

        registry.register(new ResourceLocation(Constants.MOD_ID, "adventure_token"), ModItems.adventureToken);

        registry.register(new ResourceLocation(Constants.MOD_ID, IRON_GATE), ModItems.irongate);
        registry.register(new ResourceLocation(Constants.MOD_ID, WOODEN_GATE), ModItems.woodgate);
        registry.register(new ResourceLocation(Constants.MOD_ID, "colony_banner"), ModItems.flagBanner);


        registry.register(new ResourceLocation(Constants.MOD_ID, "sifter_mesh_string"), ModItems.sifterMeshString);
        registry.register(new ResourceLocation(Constants.MOD_ID, "sifter_mesh_flint"), ModItems.sifterMeshFlint);
        registry.register(new ResourceLocation(Constants.MOD_ID, "sifter_mesh_iron"), ModItems.sifterMeshIron);
        registry.register(new ResourceLocation(Constants.MOD_ID, "sifter_mesh_diamond"), ModItems.sifterMeshDiamond);

        registry.register(new ResourceLocation(Constants.MOD_ID, "build_goggles"), ModItems.buildGoggles);
    }

    private static void registerCompostItems()
    {
        // No ingredient items to register for composting since most were removed
    }
}
