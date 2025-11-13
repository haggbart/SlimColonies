package no.monopixel.slimcolonies.core.compatibility.gregtech;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import no.monopixel.slimcolonies.api.util.Log;

import java.lang.reflect.Method;

public class GregTechOreHelper
{
    private static final String GREGTECH_MOD_ID = "gtceu";
    private static Boolean isGregTechLoaded = null;
    private static Object oreVeinsRegistry = null;
    private static boolean initialized = false;
    private static final java.util.Map<String, java.util.Set<String>> materialDimensions = new java.util.HashMap<>();

    public static boolean isLoaded()
    {
        if (isGregTechLoaded == null)
        {
            isGregTechLoaded = ModList.get().isLoaded(GREGTECH_MOD_ID);
        }
        return isGregTechLoaded;
    }

    private static boolean initializeReflection()
    {
        if (initialized)
        {
            return oreVeinsRegistry != null;
        }

        initialized = true;

        try
        {
            Class<?> gtRegistriesClass = Class.forName("com.gregtechceu.gtceu.api.registry.GTRegistries");
            java.lang.reflect.Field oreVeinsField = gtRegistriesClass.getDeclaredField("ORE_VEINS");
            oreVeinsRegistry = oreVeinsField.get(null);

            if (oreVeinsRegistry == null)
            {
                Log.getLogger().warn("GregTech ORE_VEINS registry is null");
                return false;
            }

            Log.getLogger().info("Successfully initialized GregTech ore vein integration");
            buildMaterialDimensionMap();

            return true;
        }
        catch (final Exception e)
        {
            Log.getLogger().warn("Failed to initialize GregTech reflection: " + e.getMessage());
            return false;
        }
    }

    private static void buildMaterialDimensionMap()
    {
        try
        {
            Method valuesMethod = oreVeinsRegistry.getClass().getMethod("values");
            Object veinsCollection = valuesMethod.invoke(oreVeinsRegistry);

            if (!(veinsCollection instanceof Iterable))
            {
                Log.getLogger().warn("GregTech ore veins collection is not iterable");
                return;
            }

            int veinCount = 0;

            for (Object oreDefinition : (Iterable<?>) veinsCollection)
            {
                try
                {
                    Method dimensionFilterMethod = oreDefinition.getClass().getMethod("dimensionFilter");
                    Object dimFilter = dimensionFilterMethod.invoke(oreDefinition);

                    if (dimFilter instanceof java.util.Set)
                    {
                        @SuppressWarnings("unchecked")
                        java.util.Set<net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>> dimSet =
                            (java.util.Set<net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>>) dimFilter;

                        java.util.Set<String> dimensionNames = new java.util.HashSet<>();
                        for (net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim : dimSet)
                        {
                            dimensionNames.add(dim.location().toString());
                        }

                        try
                        {
                            Method veinGenMethod = oreDefinition.getClass().getMethod("veinGenerator");
                            Object veinGen = veinGenMethod.invoke(oreDefinition);

                            Method getAllEntriesMethod = veinGen.getClass().getMethod("getAllEntries");
                            Object entries = getAllEntriesMethod.invoke(veinGen);

                            if (entries instanceof Iterable)
                            {
                                for (Object entry : (Iterable<?>) entries)
                                {
                                    String materialId = extractMaterialId(entry.toString());
                                    if (materialId != null)
                                    {
                                        materialDimensions.computeIfAbsent(materialId, k -> new java.util.HashSet<>())
                                            .addAll(dimensionNames);
                                    }
                                }
                            }
                        }
                        catch (Exception e)
                        {
                        }
                    }

                    veinCount++;
                }
                catch (Exception e)
                {
                }
            }

            Log.getLogger().info("Mapped {} GregTech materials from {} ore veins", materialDimensions.size(), veinCount);
        }
        catch (Exception e)
        {
            Log.getLogger().warn("Failed to build material-dimension map: {}", e.getMessage());
        }
    }

    private static String extractMaterialId(final String entryStr)
    {
        try
        {
            int startIdx = entryStr.indexOf('[');
            int endIdx = entryStr.indexOf(']');

            if (startIdx != -1 && endIdx != -1 && endIdx > startIdx)
            {
                String fullId = entryStr.substring(startIdx + 1, endIdx);
                int colonIdx = fullId.indexOf(':');
                if (colonIdx != -1)
                {
                    return fullId.substring(colonIdx + 1);
                }
                return fullId;
            }
        }
        catch (Exception e)
        {
        }
        return null;
    }

    public static boolean isNonOverworldOnly(final ItemStack oreStack)
    {
        if (!isLoaded())
        {
            return false;
        }

        if (!initializeReflection())
        {
            return false;
        }

        ResourceLocation itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(oreStack.getItem());
        if (itemId == null || !itemId.getNamespace().equals(GREGTECH_MOD_ID))
        {
            return false;
        }

        String materialName = extractMaterialFromOreName(itemId.getPath());
        if (materialName == null)
        {
            return false;
        }

        java.util.Set<String> dimensions = materialDimensions.get(materialName);
        if (dimensions == null || dimensions.isEmpty())
        {
            return false;
        }

        return !dimensions.contains("minecraft:overworld");
    }

    private static String extractMaterialFromOreName(final String itemName)
    {
        String name = itemName;

        if (name.startsWith("deepslate_"))
        {
            name = name.substring("deepslate_".length());
        }
        else if (name.startsWith("netherrack_"))
        {
            name = name.substring("netherrack_".length());
        }
        else if (name.startsWith("endstone_"))
        {
            name = name.substring("endstone_".length());
        }

        if (name.endsWith("_ore"))
        {
            name = name.substring(0, name.length() - "_ore".length());
        }

        return name.isEmpty() ? null : name;
    }
}
