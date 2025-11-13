package no.monopixel.slimcolonies.api.compatibility;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import no.monopixel.slimcolonies.api.SlimColoniesAPIProxy;
import no.monopixel.slimcolonies.api.colony.requestsystem.StandardFactoryController;
import no.monopixel.slimcolonies.api.crafting.CompostRecipe;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.api.crafting.registry.ModRecipeSerializer;
import no.monopixel.slimcolonies.api.items.CheckedNbtKey;
import no.monopixel.slimcolonies.api.items.ModTags;
import no.monopixel.slimcolonies.api.util.*;
import no.monopixel.slimcolonies.core.colony.crafting.CustomRecipeManager;
import no.monopixel.slimcolonies.core.colony.crafting.LootTableAnalyzer;
import no.monopixel.slimcolonies.core.compatibility.gregtech.GregTechOreHelper;
import no.monopixel.slimcolonies.core.generation.ItemNbtCalculator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.monopixel.slimcolonies.api.util.ItemStackUtils.*;
import static no.monopixel.slimcolonies.api.util.constant.Constants.DEFAULT_TAB_KEY;
import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_SAP_LEAF;

/**
 * CompatibilityManager handling certain list and maps of itemStacks of certain types.
 */
public class CompatibilityManager implements ICompatibilityManager
{
    /**
     * Maximum depth sub items are explored at
     */
    private static final int MAX_DEPTH = 100;

    // Overworld base stone tags
    private static final TagKey<Block> TAG_ORES_IN_STONE     = BlockTags.create(ResourceLocation.fromNamespaceAndPath("forge", "ores_in_ground/stone"));
    private static final TagKey<Block> TAG_ORES_IN_DEEPSLATE = BlockTags.create(ResourceLocation.fromNamespaceAndPath("forge", "ores_in_ground/deepslate"));

    /**
     * BiMap of saplings and leaves.
     */
    private final Map<Block, ItemStorage> leavesToSaplingMap = new HashMap<>();

    /**
     * List of saplings. Works on client and server-side.
     */
    private final List<ItemStorage> saplings = new ArrayList<>();

    /**
     * List of all ore-like blocks. Works on client and server-side.
     */
    private final Set<Block> oreBlocks = new HashSet<>();

    /**
     * Pre-computed map of breakable ore items. Items that drop themselves when broken are excluded.
     */
    private final Map<Item, Boolean> breakableOreCache = new HashMap<>();

    /**
     * List of all ore-like items
     */
    private final Set<ItemStorage> smeltableOres = new HashSet<>();

    /**
     * List of minable ore blocks (for miner priority GUI - overworld only).
     */
    private final Set<ItemStorage> minableOres = new HashSet<>();

    /**
     * List of all the compost recipes
     */
    private final Map<Item, CompostRecipe> compostRecipes = new HashMap<>();

    /**
     * List of all the items that can be planted.
     */
    private final Set<ItemStorage> plantables = new HashSet<>();

    /**
     * List of all the items that can be used as fuel
     */
    private final Set<ItemStorage> fuel = new HashSet<>();

    /**
     * List of all the items that can be used as food
     */
    private final Set<ItemStorage> food = new HashSet<>();

    /**
     * List of all the items that can be used as food
     */
    private final Set<ItemStorage> edibles = new HashSet<>();

    /**
     * Set of all beekeeper flowers.
     */
    private ImmutableSet<ItemStorage> beekeeperflowers = ImmutableSet.of();

    /**
     * List of all blocks.
     */
    private static ImmutableList<ItemStack> allItems = ImmutableList.of();

    /**
     * Hashmap of mobs we may or may not attack.
     */
    private ImmutableSet<ResourceLocation> monsters = ImmutableSet.of();

    /**
     * Mapping of itemstorage to creativemodetab.
     */
    private final Map<ItemStorage, CreativeModeTab> creativeModeTabMap = new HashMap<>();

