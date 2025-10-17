package no.monopixel.slimcolonies.api.configuration;

import net.minecraftforge.common.ForgeConfigSpec;
import no.monopixel.slimcolonies.api.colony.permissions.Explosions;
import no.monopixel.slimcolonies.api.util.constant.CitizenConstants;

import java.util.List;

/**
 * Mod server configuration. Loaded serverside, synced on connection.
 */
public class ServerConfiguration extends AbstractConfiguration
{
    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Gameplay settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */

    public final ForgeConfigSpec.IntValue     initialCitizenAmount;
    public final ForgeConfigSpec.BooleanValue allowInfiniteSupplyChests;
    public final ForgeConfigSpec.BooleanValue allowInfiniteColonies;
    public final ForgeConfigSpec.BooleanValue allowOtherDimColonies;
    public final ForgeConfigSpec.IntValue     maxCitizenPerColony;
    public final ForgeConfigSpec.BooleanValue enableInDevelopmentFeatures;
    public final ForgeConfigSpec.BooleanValue alwaysRenderNameTag;
    public final ForgeConfigSpec.IntValue     bonusOreChance;
    public final ForgeConfigSpec.IntValue     minThLevelToTeleport;
    public final ForgeConfigSpec.DoubleValue  foodModifier;
    public final ForgeConfigSpec.IntValue     maxVisitorsPerTavern;
    public final ForgeConfigSpec.BooleanValue forceLoadColony;
    public final ForgeConfigSpec.IntValue     loadtime;
    public final ForgeConfigSpec.IntValue     colonyLoadStrictness;
    public final ForgeConfigSpec.IntValue     maxTreeSize;
    public final ForgeConfigSpec.BooleanValue noSupplyPlacementRestrictions;
    public final ForgeConfigSpec.IntValue     builderScavengingIntervalMinutes;
    public final ForgeConfigSpec.IntValue     fieldCooldownMinutes;

    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Research settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */
    public final ForgeConfigSpec.BooleanValue                        researchCreativeCompletion;
    public final ForgeConfigSpec.BooleanValue                        researchDebugLog;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> researchResetCost;

    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Command settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */

    public final ForgeConfigSpec.BooleanValue canPlayerUseRTPCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseColonyTPCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseHomeTPCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseShowColonyInfoCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseKillCitizensCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseModifyCitizensCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseAddOfficerCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseDeleteColonyCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseResetCommand;

    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Claim settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */

    public final ForgeConfigSpec.IntValue maxColonySize;
    public final ForgeConfigSpec.IntValue minColonyDistance;
    public final ForgeConfigSpec.IntValue initialColonySize;
    public final ForgeConfigSpec.IntValue maxDistanceFromWorldSpawn;
    public final ForgeConfigSpec.IntValue minDistanceFromWorldSpawn;

    /*  ------------------------------------------------------------------------- *
     *  ------------------- ######## Combat Settings ######## ------------------- *
     *  ------------------------------------------------------------------------- */

    public final ForgeConfigSpec.BooleanValue mobAttackCitizens;
    public final ForgeConfigSpec.DoubleValue  guardDamageMultiplier;
    public final ForgeConfigSpec.DoubleValue  guardHealthMult;
    public final ForgeConfigSpec.BooleanValue pvp_mode;

    /*  ----------------------------------------------------------------------------- *
     *  ------------------- ######## Permission Settings ######## ------------------- *
     *  ----------------------------------------------------------------------------- */

    public final ForgeConfigSpec.BooleanValue          enableColonyProtection;
    public final ForgeConfigSpec.EnumValue<Explosions> turnOffExplosionsInColonies;

    /*  -------------------------------------------------------------------------------- *
     *  ------------------- ######## Compatibility Settings ######## ------------------- *
     *  -------------------------------------------------------------------------------- */

    public final ForgeConfigSpec.BooleanValue auditCraftingTags;
    public final ForgeConfigSpec.BooleanValue debugInventories;
    public final ForgeConfigSpec.BooleanValue blueprintBuildMode;

    /*  ------------------------------------------------------------------------------ *
     *  ------------------- ######## Pathfinding Settings ######## ------------------- *
     *  ------------------------------------------------------------------------------ */

    public final ForgeConfigSpec.IntValue pathfindingDebugVerbosity;
    public final ForgeConfigSpec.IntValue pathfindingMaxThreadCount;
    public final ForgeConfigSpec.IntValue minimumRailsToPath;

    /*  --------------------------------------------------------------------------------- *
     *  ------------------- ######## Request System Settings ######## ------------------- *
     *  --------------------------------------------------------------------------------- */

    public final ForgeConfigSpec.BooleanValue creativeResolve;

    /*  --------------------------------------------------------------------------------- *
     *  ------------------- ######## Debugging Settings ######## ------------------- *
     *  --------------------------------------------------------------------------------- */

    public final ForgeConfigSpec.BooleanValue netherWorkerTakesDamage;

