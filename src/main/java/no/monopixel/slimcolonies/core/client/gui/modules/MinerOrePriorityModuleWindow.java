package no.monopixel.slimcolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.ScrollingList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.client.gui.AbstractModuleWindow;
import no.monopixel.slimcolonies.core.colony.buildings.moduleviews.MinerOrePriorityModuleView;
import no.monopixel.slimcolonies.core.network.messages.server.colony.building.AlterMinerOrePriorityMessage;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

import static no.monopixel.slimcolonies.api.util.constant.WindowConstants.*;

public class MinerOrePriorityModuleWindow extends AbstractModuleWindow
{

    private static final String RESOURCE_STRING = ":gui/layouthuts/layoutorepriority.xml";

    private static final String LABEL_LIMIT_REACHED = "no.monopixel.slimcolonies.coremod.gui.warehouse.limitreached";

    private static final TagKey<Block> TAG_ORES_IN_NETHERRACK = TagKey.create(ForgeRegistries.Keys.BLOCKS, new ResourceLocation("forge", "ores_in_ground/netherrack"));
    private static final TagKey<Block> TAG_ORES_IN_END_STONE  = TagKey.create(ForgeRegistries.Keys.BLOCKS, new ResourceLocation("forge", "ores_in_ground/end_stone"));
    private static final TagKey<Block> TAG_ORES_IN_DEEPSLATE  = TagKey.create(ForgeRegistries.Keys.BLOCKS, new ResourceLocation("forge", "ores_in_ground/deepslate"));
    private static final TagKey<Block> TAG_ORES_DIAMOND       = TagKey.create(ForgeRegistries.Keys.BLOCKS, new ResourceLocation("forge", "ores/diamond"));
    private static final TagKey<Block> TAG_ORES_EMERALD       = TagKey.create(ForgeRegistries.Keys.BLOCKS, new ResourceLocation("forge", "ores/emerald"));
    private static final TagKey<Block> TAG_ORES_LAPIS         = TagKey.create(ForgeRegistries.Keys.BLOCKS, new ResourceLocation("forge", "ores/lapis"));
    private static final TagKey<Block> TAG_ORES_REDSTONE      = TagKey.create(ForgeRegistries.Keys.BLOCKS, new ResourceLocation("forge", "ores/redstone"));
    private static final TagKey<Block> TAG_ORES_GOLD          = TagKey.create(ForgeRegistries.Keys.BLOCKS, new ResourceLocation("forge", "ores/gold"));

    private static final Comparator<ItemStorage> BY_DISPLAY_NAME =
        Comparator.comparing(o -> o.getItemStack().getDisplayName().getString().toLowerCase(Locale.US));

    private final   ScrollingList              priorityList;
    private final   MinerOrePriorityModuleView moduleView;
    protected final ScrollingList              resourceList;
    private         String                     filter               = "";
    protected       List<ItemStorage>          groupedItemList;
    protected final List<ItemStorage>          currentDisplayedList = new ArrayList<>();
    private         int                        tick;
    private         List<ItemStorage>          priorityOres;

