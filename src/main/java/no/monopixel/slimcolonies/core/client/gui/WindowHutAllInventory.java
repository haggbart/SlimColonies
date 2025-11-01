package no.monopixel.slimcolonies.core.client.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.ScrollingList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.api.util.MessageUtils;
import no.monopixel.slimcolonies.api.util.Utils;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.client.render.worldevent.HighlightManager;
import no.monopixel.slimcolonies.core.client.render.worldevent.highlightmanager.TimedBoxRenderData;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.tileentities.TileEntityRack;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.MESSAGE_LOCATING_ITEMS;
import static no.monopixel.slimcolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for a hut name entry.
 */
public class WindowHutAllInventory extends AbstractWindowSkeleton
{

    /**
     * List of all item stacks in the warehouse.
     */
    List<ItemStorage> allItems = new ArrayList<>();

    /**
     * Resource scrolling list.
     */
    private final ScrollingList stackList;

    /**
     * The filter for the resource list.
     */
    private String filter = "";

    /**
     * The sortDescriptor so how we want to sort
     */
    private int sortDescriptor = COUNT_DESC_SORT;

    /**
     * The building associated to the GUI.
     */
    private final IBuildingView building;

    /**
     * The previous window.
     */
    private final BOWindow prev;

    /**
     * Update delay.
     */
    private int tick;

