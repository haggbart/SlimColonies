package no.monopixel.slimcolonies.apiimp.initializer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import no.monopixel.slimcolonies.api.blocks.ModBlocks;
import no.monopixel.slimcolonies.api.items.ModItems;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.items.*;

import static no.monopixel.slimcolonies.api.blocks.decorative.AbstractBlockGate.IRON_GATE;
import static no.monopixel.slimcolonies.api.blocks.decorative.AbstractBlockGate.WOODEN_GATE;

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
        ModItems.assistantAxe = new ItemAssistantHammer("assistantaxe", new Item.Properties(), 2);
        ModItems.supplyCamp = new ItemSupplyCampDeployer(new Item.Properties());
        ModItems.ancientTome = new ItemAncientTome(new Item.Properties());
        ModItems.clipboard = new ItemClipboard(new Item.Properties());
        ModItems.compost = new ItemCompost(new Item.Properties());
        ModItems.resourceScroll = new ItemResourceScroll(new Item.Properties());
        ModItems.scepterBeekeeper = new ItemScepterBeekeeper(new Item.Properties());
        ModItems.questLog = new ItemQuestLog(new Item.Properties());

        ModItems.adventureToken = new ItemAdventureToken(new Item.Properties());

        ModItems.irongate = new ItemGate(IRON_GATE, ModBlocks.blockIronGate, new Item.Properties());
        ModItems.woodgate = new ItemGate(WOODEN_GATE, ModBlocks.blockWoodenGate, new Item.Properties());

        ModItems.flagBanner = new ItemColonyFlagBanner("colony_banner", new Item.Properties());

        ModItems.sifterMeshString = new ItemSifterMesh("sifter_mesh_string", new Item.Properties().durability(500).setNoRepair());
        ModItems.sifterMeshFlint = new ItemSifterMesh("sifter_mesh_flint", new Item.Properties().durability(1000).setNoRepair());
        ModItems.sifterMeshIron = new ItemSifterMesh("sifter_mesh_iron", new Item.Properties().durability(1500).setNoRepair());
        ModItems.sifterMeshDiamond = new ItemSifterMesh("sifter_mesh_diamond", new Item.Properties().durability(2000).setNoRepair());

        ModItems.buildGoggles = new ItemBuildGoggles("build_goggles", new Item.Properties());
        ModItems.scanAnalyzer = new ItemScanAnalyzer("scan_analyzer", new Item.Properties());


        // Baker will now produce final food products directly - intermediate dough items removed

        // Large bottle system removed for simplicity

        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "supplychestdeployer"), ModItems.supplyChest);
        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "scan_analyzer"), ModItems.scanAnalyzer);
        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "scepterpermission"), ModItems.permTool);
        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "scepterguard"), ModItems.scepterGuard);
        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "assistantaxe"), ModItems.assistantAxe);
        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "supplycampdeployer"), ModItems.supplyCamp);
        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "ancienttome"), ModItems.ancientTome);
        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "clipboard"), ModItems.clipboard);
        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "compost"), ModItems.compost);
        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "resourcescroll"), ModItems.resourceScroll);
        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "scepterlumberjack"), ModItems.scepterLumberjack);
        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "scepterbeekeeper"), ModItems.scepterBeekeeper);
        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "questlog"), ModItems.questLog);

        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "adventure_token"), ModItems.adventureToken);

        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, IRON_GATE), ModItems.irongate);
        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, WOODEN_GATE), ModItems.woodgate);
        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "colony_banner"), ModItems.flagBanner);


        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "sifter_mesh_string"), ModItems.sifterMeshString);
        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "sifter_mesh_flint"), ModItems.sifterMeshFlint);
        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "sifter_mesh_iron"), ModItems.sifterMeshIron);
        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "sifter_mesh_diamond"), ModItems.sifterMeshDiamond);

        registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "build_goggles"), ModItems.buildGoggles);
    }

    private static void registerCompostItems()
    {
        // No ingredient items to register for composting since most were removed
    }
}
