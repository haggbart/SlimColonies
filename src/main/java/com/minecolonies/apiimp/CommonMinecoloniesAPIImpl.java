package com.minecolonies.apiimp;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.colony.ICitizenDataManager;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.registry.IBuildingDataManager;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventDescriptionTypeRegistryEntry;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventTypeRegistryEntry;
import com.minecolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries;
import com.minecolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries.BuildingExtensionEntry;
import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.guardtype.registry.IGuardTypeDataManager;
import com.minecolonies.api.colony.guardtype.registry.ModGuardTypes;
import com.minecolonies.api.colony.interactionhandling.registry.IInteractionResponseHandlerDataManager;
import com.minecolonies.api.colony.interactionhandling.registry.InteractionResponseHandlerEntry;
import com.minecolonies.api.colony.jobs.registry.IJobDataManager;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.compatibility.IFurnaceRecipes;
import com.minecolonies.api.configuration.Configuration;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.crafting.registry.RecipeTypeEntry;
// Happiness imports removed
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.eventbus.DefaultEventBus;
import com.minecolonies.api.eventbus.EventBus;
import com.minecolonies.api.quests.registries.QuestRegistries;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.ModResearchCosts.ResearchCostEntry;
import com.minecolonies.api.research.ModResearchEffects;
import com.minecolonies.api.research.ModResearchRequirements;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.colony.CitizenDataManager;
import com.minecolonies.core.colony.ColonyManager;
import com.minecolonies.core.colony.buildings.registry.BuildingDataManager;
import com.minecolonies.core.colony.interactionhandling.registry.InteractionResponseHandlerManager;
import com.minecolonies.core.colony.jobs.registry.JobDataManager;
import com.minecolonies.core.entity.pathfinding.registry.PathNavigateRegistry;
import com.minecolonies.core.research.GlobalResearchTree;
import com.minecolonies.core.util.FurnaceRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import org.jetbrains.annotations.NotNull;

public class CommonMinecoloniesAPIImpl implements IMinecoloniesAPI
{
    private final  IColonyManager                                          colonyManager          = new ColonyManager();
    private final  ICitizenDataManager                                     citizenDataManager     = new CitizenDataManager();
    private final  IPathNavigateRegistry              pathNavigateRegistry   = new PathNavigateRegistry();
    private        IForgeRegistry<EquipmentTypeEntry> equipmentTypeRegistry;
    private        IForgeRegistry<BuildingEntry>      buildingRegistry;
    private        IForgeRegistry<BuildingExtensionRegistries.BuildingExtensionEntry> buildingExtensionRegistry;
    private final  IBuildingDataManager                                    buildingDataManager    = new BuildingDataManager();
    private final  IJobDataManager                                         jobDataManager         = new JobDataManager();
    private final  IGuardTypeDataManager                                   guardTypeDataManager   = new com.minecolonies.core.colony.buildings.registry.GuardTypeDataManager();
    private        IForgeRegistry<JobEntry>                                jobRegistry;
    private        IForgeRegistry<GuardType>                               guardTypeRegistry;
    private        IForgeRegistry<InteractionResponseHandlerEntry>         interactionHandlerRegistry;
    private final  IInteractionResponseHandlerDataManager                  interactionDataManager = new InteractionResponseHandlerManager();
    private        IForgeRegistry<ColonyEventTypeRegistryEntry>            colonyEventRegistry;
    private        IForgeRegistry<ColonyEventDescriptionTypeRegistryEntry> colonyEventDescriptionRegistry;
    private static IGlobalResearchTree                                     globalResearchTree     = new GlobalResearchTree();
    private        IForgeRegistry<ModResearchRequirements.ResearchRequirementEntry> researchRequirementRegistry;
    private        IForgeRegistry<ModResearchEffects.ResearchEffectEntry>           researchEffectRegistry;
    private        IForgeRegistry<ResearchCostEntry>                                researchCostRegistry;
    private        IForgeRegistry<RecipeTypeEntry>     recipeTypeEntryRegistry;
    private        IForgeRegistry<CraftingType>                            craftingTypeRegistry;
    private        IForgeRegistry<QuestRegistries.ObjectiveEntry>          questObjectiveRegistry;
    private        IForgeRegistry<QuestRegistries.RewardEntry>             questRewardRegistry;
    private        IForgeRegistry<QuestRegistries.TriggerEntry>            questTriggerRegistry;
    private        IForgeRegistry<QuestRegistries.DialogueAnswerEntry>     questDialogueAnswerRegistry;
    // Happiness registries removed

    private EventBus eventBus = new DefaultEventBus();

    @Override
    @NotNull
    public IColonyManager getColonyManager()
    {
        return colonyManager;
    }

    @Override
    @NotNull
    public ICitizenDataManager getCitizenDataManager()
    {
        return citizenDataManager;
    }

    @Override
    @NotNull
    public IMobAIRegistry getMobAIRegistry()
    {
        return null; // Mob AI registry disabled for SlimColonies
    }

    @Override
    @NotNull
    public IPathNavigateRegistry getPathNavigateRegistry()
    {
        return pathNavigateRegistry;
    }

    @Override
    @NotNull
    public IBuildingDataManager getBuildingDataManager()
    {
        return buildingDataManager;
    }

    @Override
    @NotNull
    public IForgeRegistry<BuildingEntry> getBuildingRegistry()
    {
        return buildingRegistry;
    }

    @Override
    @NotNull
    public IForgeRegistry<BuildingExtensionEntry> getBuildingExtensionRegistry()
    {
        return buildingExtensionRegistry;
    }

    @Override
    public IJobDataManager getJobDataManager()
    {
        return jobDataManager;
    }

    @Override
    public IForgeRegistry<JobEntry> getJobRegistry()
    {
        return jobRegistry;
    }

