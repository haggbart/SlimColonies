package no.monopixel.slimcolonies.core.event;

import com.google.common.collect.ImmutableMap;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.wrapper.InvWrapper;
import no.monopixel.slimcolonies.api.ISlimColoniesAPI;
import no.monopixel.slimcolonies.api.SlimColoniesAPIProxy;
import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.blocks.interfaces.IBuildingBrowsableBlock;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.colony.buildings.modules.IBuildingModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import no.monopixel.slimcolonies.api.research.IGlobalResearch;
import no.monopixel.slimcolonies.api.util.InventoryUtils;
import no.monopixel.slimcolonies.api.util.constant.ColonyConstants;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.api.util.constant.TranslationConstants;
import no.monopixel.slimcolonies.core.client.gui.WindowBuildingBrowser;
import no.monopixel.slimcolonies.core.client.render.worldevent.ColonyBorderRenderer;
import no.monopixel.slimcolonies.core.client.render.worldevent.WorldEventContext;
import no.monopixel.slimcolonies.core.colony.crafting.CustomRecipe;
import no.monopixel.slimcolonies.core.colony.crafting.CustomRecipeManager;
import no.monopixel.slimcolonies.core.util.DomumOrnamentumUtils;
import no.monopixel.slimcolonies.core.util.SchemAnalyzerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

import static no.monopixel.slimcolonies.api.sounds.ModSoundEvents.CITIZEN_SOUND_EVENT_PREFIX;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.*;
import static no.monopixel.slimcolonies.api.util.constant.translation.DebugTranslationConstants.*;

/**
 * Used to handle client events.
 */