    /**
     * Constructor for a hut rename entry window.
     *
     * @param b    {@link AbstractBuilding}
     * @param prev the previous window.
     */
    public WindowHutAllInventory(final IBuildingView b, final BOWindow prev)
    {
        super(Constants.MOD_ID + HUT_ALL_INVENTORY_SUFFIX);
        this.building = b;
        registerButton(BUTTON_SORT, this::setSortFlag);
        registerButton(BUTTON_BACK, this::back);
        this.stackList = findPaneOfTypeByID(LIST_ALLINVENTORY, ScrollingList.class);
        updateResources();
        registerButton(LOCATE, this::locate);
        this.prev = prev;

        window.findPaneOfTypeByID("names", TextField.class).setHandler(input -> {
            final String newFilter = input.getText();
            if (!newFilter.equals(filter))
            {
                filter = newFilter;
                this.tick = 10;
            }
        });
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

    private void locate(final Button button)
    {
        final int row = stackList.getListElementIndexByPane(button);
        final ItemStorage storage = allItems.get(row);
        final Set<BlockPos> containerList = new HashSet<>(building.getContainerList());
        containerList.add(building.getID());
        HighlightManager.clearHighlightsForKey("inventoryHighlight");

        MessageUtils.format(MESSAGE_LOCATING_ITEMS).sendTo(Minecraft.getInstance().player);
        close();

        for (BlockPos blockPos : containerList)
        {
            final BlockEntity rack = Minecraft.getInstance().level.getBlockEntity(blockPos);
            if (rack instanceof TileEntityRack)
            {
                int count = ((TileEntityRack) rack).getCount(storage.getItemStack(), storage.ignoreDamageValue(), false);
                if (count > 0)
                {
                    // Varies the color between red(1 pc) over yellow(32 pcs) to green(64+ pcs)
                    // mixing equation: alpha | red part | green part
                    final int color = 0x80000000 | (Mth.clamp((int) (0xff * (2.0f - count / 32.0f)), 0, 255) << 16)
                        | (Mth.clamp((int) (0xff * count / 32.0f), 0, 255) << 8);
                    HighlightManager.addHighlight("inventoryHighlight", blockPos.toString(),
                        new TimedBoxRenderData(blockPos)
                            .setDuration(Duration.ofSeconds(60))
                            .addText("" + count)
                            .setColor(color));
                }
            }
        }
    }

    /**
     * On prev clicked.
     */
    private void back()
    {
        this.prev.open();
    }

    /**
     * Toggles between COUNT_DESC_SORT (most abundant first) and ASC_SORT (alphabetical A-Z)
     **/
    private void setSortFlag()
    {
        if (sortDescriptor == COUNT_DESC_SORT)
        {
            sortDescriptor = ASC_SORT;
            findPaneOfTypeByID(BUTTON_SORT, ButtonImage.class).setText(Component.literal("A^"));
        }
        else
        {
            sortDescriptor = COUNT_DESC_SORT;
            findPaneOfTypeByID(BUTTON_SORT, ButtonImage.class).setText(Component.literal("1v"));
        }

        updateResources();
    }

    /**
     * Update the item list.
     */
    private void updateResources()
    {
        final Set<BlockPos> containerList = new HashSet<>(building.getContainerList());

        final Map<ItemStorage, Integer> storedItems = new HashMap<>();
        final Level world = building.getColony().getWorld();
        containerList.add(building.getPosition());

        for (final BlockPos blockPos : containerList)
        {
            final BlockEntity rack = world.getBlockEntity(blockPos);
            if (rack instanceof TileEntityRack)
            {
                final Map<ItemStorage, Integer> rackStorage = ((TileEntityRack) rack).getAllContent();

                for (final Map.Entry<ItemStorage, Integer> entry : rackStorage.entrySet())
                {
                    if (storedItems.containsKey(entry.getKey()))
                    {
                        storedItems.put(entry.getKey(), storedItems.get(entry.getKey()) + entry.getValue());
                    }
                    else
                    {
                        storedItems.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        final List<ItemStorage> filterItems = new ArrayList<>();
        storedItems.forEach((storage, amount) -> {
            storage.setAmount(amount);
            filterItems.add(storage);
        });
        final Predicate<ItemStorage> filterPredicate = stack -> filter.isEmpty()
            || stack.getItemStack().getDescriptionId().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
            || getString(stack.getItemStack())
            .toLowerCase(Locale.US)
            .contains(filter.toLowerCase(Locale.US));

        allItems.clear();
        if (filter.isEmpty())
        {
            allItems.addAll(filterItems);
        }
        else
        {
            allItems.addAll(filterItems.stream().filter(filterPredicate).toList());
            allItems.sort(Comparator.comparingInt(s1 -> StringUtils.getLevenshteinDistance(s1.getItemStack().getHoverName().getString(), filter)));
        }

        final Comparator<ItemStorage> compareByName = Comparator.comparing((ItemStorage o) -> o.getItemStack().getHoverName().getString());
        final Comparator<ItemStorage> compareByCount = Comparator.comparingInt(ItemStorage::getAmount);
        switch (sortDescriptor)
        {
            case ASC_SORT:
                allItems.sort(compareByName);
                break;
            case COUNT_DESC_SORT:
                allItems.sort(compareByCount.reversed());
                break;
        }

        updateResourceList();
    }

    /**
     * Get identifying string from itemstack.
     *
     * @param stack the stack to gen the string from.
     * @return a single string.
     */
    private static String getString(final ItemStack stack)
    {
        final StringBuilder output = new StringBuilder();
        for (final Component comp : stack.getTooltipLines(Minecraft.getInstance().player, TooltipFlag.Default.NORMAL))
        {
            output.append(comp.getString()).append(" ");
        }
        return output.toString();
    }

    /**
     * Updates the resource list in the GUI with the info we need.
     */
    private void updateResourceList()
    {
        stackList.enable();

        //Creates a dataProvider for the unemployed stackList.
        stackList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return allItems.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ItemStorage resource = allItems.get(index);
                final Text resourceLabel = rowPane.findPaneOfTypeByID("ressourceStackName", Text.class);
                final String name = resource.getItemStack().getHoverName().getString();
                resourceLabel.setText(Component.literal(name.substring(0, Math.min(17, name.length()))));
                final Text qtys = rowPane.findPaneOfTypeByID("quantities", Text.class);
                if (!Screen.hasShiftDown())
                {
                    qtys.setText(Component.literal(Utils.format(resource.getAmount())));
                }
                else
                {
                    qtys.setText(Component.literal(Integer.toString(resource.getAmount())));
                }
                final Item imagesrc = resource.getItemStack().getItem();
                final ItemStack image = new ItemStack(imagesrc, 1);
                image.setTag(resource.getItemStack().getTag());
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(image);
            }
        });
    }
}