    @Override
    public IForgeRegistry<InteractionResponseHandlerEntry> getInteractionResponseHandlerRegistry()
    {
        return interactionHandlerRegistry;
    }

    @Override
    public IGuardTypeDataManager getGuardTypeDataManager()
    {
        return guardTypeDataManager;
    }

    @Override
    public IForgeRegistry<GuardType> getGuardTypeRegistry()
    {
        return guardTypeRegistry;
    }

    @Override
    public IModelTypeRegistry getModelTypeRegistry()
    {
        return null;
    }

    @Override
    public Configuration getConfig()
    {
        return MineColonies.getConfig();
    }

    @Override
    public IFurnaceRecipes getFurnaceRecipes()
    {
        return FurnaceRecipes.getInstance();
    }

    @Override
    public IInteractionResponseHandlerDataManager getInteractionResponseHandlerDataManager()
    {
        return interactionDataManager;
    }

    @Override
    public IGlobalResearchTree getGlobalResearchTree()
    {
        return globalResearchTree;
    }

    @Override
    public IForgeRegistry<ModResearchRequirements.ResearchRequirementEntry> getResearchRequirementRegistry() {return researchRequirementRegistry;}

    @Override
    public IForgeRegistry<ModResearchEffects.ResearchEffectEntry> getResearchEffectRegistry() {return researchEffectRegistry;}

    @Override
    public IForgeRegistry<ResearchCostEntry> getResearchCostRegistry()
    {
        return researchCostRegistry;
    }

    @Override
    public void onRegistryNewRegistry(final NewRegistryEvent event)
    {
        event.create(new RegistryBuilder<EquipmentTypeEntry>()
                        .setName(new ResourceLocation(Constants.MOD_ID, "equipmenttypes"))
                        .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                        .disableSaving()
                        .allowModification()
                        .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> equipmentTypeRegistry = b);

        event.create(new RegistryBuilder<BuildingEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "buildings"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving()
                       .allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> buildingRegistry = b);

        event.create(new RegistryBuilder<BuildingExtensionEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "buildingextensions"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving()
                       .allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> buildingExtensionRegistry = b);

        event.create(new RegistryBuilder<JobEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "jobs"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving()
                       .allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> jobRegistry = b);

        event.create(new RegistryBuilder<GuardType>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "guardtypes"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving()
                       .allowModification()
                       .setDefaultKey(ModGuardTypes.KNIGHT_ID)
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> guardTypeRegistry = b);

        event.create(new RegistryBuilder<InteractionResponseHandlerEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "interactionresponsehandlers"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving()
                       .allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> interactionHandlerRegistry = b);

        event.create(new RegistryBuilder<ColonyEventTypeRegistryEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "colonyeventtypes"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> colonyEventRegistry = b);

        event.create(new RegistryBuilder<ColonyEventDescriptionTypeRegistryEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "colonyeventdesctypes"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> colonyEventDescriptionRegistry = b);


        event.create(new RegistryBuilder<CraftingType>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "craftingtypes"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> craftingTypeRegistry = b);

        event.create(new RegistryBuilder<RecipeTypeEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "recipetypeentries"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "classic"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> recipeTypeEntryRegistry = b);

        event.create(new RegistryBuilder<ModResearchRequirements.ResearchRequirementEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "researchrequirementtypes"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> researchRequirementRegistry = b);

        event.create(new RegistryBuilder<ModResearchEffects.ResearchEffectEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "researcheffecttypes"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> researchEffectRegistry = b);

        event.create(new RegistryBuilder<ResearchCostEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "researchcosttypes"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> researchCostRegistry = b);

        event.create(new RegistryBuilder<QuestRegistries.ObjectiveEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "questobjectives"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> questObjectiveRegistry = b);

        event.create(new RegistryBuilder<QuestRegistries.RewardEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "questrewards"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> questRewardRegistry = b);

        event.create(new RegistryBuilder<QuestRegistries.TriggerEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "questtriggers"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> questTriggerRegistry = b);

        event.create(new RegistryBuilder<QuestRegistries.DialogueAnswerEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "questanswerresults"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> questDialogueAnswerRegistry = b);

        // Happiness registries removed
    }

    @Override
    public IForgeRegistry<ColonyEventTypeRegistryEntry> getColonyEventRegistry()
    {
        return colonyEventRegistry;
    }

    @Override
    public IForgeRegistry<ColonyEventDescriptionTypeRegistryEntry> getColonyEventDescriptionRegistry()
    {
        return colonyEventDescriptionRegistry;
    }

    @Override
    public IForgeRegistry<RecipeTypeEntry> getRecipeTypeRegistry()
    {
        return recipeTypeEntryRegistry;
    }

    @Override
    public IForgeRegistry<CraftingType> getCraftingTypeRegistry()
    {
        return craftingTypeRegistry;
    }

    @Override
    public IForgeRegistry<QuestRegistries.RewardEntry> getQuestRewardRegistry()
    {
        return questRewardRegistry;
    }

    @Override
    public IForgeRegistry<QuestRegistries.ObjectiveEntry> getQuestObjectiveRegistry()
    {
        return questObjectiveRegistry;
    }

    @Override
    public IForgeRegistry<QuestRegistries.TriggerEntry> getQuestTriggerRegistry()
    {
        return questTriggerRegistry;
    }

    @Override
    public IForgeRegistry<QuestRegistries.DialogueAnswerEntry> getQuestDialogueAnswerRegistry()
    {
        return questDialogueAnswerRegistry;
    }

    // Happiness registry getters removed

    @Override
    public IForgeRegistry<EquipmentTypeEntry> getEquipmentTypeRegistry()
    {
        return equipmentTypeRegistry;
    }

    @Override
    public EventBus getEventBus()
    {
        return eventBus;
    }
}