@OnlyIn(Dist.CLIENT)
public class ClientEventHandler
{
    /**
     * Lazy cache for crafting module lookups.
     */
    private static final Lazy<Map<String, BuildingEntry>> crafterToBuilding = Lazy.of(ClientEventHandler::buildCrafterToBuildingMap);

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderWorldLastEvent(@NotNull final RenderLevelStageEvent event)
    {
        WorldEventContext.INSTANCE.renderWorldLastEvent(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onwWorldTick(@NotNull final TickEvent.LevelTickEvent event)
    {
        if (event.level.isClientSide && event.phase == TickEvent.Phase.END && ColonyConstants.rand.nextInt(20) == 0)
        {
            WorldEventContext.INSTANCE.checkNearbyColony(event.level);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(@NotNull final ClientPlayerNetworkEvent.LoggingOut event)
    {
        ColonyBorderRenderer.cleanup();
        WindowBuildingBrowser.clearCache();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlaySoundEvent(final PlaySoundEvent event)
    {
        if (event.getSound() == null)
        {
            return;
        }

        final ResourceLocation soundLocation = event.getSound().getLocation();
        if (!SlimColoniesAPIProxy.getInstance().getConfig().getClient().citizenVoices.get()
            && soundLocation.getNamespace().equals(Constants.MOD_ID)
            && soundLocation.getPath().startsWith(CITIZEN_SOUND_EVENT_PREFIX)
        )
        {
            event.setSound(null);
        }
    }

    /**
     * Fires when an item tooltip is requested, generally from inventory, JEI, or when minecraft is first populating the recipe book.
     *
     * @param event An ItemTooltipEvent
     */
    @SubscribeEvent
    public static void onItemTooltipEvent(final ItemTooltipEvent event)
    {
        // Vanilla recipe books populate tooltips once before the player exists on remote clients, some other cases.
        if (event.getEntity() == null)
        {
            return;
        }

        final ItemStack stack = event.getItemStack();

        IColony colony = ISlimColoniesAPI.getInstance().getColonyManager().getIColony(event.getEntity().level, event.getEntity().blockPosition());
        if (colony == null)
        {
            colony = ISlimColoniesAPI.getInstance().getColonyManager().getIColonyByOwner(event.getEntity().level, event.getEntity());
        }
        handleCrafterRecipeTooltips(colony, event.getToolTip(), stack.getItem());
        if (stack.getItem() instanceof BlockItem)
        {
            final BlockItem blockItem = (BlockItem) stack.getItem();
            if (blockItem.getBlock() instanceof AbstractBlockHut)
            {
                handleHutBlockResearchUnlocks(colony, event.getToolTip(), blockItem.getBlock());
            }

            if (event.getEntity().isCreative() && InventoryUtils.hasItemInItemHandler(new InvWrapper(event.getEntity().getInventory()), ModItems.scanTool.get()))
            {
                int tier = SchemAnalyzerUtil.getBlockTier(blockItem.getBlock());

                if (DomumOrnamentumUtils.isDoBlock(blockItem.getBlock()) && stack.hasTag())
                {
                    for (Block block : DomumOrnamentumUtils.getTextureData(stack).getTexturedComponents().values())
                    {
                        tier = Math.max(tier, SchemAnalyzerUtil.getBlockTier(block));
                    }
                }

                event.getToolTip().add(Component.translatable("no.monopixel.slimcolonies.coremod.tooltip.schematic.tier", tier));
            }
        }
    }

    /**
     * Display crafter recipe-related information on the client.
     *
     * @param colony  The colony to check against, if one is present.
     * @param toolTip The tooltip to add the text onto.
     * @param item    The item that will have the tooltip text added.
     */
    private static void handleCrafterRecipeTooltips(@Nullable final IColony colony, final List<Component> toolTip, final Item item)
    {
        final List<CustomRecipe> recipes = CustomRecipeManager.getInstance().getRecipeByOutput(item);
        if (recipes.isEmpty())
        {
            return;
        }

        final Map<BuildingEntry, Integer> minimumBuildingLevels = new HashMap<>();

        for (CustomRecipe rec : recipes)
        {
            if (!rec.getShowTooltip() || rec.getCrafter().length() < 2)
            {
                continue;
            }
            final BuildingEntry craftingBuilding = crafterToBuilding.get().get(rec.getCrafter());
            if (craftingBuilding == null)
            {
                continue;
            }
            minimumBuildingLevels.putIfAbsent(craftingBuilding, null);
            if (minimumBuildingLevels.get(craftingBuilding) == null || rec.getMinBuildingLevel() < minimumBuildingLevels.get(craftingBuilding))
            {
                minimumBuildingLevels.put(craftingBuilding, rec.getMinBuildingLevel());
            }
            for (final ResourceLocation id : rec.getRequiredResearchIds())
            {
                final Set<IGlobalResearch> researches;
                if (ISlimColoniesAPI.getInstance().getGlobalResearchTree().hasResearch(id))
                {
                    researches = new HashSet<>();
                    researches.add(ISlimColoniesAPI.getInstance().getGlobalResearchTree().getResearch(id));
                }
                else
                {
                    researches = ISlimColoniesAPI.getInstance().getGlobalResearchTree().getResearchForEffect(id);
                }
                if (researches != null)
                {
                    final ChatFormatting researchFormat;
                    if (colony != null && (colony.getResearchManager().getResearchTree().hasCompletedResearch(id) ||
                        colony.getResearchManager().getResearchEffects().getEffectStrength(id) > 0))
                    {
                        researchFormat = ChatFormatting.AQUA;
                    }
                    else
                    {
                        researchFormat = ChatFormatting.RED;
                    }

                    for (IGlobalResearch research : researches)
                    {
                        toolTip.add(Component.translatable(COREMOD_ITEM_REQUIRES_RESEARCH_TOOLTIP_GUI,
                            MutableComponent.create(research.getName())).setStyle(Style.EMPTY.withColor(researchFormat)));
                    }
                }
            }
        }

        for (final Entry<BuildingEntry, Integer> crafterBuildingCombination : minimumBuildingLevels.entrySet())
        {
            final Component craftingBuildingName = getFullBuildingName(crafterBuildingCombination.getKey());
            final Integer minimumLevel = crafterBuildingCombination.getValue();
            if (minimumLevel > 0)
            {
                final ResourceLocation schematicName = crafterBuildingCombination.getKey().getRegistryName();
                // the above is not guaranteed to match (and indeed doesn't for a few buildings), but
                // does match for all currently interesting crafters, at least.  there doesn't otherwise
                // appear to be an easy way to get the schematic name from a BuildingEntry ... or
                // unless we can change how colony.hasBuilding uses its parameter...

                final MutableComponent reqLevelText = Component.translatable(COREMOD_ITEM_BUILDLEVEL_TOOLTIP_GUI, craftingBuildingName, minimumLevel);
                if (colony != null && colony.hasBuilding(schematicName, minimumLevel, true))
                {
                    reqLevelText.setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA));
                }
                else
                {
                    reqLevelText.setStyle(Style.EMPTY.withColor(ChatFormatting.RED));
                }
                toolTip.add(reqLevelText);
            }
            else
            {
                final MutableComponent reqBuildingTxt = Component.translatable(COREMOD_ITEM_AVAILABLE_TOOLTIP_GUI, craftingBuildingName)
                    .setStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY));
                toolTip.add(reqBuildingTxt);
            }
        }
    }

    /**
     * Gets a string like "ModName Building Name" for the specified building entry.
     *
     * @param building The building entry
     * @return The translated building name
     */
    private static Component getFullBuildingName(@NotNull final BuildingEntry building)
    {
        final String namespace = building.getBuildingBlock().getRegistryName().getNamespace();
        final String modName = ModList.get().getModContainerById(namespace)
            .map(m -> m.getModInfo().getDisplayName())
            .orElse(namespace);
        final Component buildingName = building.getBuildingBlock().getName();
        return Component.literal(modName + " ").append(buildingName);
    }

    /**
     * Builds a mapping from crafting module ids to the corresponding buildings.
     *
     * @return The mapping
     */
    private static Map<String, BuildingEntry> buildCrafterToBuildingMap()
    {
        final ImmutableMap.Builder<String, BuildingEntry> builder = new ImmutableMap.Builder<>();
        for (final BuildingEntry building : ISlimColoniesAPI.getInstance().getBuildingRegistry())
        {
            for (final BuildingEntry.ModuleProducer moduleProducer : building.getModuleProducers())
            {
                final IBuildingModule module = BuildingEntry.produceModuleWithoutBuilding(moduleProducer.key);
                if (module instanceof ICraftingBuildingModule craftingBuildingModule && craftingBuildingModule.getCraftingJob() != null)
                {
                    builder.put(craftingBuildingModule.getCustomRecipeKey(), building);
                }
            }
        }
        return builder.build();
    }

    /**
     * Display research-related information on Building hut blocks.
     * While this test can handle other non-hut blocks, research can only currently effect AbstractHutBlocks.
     *
     * @param colony  The colony to check against, if one is present.
     * @param tooltip The tooltip to add the text onto.
     * @param block   The hut block
     */
    private static void handleHutBlockResearchUnlocks(final IColony colony, final List<Component> tooltip, final Block block)
    {
        if (colony == null)
        {
            return;
        }
        final ResourceLocation effectId = colony.getResearchManager().getResearchEffectIdFrom(block);
        if (colony.getResearchManager().getResearchEffects().getEffectStrength(effectId) > 0)
        {
            return;
        }
        if (SlimColoniesAPIProxy.getInstance().getGlobalResearchTree().getResearchForEffect(effectId) != null)
        {
            tooltip.add(Component.translatable(TranslationConstants.HUT_NEEDS_RESEARCH_TOOLTIP_1, block.getName()));
            tooltip.add(Component.translatable(TranslationConstants.HUT_NEEDS_RESEARCH_TOOLTIP_2, block.getName()));
        }
    }

    /**
     * Event when the debug screen is opened. Event gets called by displayed text on the screen, we only need it when f3 is clicked.
     */
    @SubscribeEvent
    public static void onDebugOverlay(final CustomizeGuiOverlayEvent.DebugText event)
    {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.options.renderDebug)
        {
            final ClientLevel world = mc.level;
            final LocalPlayer player = mc.player;
            final BlockPos pos = player.blockPosition();
            IColony colony = IColonyManager.getInstance().getIColony(world, pos);
            if (colony == null)
            {
                if (IColonyManager.getInstance().isFarEnoughFromColonies(world, pos))
                {
                    event.getLeft().add(Component.translatable(DEBUG_NO_CLOSE_COLONY).getString());
                    return;
                }
                colony = IColonyManager.getInstance().getClosestIColony(world, pos);

                if (colony == null)
                {
                    return;
                }

                event.getLeft()
                    .add(Component.translatable(DEBUG_NEXT_COLONY,
                        (int) Math.sqrt(colony.getDistanceSquared(pos)),
                        IColonyManager.getInstance().getMinimumDistanceBetweenTownHalls()).getString());
                return;
            }

            event.getLeft().add(colony.getName() + " : " + Component.translatable(DEBUG_BLOCKS_FROM_CENTER, (int) Math.sqrt(colony.getDistanceSquared(pos))).getString());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onUseItem(@NotNull final PlayerInteractEvent.RightClickItem event)
    {
        if (!event.getLevel().isClientSide())
        {
            return;
        }

        if (event.getHand() == InteractionHand.MAIN_HAND && event.getItemStack().getItem() instanceof BlockItem blockItem)
        {
            // due to a Forge bug, this event still triggers on right-clicking a block (and there are no properties on
            // the event itself to distinguish the two cases, even though there are likely-sounding ones), so we need
            // to filter that out
            if (Minecraft.getInstance().hitResult != null && Minecraft.getInstance().hitResult.getType() != HitResult.Type.MISS)
            {
                return;
            }

            final Block block = blockItem.getBlock();

            if (block instanceof IBuildingBrowsableBlock browsable && browsable.shouldBrowseBuildings(event))
            {
                SlimColoniesAPIProxy.getInstance().getBuildingDataManager().openBuildingBrowser(block);

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }
}