    /**
     * Instantiates the compatibilityManager.
     */
    public CompatibilityManager()
    {
        /*
         * Intentionally left empty.
         */
    }

    private void clear()
    {
        saplings.clear();
        oreBlocks.clear();
        smeltableOres.clear();
        minableOres.clear();
        plantables.clear();
        beekeeperflowers = ImmutableSet.of();

        food.clear();
        edibles.clear();
        fuel.clear();
        breakableOreCache.clear();
        compostRecipes.clear();

        monsters = ImmutableSet.of();
        creativeModeTabMap.clear();
    }

    /**
     * Called server-side *only* to calculate the various lists of items from the registry, recipes, and tags.
     *
     * @param recipeManager The vanilla recipe manager.
     */
    @Override
    public void discover(@NotNull final RecipeManager recipeManager, final Level level)
    {
        clear();
        discoverAllItems(level);

        discoverModCompat();
        discoverBreakableOres();

        discoverCompostRecipes(recipeManager);
        discoverMobs();
    }

    @Override
    public void serialize(@NotNull final FriendlyByteBuf buf)
    {
        serializeItemStorageList(buf, saplings);
        serializeBlockList(buf, oreBlocks);
        serializeItemStorageList(buf, smeltableOres);
        serializeItemStorageList(buf, minableOres);
        serializeItemStorageList(buf, plantables);
        serializeItemStorageList(buf, beekeeperflowers);

        serializeItemStorageList(buf, food);
        serializeItemStorageList(buf, edibles);
        serializeItemStorageList(buf, fuel);
        serializeRegistryIds(buf, ForgeRegistries.ENTITY_TYPES, monsters);

        serializeCompostRecipes(buf, compostRecipes);

        buf.writeInt(CHECKED_NBT_KEYS.size());
        for (final var entry : CHECKED_NBT_KEYS.entrySet())
        {
            buf.writeInt(BuiltInRegistries.ITEM.getId(entry.getKey()));
            buf.writeInt(entry.getValue().size());
            for (final CheckedNbtKey key : entry.getValue())
            {
                ItemNbtCalculator.serializeKeyToBuffer(key, buf);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void deserialize(@NotNull final FriendlyByteBuf buf, final ClientLevel level)
    {
        clear();
        discoverAllItems(level);

        saplings.addAll(deserializeItemStorageList(buf));
        oreBlocks.addAll(deserializeBlockList(buf));
        smeltableOres.addAll(deserializeItemStorageList(buf));
        minableOres.addAll(deserializeItemStorageList(buf));
        plantables.addAll(deserializeItemStorageList(buf));
        beekeeperflowers = ImmutableSet.copyOf(deserializeItemStorageList(buf));

        food.addAll(deserializeItemStorageList(buf));
        edibles.addAll(deserializeItemStorageList(buf));
        fuel.addAll(deserializeItemStorageList(buf));
        monsters = ImmutableSet.copyOf(deserializeRegistryIds(buf, ForgeRegistries.ENTITY_TYPES));

        Log.getLogger().info("Synchronized {} saplings", saplings.size());
        Log.getLogger().info("Synchronized {} ore blocks with {} smeltable ores and {} minable ores", oreBlocks.size(), smeltableOres.size(), minableOres.size());
        Log.getLogger().info("Synchronized {} plantables", plantables.size());
        Log.getLogger().info("Synchronized {} flowers", beekeeperflowers.size());

        Log.getLogger().info("Synchronized {} food types with {} edible", food.size(), edibles.size());
        Log.getLogger().info("Synchronized {} fuel types", fuel.size());
        Log.getLogger().info("Synchronized {} monsters", monsters.size());

        discoverCompostRecipes(deserializeCompostRecipes(buf));

        // the below are loaded from config files, which have been synched already by this point
        discoverModCompat();

        for (int i = 0, amount = buf.readInt(); i < amount; i++)
        {
            final Item item = BuiltInRegistries.ITEM.byId(buf.readInt());
            Set<CheckedNbtKey> nbtKeys = new HashSet<>();
            for (int j = 0, children = buf.readInt(); j < children; j++)
            {
                nbtKeys.add(ItemNbtCalculator.deSerializeKeyFromBuffer(buf));
            }

            CHECKED_NBT_KEYS.put(item, nbtKeys);
        }
    }

    private static void serializeItemStorageList(
        @NotNull final FriendlyByteBuf buf,
        @NotNull final Collection<ItemStorage> list)
    {
        buf.writeCollection(list, StandardFactoryController.getInstance()::serialize);
    }

    @NotNull
    private static List<ItemStorage> deserializeItemStorageList(@NotNull final FriendlyByteBuf buf)
    {
        return buf.readList(StandardFactoryController.getInstance()::deserialize);
    }

    private static void serializeBlockList(
        @NotNull final FriendlyByteBuf buf,
        @NotNull final Collection<Block> list)
    {
        buf.writeCollection(list.stream().map(ItemStack::new).toList(), FriendlyByteBuf::writeItem);
    }

    @NotNull
    private static List<Block> deserializeBlockList(@NotNull final FriendlyByteBuf buf)
    {
        final List<ItemStack> stacks = buf.readList(FriendlyByteBuf::readItem);
        return stacks.stream()
            .flatMap(stack -> stack.getItem() instanceof BlockItem blockItem
                ? Stream.of(blockItem.getBlock()) : Stream.empty())
            .toList();
    }

    private static void serializeRegistryIds(
        @NotNull final FriendlyByteBuf buf,
        @NotNull final IForgeRegistry<?> registry,
        @NotNull final Collection<ResourceLocation> ids)
    {
        buf.writeCollection(ids, (b, id) -> b.writeRegistryIdUnsafe(registry, id));
    }

    @NotNull
    private static <T> List<ResourceLocation>
    deserializeRegistryIds(
        @NotNull final FriendlyByteBuf buf,
        @NotNull final IForgeRegistry<T> registry)
    {
        return buf.readList(b -> b.readRegistryIdUnsafe(registry)).stream()
            .flatMap(item -> Stream.ofNullable(registry.getKey(item)))
            .toList();
    }

    private static void serializeCompostRecipes(
        @NotNull final FriendlyByteBuf buf,
        @NotNull final Map<Item, CompostRecipe> compostRecipes)
    {
        final List<CompostRecipe> recipes = compostRecipes.values().stream().distinct().toList();
        buf.writeCollection(recipes, ModRecipeSerializer.CompostRecipeSerializer.get()::toNetwork);
    }

    @NotNull
    private static List<CompostRecipe> deserializeCompostRecipes(@NotNull final FriendlyByteBuf buf)
    {
        final CompostRecipe.Serializer serializer = ModRecipeSerializer.CompostRecipeSerializer.get();
        final ResourceLocation empty = new ResourceLocation("");
        return buf.readList(b -> serializer.fromNetwork(empty, b));
    }

    /**
     * Getter for the list.
     *
     * @return the list of itemStacks.
     */
    @Override
    public List<ItemStack> getListOfAllItems()
    {
        if (allItems.isEmpty())
        {
            Log.getLogger().error("getListOfAllItems when empty");
        }
        return allItems;
    }

    @Override
    public Set<ItemStorage> getSetOfAllItems()
    {
        if (creativeModeTabMap.isEmpty())
        {
            Log.getLogger().error("getSetOfAllItems when empty");
        }
        return creativeModeTabMap.keySet();
    }

    @Override
    public boolean isPlantable(final ItemStack itemStack)
    {
        return !itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem && itemStack.is(ModTags.floristFlowers);
    }

    @Override
    public boolean isLuckyBlock(final Block block)
    {
        return block.defaultBlockState().is(ModTags.oreChanceBlocks);
    }

    @Nullable
    @Override
    public ItemStack getSaplingForLeaf(final Block block)
    {
        if (leavesToSaplingMap.containsKey(block))
        {
            return leavesToSaplingMap.get(block).getItemStack();
        }
        return null;
    }

    @Override
    public Set<ItemStorage> getCopyOfSaplings()
    {
        if (saplings.isEmpty())
        {
            Log.getLogger().error("getCopyOfSaplings when empty");
        }
        return new HashSet<>(saplings);
    }

    @Override
    public Set<ItemStorage> getFuel()
    {
        if (fuel.isEmpty())
        {
            Log.getLogger().error("getFuel when empty");
        }
        return fuel;
    }

    @Override
    public Set<ItemStorage> getFood()
    {
        if (food.isEmpty())
        {
            Log.getLogger().error("getFood when empty");
        }
        return food;
    }

    @Override
    public Set<ItemStorage> getEdibles(final int minNutrition)
    {
        if (edibles.isEmpty())
        {
            Log.getLogger().error("getEdibles when empty");
        }
        final Set<ItemStorage> filteredEdibles = new HashSet<>();
        for (final ItemStorage storage : edibles)
        {
            if ((storage.getItemStack().getFoodProperties(null) != null && storage.getItemStack().getFoodProperties(null).getNutrition() >= minNutrition))
            {
                filteredEdibles.add(storage);
            }
        }
        return filteredEdibles;
    }

    @Override
    public Set<ItemStorage> getSmeltableOres()
    {
        if (smeltableOres.isEmpty())
        {
            Log.getLogger().error("getSmeltableOres when empty");
        }
        return smeltableOres;
    }

    @Override
    public Set<ItemStorage> getMinableOres()
    {
        if (minableOres.isEmpty())
        {
            Log.getLogger().error("getMinableOres when empty");
        }
        return minableOres;
    }

    @Override
    public Map<Item, CompostRecipe> getCopyOfCompostRecipes()
    {
        if (compostRecipes.isEmpty())
        {
            Log.getLogger().error("getCopyOfCompostRecipes when empty");
        }
        return ImmutableMap.copyOf(compostRecipes);
    }

    @Override
    public Set<ItemStorage> getCompostInputs()
    {
        if (compostRecipes.isEmpty())
        {
            Log.getLogger().error("getCompostInputs when empty");
        }
        return compostRecipes.keySet().stream()
            .map(item -> new ItemStorage(new ItemStack(item)))
            .collect(Collectors.toSet());
    }

    @Override
    public Set<ItemStorage> getCopyOfPlantables()
    {
        if (plantables.isEmpty())
        {
            Log.getLogger().error("getCopyOfPlantables when empty");
        }
        return new HashSet<>(plantables);
    }

    @Override
    public Set<ItemStorage> getImmutableFlowers()
    {
        if (beekeeperflowers.isEmpty())
        {
            Log.getLogger().error("getImmutableFlowers when empty");
        }
        return beekeeperflowers;
    }

    @Override
    public boolean isOre(final BlockState block)
    {
        if (oreBlocks.isEmpty())
        {
            Log.getLogger().error("isOre when empty");
        }

        return oreBlocks.contains(block.getBlock());
    }

    @Override
    public boolean isOre(@NotNull final ItemStack stack)
    {
        if (isBreakableOre(stack))
        {
            return true;
        }
        if (isMineableOre(stack) || stack.is(ModTags.raw_ore))
        {
            ItemStack smeltingResult = SlimColoniesAPIProxy.getInstance().getFurnaceRecipes().getSmeltingResult(stack);
            return !smeltingResult.isEmpty();
        }

        return false;
    }

    @Override
    public boolean isMineableOre(@NotNull final ItemStack stack)
    {
        return !isEmpty(stack) && stack.is(Tags.Items.ORES);
    }

    @Override
    public boolean isBreakableOre(@NotNull final ItemStack stack)
    {
        return breakableOreCache.getOrDefault(stack.getItem(), false);
    }

    @Override
    public void write(@NotNull final CompoundTag compound)
    {
        @NotNull final ListTag saplingsLeavesTagList =
            leavesToSaplingMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null)
                .map(entry -> writeLeafSaplingEntryToNBT(entry.getKey().defaultBlockState(), entry.getValue()))
                .collect(NBTUtils.toListNBT());
        compound.put(TAG_SAP_LEAF, saplingsLeavesTagList);
    }

    @Override
    public void read(@NotNull final CompoundTag compound)
    {
        NBTUtils.streamCompound(compound.getList(TAG_SAP_LEAF, Tag.TAG_COMPOUND))
            .map(CompatibilityManager::readLeafSaplingEntryFromNBT)
            .filter(key -> !key.getA().isAir() && !leavesToSaplingMap.containsKey(key.getA().getBlock()) && !leavesToSaplingMap.containsValue(key.getB()))
            .forEach(key -> leavesToSaplingMap.put(key.getA().getBlock(), key.getB()));
    }

    @Override
    public void connectLeafToSapling(final Block leaf, final ItemStack stack)
    {
        if (!leavesToSaplingMap.containsKey(leaf))
        {
            leavesToSaplingMap.put(leaf, new ItemStorage(stack, false, true));
        }
    }

    @Override
    public CreativeModeTab getCreativeTab(final ItemStorage checkItem)
    {
        return creativeModeTabMap.get(checkItem);
    }

    @Override
    public int getCreativeTabKey(final ItemStorage checkItem)
    {
        final CreativeModeTab creativeTab = creativeModeTabMap.get(checkItem);
        return creativeTab == null ? DEFAULT_TAB_KEY : creativeModeTabMap.get(checkItem).column();
    }

    @Override
    public ImmutableSet<ResourceLocation> getAllMonsters()
    {
        if (monsters.isEmpty())
        {
            Log.getLogger().error("getAllMonsters when empty");
        }
        return monsters;
    }

    //------------------------------- Private Utility Methods -------------------------------//

    /**
     * Calculate all monsters.
     */
    private void discoverMobs()
    {
        Set<ResourceLocation> monsterSet = new HashSet<>();

        for (final Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : ForgeRegistries.ENTITY_TYPES.getEntries())
        {
            if (entry.getValue().getCategory() == MobCategory.MONSTER)
            {
                monsterSet.add(entry.getKey().location());
            }
            else if (entry.getValue().is(ModTags.hostile))
            {
                monsterSet.add(entry.getKey().location());
            }
        }

        monsters = ImmutableSet.copyOf(monsterSet);
    }

    /**
     * Create complete list of all existing items, client side only.
     */
    private void discoverAllItems(final Level level)
    {
        if (!food.isEmpty())
        {
            return;
        }

        final Set<ItemStorage> tempDuplicates = new HashSet<>();
        final Set<ItemStorage> tempFlowers = new HashSet<>();

        final CreativeModeTab.ItemDisplayParameters tempDisplayParams = new CreativeModeTab.ItemDisplayParameters(level.enabledFeatures(), false, level.registryAccess());

        final ImmutableList.Builder<ItemStack> listBuilder = new ImmutableList.Builder<>();

        CraftingUtils.forEachCreativeTabItems(tempDisplayParams, (tab, stacks) ->
        {
            final Object2IntLinkedOpenHashMap<Item> mapping = new Object2IntLinkedOpenHashMap<>();
            for (final ItemStack item : stacks)
            {
                if (!tempDuplicates.add(new ItemStorage(item)) || mapping.addTo(item.getItem(), 1) > MAX_DEPTH)
                {
                    continue;
                }

                listBuilder.add(item);
                discoverSaplings(item);
                discoverOres(item);
                discoverPlantables(item);
                discoverFood(item);
                discoverFuel(item);
                discoverBeekeeperFlowers(item, tempFlowers);

                creativeModeTabMap.put(new ItemStorage(item), tab);
            }
        });

        discoverFungi();

        beekeeperflowers = ImmutableSet.copyOf(tempFlowers);
        Log.getLogger().info("Finished discovering ores " + oreBlocks.size() + " " + smeltableOres.size());
        Log.getLogger().info("Finished discovering saplings " + saplings.size());
        Log.getLogger().info("Finished discovering plantables " + plantables.size());
        Log.getLogger().info("Finished discovering food " + edibles.size() + " " + food.size());
        Log.getLogger().info("Finished discovering fuel " + fuel.size());
        Log.getLogger().info("Finished discovering flowers " + beekeeperflowers.size());


        allItems = listBuilder.build();
        Log.getLogger().info("Finished discovering items " + allItems.size());
    }

    /**
     * Discover all flowers for the beekeeper.
     */
    private void discoverBeekeeperFlowers(final ItemStack item, final Set<ItemStorage> tempFlowers)
    {
        if (item.is(ItemTags.FLOWERS))
        {
            tempFlowers.add(new ItemStorage(item));
        }
    }

    /**
     * Discover ores for the Smelter and Miners.
     */
    private void discoverOres(final ItemStack stack)
    {
        if (stack.is(Tags.Items.ORES) || stack.is(ModTags.breakable_ore) || stack.is(ModTags.raw_ore))
        {
            boolean isBlockItem = false;
            boolean isFromOtherDimension = false;

            if (stack.getItem() instanceof BlockItem blockItem)
            {
                isBlockItem = true;
                final Block block = blockItem.getBlock();
                final BlockState state = block.defaultBlockState();

                // Check dimension using both tag whitelist and GregTech integration
                boolean filteredByTags = isOreFromOtherDimension(state);
                boolean filteredByGregTech = GregTechOreHelper.isNonOverworldOnly(stack);
                isFromOtherDimension = filteredByTags || filteredByGregTech;

                if (!isFromOtherDimension)
                {
                    oreBlocks.add(block);
                }
            }

            // smeltableOres: ALL ores with smelting recipes (for smelters - includes all dimensions)
            if (!SlimColoniesAPIProxy.getInstance().getFurnaceRecipes().getSmeltingResult(stack).isEmpty())
            {
                smeltableOres.add(new ItemStorage(stack));
            }

            // minableOres: only overworld ore BLOCKS (for miner priority GUI)
            if (isBlockItem && !isFromOtherDimension &&
                !SlimColoniesAPIProxy.getInstance().getFurnaceRecipes().getSmeltingResult(stack).isEmpty())
            {
                minableOres.add(new ItemStorage(stack));
            }
        }
    }

    /**
     * Check if an ore block is from a non-overworld dimension.
     *
     * @param state the block state to check
     * @return true if the ore is NOT from overworld (not in stone or deepslate)
     */
    private boolean isOreFromOtherDimension(final BlockState state)
    {
        return !state.is(TAG_ORES_IN_STONE) && !state.is(TAG_ORES_IN_DEEPSLATE);
    }

    /**
     * Discover saplings from the vanilla Saplings tag, used for the Forester
     */
    private void discoverSaplings(final ItemStack stack)
    {
        if (stack.is(ItemTags.SAPLINGS) || stack.is(Tags.Items.MUSHROOMS) || stack.is(ModTags.fungi))
        {
            saplings.add(new ItemStorage(stack, false, true));
        }
    }

    /**
     * "Discover" associated saplings for fungi; there currently isn't a great way to do this automatically,
     * so it's just hard-coded for now.  (TODO: datapack this in 1.20.4?)
     */
    private void discoverFungi()
    {
        // regular saplings and overworld mushrooms are discovered by loot drops, so will populate this table on
        // their own (though only after the first tree is cut); nether "leaves" don't drop saplings by default
        // though, so we instead use this table to force that.
        leavesToSaplingMap.put(Blocks.NETHER_WART_BLOCK, new ItemStorage(new ItemStack(Items.CRIMSON_FUNGUS)));
        leavesToSaplingMap.put(Blocks.WARPED_WART_BLOCK, new ItemStorage(new ItemStack(Items.WARPED_FUNGUS)));
    }

    /**
     * Create complete list of compost recipes.
     *
     * @param recipeManager recipe manager
     */
    private void discoverCompostRecipes(@NotNull final RecipeManager recipeManager)
    {
        if (compostRecipes.isEmpty())
        {
            discoverCompostRecipes(recipeManager.byType(ModRecipeSerializer.CompostRecipeType.get()).values().stream()
                .map(r -> (CompostRecipe) r).toList());
            Log.getLogger().info("Finished discovering compostables " + compostRecipes.size());
        }
    }

    private void discoverCompostRecipes(@NotNull final List<CompostRecipe> recipes)
    {
        for (final CompostRecipe recipe : recipes)
        {
            for (final ItemStack stack : recipe.getInput().getItems())
            {
                // there can be duplicates due to overlapping tags.  weakest one wins.
                compostRecipes.merge(stack.getItem(), recipe,
                    (r1, r2) -> r1.getStrength() < r2.getStrength() ? r1 : r2);
            }
        }
    }

    /**
     * Create complete list of plantable items, from the "slimcolonies:florist_flowers" tag, for the Florist.
     */
    private void discoverPlantables(final ItemStack stack)
    {
        if (stack.is(ModTags.floristFlowers))
        {
            if (stack.getItem() instanceof BlockItem)
            {
                plantables.add(new ItemStorage(stack));
            }
        }
    }

    /**
     * Create complete list of fuel items.
     */
    private void discoverFuel(final ItemStack stack)
    {
        if (FurnaceBlockEntity.isFuel(stack))
        {
            fuel.add(new ItemStorage(stack));
        }
    }

    /**
     * Create complete list of food items.
     */
    private void discoverFood(final ItemStack stack)
    {
        if (ISFOOD.test(stack) || ISCOOKABLE.test(stack))
        {
            food.add(new ItemStorage(stack));
            if (FoodUtils.EDIBLE.test(stack))
            {
                edibles.add(new ItemStorage(stack));
            }
        }
    }

    private static CompoundTag writeLeafSaplingEntryToNBT(final BlockState state, final ItemStorage storage)
    {
        final CompoundTag compound = NbtUtils.writeBlockState(state);
        storage.getItemStack().save(compound);
        return compound;
    }

    private static Tuple<BlockState, ItemStorage> readLeafSaplingEntryFromNBT(final CompoundTag compound)
    {
        return new Tuple<>(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), compound), new ItemStorage(ItemStack.of(compound), false, true));
    }

    /**
     * Inits compats
     */
    private void discoverModCompat()
    {
        // No mod compatibility currently supported
    }

    private void discoverBreakableOres()
    {
        for (final Item item : ForgeRegistries.ITEMS.tags().getTag(ModTags.breakable_ore))
        {
            final ItemStack stack = item.getDefaultInstance();
            final Block block = Block.byItem(item);
            if (block.defaultBlockState().isAir())
            {
                breakableOreCache.put(item, false);
                continue;
            }

            final List<LootTableAnalyzer.LootDrop> drops = CustomRecipeManager.getInstance().getLootDrops(block.getLootTable());
            boolean dropsSelf = false;
            for (final LootTableAnalyzer.LootDrop drop : drops)
            {
                for (final ItemStack dropStack : drop.getItemStacks())
                {
                    if (ItemStackUtils.compareItemStacksIgnoreStackSize(stack, dropStack))
                    {
                        dropsSelf = true;
                        break;
                    }
                }
                if (dropsSelf)
                {
                    break;
                }
            }
            breakableOreCache.put(item, !dropsSelf);
        }
    }

    @Override
    public int getNumberOfSaplings()
    {
        return saplings.size();
    }
}
