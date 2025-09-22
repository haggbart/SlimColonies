package com.minecolonies.api.items;

import net.minecraft.world.item.Item;

/**
 * Class handling the registering of the mod items.
 * <p>
 * We disabled the following finals since we are neither able to mark the items as final, nor do we want to provide public accessors.
 */
@SuppressWarnings({"squid:ClassVariableVisibilityCheck", "squid:S2444", "squid:S1444"})
public final class ModItems
{
    public static Item supplyChest;
    public static Item permTool;
    public static Item scepterGuard;
    public static Item assistantHammer_Iron;
    public static Item assistantHammer_Gold;
    public static Item assistantHammer_Diamond;
    public static Item bannerRallyGuards;
    public static Item supplyCamp;
    public static Item ancientTome;
    public static Item scimitar;
    public static Item scepterLumberjack;
    public static Item questLog;
    public static Item scepterBeekeeper;
    public static Item mistletoe;

    public static Item clipboard;
    public static Item compost;
    public static Item resourceScroll;

    public static Item plateArmorHelmet;
    public static Item plateArmorChest;
    public static Item plateArmorLegs;
    public static Item plateArmorBoots;

    public static Item santaHat;

    public static Item flagBanner;
    public static Item irongate;
    public static Item woodgate;

    public static Item breadDough;
    public static Item cookieDough;
    public static Item cakeBatter;
    public static Item rawPumpkinPie;

    // Basic ingredient items
    public static Item muffin_dough;
    public static Item manchet_dough;
    public static Item raw_noodle;
    public static Item butter;
    public static Item cornmeal;
    public static Item creamcheese;
    public static Item soysauce;

    public static Item adventureToken;

    public static Item scrollColonyTP;
    public static Item scrollColonyAreaTP;
    public static Item scrollBuff;
    public static Item scrollGuardHelp;
    public static Item scrollHighLight;

    public static Item sifterMeshString;
    public static Item sifterMeshFlint;
    public static Item sifterMeshIron;
    public static Item sifterMeshDiamond;

    public static Item magicpotion;

    public static Item buildGoggles;
    public static Item scanAnalyzer;
    public static Item colonyMap;

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModItems()
    {
        /*
         * Intentionally left empty.
         */
    }
}