    /**
     * Builds server configuration.
     *
     * @param builder config builder
     */
    protected ServerConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "gameplay");

        initialCitizenAmount = defineInteger(builder, "initialcitizenamount", 4, 1, 10);
        allowInfiniteSupplyChests = defineBoolean(builder, "allowinfinitesupplychests", false);
        allowInfiniteColonies = defineBoolean(builder, "allowinfinitecolonies", false);
        allowOtherDimColonies = defineBoolean(builder, "allowotherdimcolonies", true);
        maxCitizenPerColony = defineInteger(builder, "maxcitizenpercolony", 250, 30, CitizenConstants.CITIZEN_LIMIT_MAX);
        enableInDevelopmentFeatures = defineBoolean(builder, "enableindevelopmentfeatures", false);
        alwaysRenderNameTag = defineBoolean(builder, "alwaysrendernametag", true);
        bonusOreChance = defineInteger(builder, "bonusorechance", 25, 0, 100);
        minThLevelToTeleport = defineInteger(builder, "minthleveltoteleport", 3, 0, 5);
        foodModifier = defineDouble(builder, "foodmodifier", 1.0, 0.1, 100);
        maxVisitorsPerTavern = defineInteger(builder, "maxvisitorspertavern", 3, 1, 15);
        forceLoadColony = defineBoolean(builder, "forceloadcolony", true);
        loadtime = defineInteger(builder, "loadtime", 10, 1, 1440);
        colonyLoadStrictness = defineInteger(builder, "colonyloadstrictness", 3, 1, 15);
        maxTreeSize = defineInteger(builder, "maxtreesize", 400, 1, 1000);
        noSupplyPlacementRestrictions = defineBoolean(builder, "nosupplyplacementrestrictions", false);
        builderScavengingIntervalMinutes = defineInteger(builder, "builderscavengingintervalminutes", 2, 0, 60);
        fieldCooldownMinutes = defineInteger(builder, "fieldcooldownminutes", 15, 1, 60);

        swapToCategory(builder, "research");
        researchCreativeCompletion = defineBoolean(builder, "researchcreativecompletion", true);
        researchDebugLog = defineBoolean(builder, "researchdebuglog", false);
        researchResetCost = defineList(builder, "researchresetcost", List.of("slimcolonies:ancienttome:1"), s -> s instanceof String);

        swapToCategory(builder, "commands");

        canPlayerUseRTPCommand = defineBoolean(builder, "canplayerusertpcommand", false);
        canPlayerUseColonyTPCommand = defineBoolean(builder, "canplayerusecolonytpcommand", false);
        canPlayerUseHomeTPCommand = defineBoolean(builder, "canplayerusehometpcommand", false);
        canPlayerUseShowColonyInfoCommand = defineBoolean(builder, "canplayeruseshowcolonyinfocommand", true);
        canPlayerUseKillCitizensCommand = defineBoolean(builder, "canplayerusekillcitizenscommand", false);
        canPlayerUseModifyCitizensCommand = defineBoolean(builder, "canplayerusemodifycitizenscommand", false);
        canPlayerUseAddOfficerCommand = defineBoolean(builder, "canplayeruseaddofficercommand", true);
        canPlayerUseDeleteColonyCommand = defineBoolean(builder, "canplayerusedeletecolonycommand", false);
        canPlayerUseResetCommand = defineBoolean(builder, "canplayeruseresetcommand", false);

        swapToCategory(builder, "claims");

        maxColonySize = defineInteger(builder, "maxColonySize", 20, 1, 250);
        minColonyDistance = defineInteger(builder, "minColonyDistance", 8, 1, 200);
        initialColonySize = defineInteger(builder, "initialColonySize", 4, 1, 15);
        maxDistanceFromWorldSpawn = defineInteger(builder, "maxdistancefromworldspawn", 30000, 1000, Integer.MAX_VALUE);
        minDistanceFromWorldSpawn = defineInteger(builder, "mindistancefromworldspawn", 0, 0, 1000);

        swapToCategory(builder, "combat");

        mobAttackCitizens = defineBoolean(builder, "mobattackcitizens", true);
        guardDamageMultiplier = defineDouble(builder, "guardDamageMultiplier", 1.0, 0.1, 15.0);
        guardHealthMult = defineDouble(builder, "guardhealthmult", 1.0, 0.1, 5.0);
        pvp_mode = defineBoolean(builder, "pvp_mode", false);

        swapToCategory(builder, "permissions");

        enableColonyProtection = defineBoolean(builder, "enablecolonyprotection", true);
        turnOffExplosionsInColonies = defineEnum(builder, "turnoffexplosionsincolonies", Explosions.DAMAGE_ENTITIES);

        swapToCategory(builder, "compatibility");

        auditCraftingTags = defineBoolean(builder, "auditcraftingtags", false);
        debugInventories = defineBoolean(builder, "debuginventories", false);
        blueprintBuildMode = defineBoolean(builder, "blueprintbuildmode", false);

        swapToCategory(builder, "pathfinding");

        pathfindingDebugVerbosity = defineInteger(builder, "pathfindingdebugverbosity", 0, 0, 10);
        minimumRailsToPath = defineInteger(builder, "minimumrailstopath", 8, 5, 100);
        pathfindingMaxThreadCount = defineInteger(builder, "pathfindingmaxthreadcount", 1, 1, 10);

        swapToCategory(builder, "requestSystem");

        creativeResolve = defineBoolean(builder, "creativeresolve", false);

        swapToCategory(builder, "debugging");

        netherWorkerTakesDamage = defineBoolean(builder, "netherworkertakesdamage", true);

        finishCategory(builder);
    }
}
