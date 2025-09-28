package no.monopixel.slimcolonies.api.items;

import net.minecraft.world.item.Item;

/**
 * Class handling the registering of the mod items.
 * <p>
 * We disabled the following finals since we are neither able to mark the items as final, nor do we want to provide public accessors.
 */
public final class ModItems
{
    public static Item supplyChest;
    public static Item permTool;
    public static Item scepterGuard;
    public static Item assistantHammer_Iron;
    public static Item assistantHammer_Gold;
    public static Item assistantHammer_Diamond;
    public static Item supplyCamp;
    public static Item ancientTome;
    public static Item scepterLumberjack;
    public static Item questLog;
    public static Item scepterBeekeeper;

    public static Item clipboard;
    public static Item compost;
    public static Item resourceScroll;


    public static Item flagBanner;
    public static Item irongate;
    public static Item woodgate;

    public static Item breadDough;
    public static Item cookieDough;
    public static Item cakeBatter;
    public static Item rawPumpkinPie;

    public static Item adventureToken;

    public static Item sifterMeshString;
    public static Item sifterMeshFlint;
    public static Item sifterMeshIron;
    public static Item sifterMeshDiamond;

    public static Item buildGoggles;
    public static Item scanAnalyzer;

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
