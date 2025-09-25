package no.monopixel.slimcolonies.api.inventory;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.RegistryObject;
import no.monopixel.slimcolonies.api.inventory.container.*;

public class ModContainers
{
    public static RegistryObject<MenuType<ContainerCraftingFurnace>> craftingFurnace;

    public static RegistryObject<MenuType<ContainerBuildingInventory>> buildingInv;

    public static RegistryObject<MenuType<ContainerCitizenInventory>> citizenInv;

    public static RegistryObject<MenuType<ContainerRack>> rackInv;

    public static RegistryObject<MenuType<ContainerGrave>> graveInv;

    public static RegistryObject<MenuType<ContainerCrafting>> craftingGrid;

    public static RegistryObject<MenuType<ContainerCraftingBrewingstand>> craftingBrewingstand;
}