    public MinerOrePriorityModuleWindow(final IBuildingView building, final MinerOrePriorityModuleView moduleView)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);

        priorityList = this.window.findPaneOfTypeByID("resourcesstock", ScrollingList.class);
        this.moduleView = moduleView;

        registerButton(BUTTON_SWITCH, this::switchClicked);
        registerButton(STOCK_REMOVE, this::removeOre);

        resourceList = window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);

        groupedItemList = new ArrayList<>();
        for (ItemStorage storage : IColonyManager.getInstance().getCompatibilityManager().getSmeltableOres())
        {
            // Only include ore blocks, not raw materials
            if (storage.getItemStack().getItem() instanceof net.minecraft.world.item.BlockItem blockItem)
            {
                final Block block = blockItem.getBlock();
                final ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
                final int buildingLevel = building.getBuildingLevel();

                if (block.defaultBlockState().is(TAG_ORES_IN_NETHERRACK) ||
                    block.defaultBlockState().is(TAG_ORES_IN_END_STONE))
                {
                    continue;
                }

                if (blockId != null && blockId.getPath().contains("ancient_debris"))
                {
                    continue;
                }

                if (block.defaultBlockState().is(TAG_ORES_IN_DEEPSLATE) && buildingLevel < 3)
                {
                    continue;
                }

                if (block.defaultBlockState().is(TAG_ORES_GOLD) && buildingLevel < 2)
                {
                    continue;
                }
                if (buildingLevel < 3 && (
                    block.defaultBlockState().is(TAG_ORES_LAPIS) ||
                        block.defaultBlockState().is(TAG_ORES_REDSTONE)))
                {
                    continue;
                }
                if (block.defaultBlockState().is(TAG_ORES_DIAMOND) && buildingLevel < 4)
                {
                    continue;
                }
                if (block.defaultBlockState().is(TAG_ORES_EMERALD) && buildingLevel < 5)
                {
                    continue;
                }

                groupedItemList.add(storage);
            }
        }

        window.findPaneOfTypeByID(INPUT_FILTER, TextField.class).setHandler(input -> {
            final String newFilter = input.getText();
            if (!newFilter.equals(filter))
            {
                filter = newFilter;
                this.tick = 10;
            }
        });
    }

    private void removeOre(final Button button)
    {
        final int row = priorityList.getListElementIndexByPane(button);
        final ItemStorage storage = priorityOres.get(row);
        moduleView.getPriorityOres().remove(storage);
        Network.getNetwork().sendToServer(AlterMinerOrePriorityMessage.removeOre(buildingView, storage.getItemStack(), moduleView.getProducer().getRuntimeID()));
        updatePriorityList();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        updatePriorityList();
        updateResources();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (tick > 0 && --tick == 0)
        {
            updateResources();
        }
    }

    private void switchClicked(@NotNull final Button button)
    {
        if (!moduleView.hasReachedLimit())
        {
            final int row = resourceList.getListElementIndexByPane(button);
            final ItemStorage storage = currentDisplayedList.get(row);

            Network.getNetwork().sendToServer(AlterMinerOrePriorityMessage.addOre(buildingView, storage.getItemStack(), moduleView.getProducer().getRuntimeID()));
            moduleView.getPriorityOres().add(storage);
            updatePriorityList();

            resourceList.refreshElementPanes();
        }
    }

    private void updatePriorityList()
    {
        priorityOres = new ArrayList<>(moduleView.getPriorityOres());
        priorityOres.sort(BY_DISPLAY_NAME);

        if (priorityOres.isEmpty())
        {
            Objects.requireNonNull(findPaneByID("warning")).show();
        }
        else
        {
            Objects.requireNonNull(findPaneByID("warning")).hide();
        }

        priorityList.enable();
        priorityList.show();

        priorityList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return priorityOres.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ItemStack resource = priorityOres.get(index).getItemStack().copy();

                rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class).setText(resource.getHoverName());
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resource);

                final Gradient gradient = rowPane.findPaneOfTypeByID("gradient", Gradient.class);
                gradient.setGradientStart(0, 0, 0, 0);
                gradient.setGradientEnd(0, 0, 0, 0);
            }
        });
    }

    private void updateResources()
    {
        final Predicate<ItemStack> filterPredicate = stack -> filter.isEmpty()
            || stack.getDescriptionId().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
            || stack.getHoverName().getString().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US));
        currentDisplayedList.clear();
        for (final ItemStorage storage : groupedItemList)
        {
            if (filterPredicate.test(storage.getItemStack()))
            {
                currentDisplayedList.add(storage);
            }
        }

        currentDisplayedList.sort(BY_DISPLAY_NAME);

        updateResourceList();
    }

    protected void updateResourceList()
    {
        resourceList.enable();
        resourceList.show();

        resourceList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return currentDisplayedList.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ItemStack resource = currentDisplayedList.get(index).getItemStack();
                final Text resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class);
                resourceLabel.setText(resource.getItem().getName(resource).plainCopy());
                final ItemIcon itemIcon = rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class);
                itemIcon.setItem(resource);
                final boolean isInPriority = moduleView.getPriorityOres().contains(new ItemStorage(resource));
                final Button switchButton = rowPane.findPaneOfTypeByID(BUTTON_SWITCH, Button.class);
                final Gradient gradient = rowPane.findPaneOfTypeByID("gradient", Gradient.class);
                gradient.setGradientStart(0, 0, 0, 0);
                gradient.setGradientEnd(0, 0, 0, 0);

                if (moduleView.hasReachedLimit())
                {
                    switchButton.disable();
                    PaneBuilders.tooltipBuilder()
                        .append(Component.translatable(LABEL_LIMIT_REACHED))
                        .hoverPane(switchButton)
                        .build();
                }
                if (isInPriority)
                {
                    switchButton.disable();
                }
                else
                {
                    switchButton.enable();
                }
            }
        });
    }
}
