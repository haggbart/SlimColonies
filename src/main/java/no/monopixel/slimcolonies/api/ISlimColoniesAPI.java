package no.monopixel.slimcolonies.api;

import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import no.monopixel.slimcolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import no.monopixel.slimcolonies.api.colony.ICitizenDataManager;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries.BuildingExtensionEntry;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import no.monopixel.slimcolonies.api.colony.buildings.registry.IBuildingDataManager;
import no.monopixel.slimcolonies.api.colony.colonyEvents.registry.ColonyEventDescriptionTypeRegistryEntry;
import no.monopixel.slimcolonies.api.colony.colonyEvents.registry.ColonyEventTypeRegistryEntry;
import no.monopixel.slimcolonies.api.colony.guardtype.GuardType;
import no.monopixel.slimcolonies.api.colony.guardtype.registry.IGuardTypeDataManager;
import no.monopixel.slimcolonies.api.colony.interactionhandling.registry.IInteractionResponseHandlerDataManager;
import no.monopixel.slimcolonies.api.colony.interactionhandling.registry.InteractionResponseHandlerEntry;
import no.monopixel.slimcolonies.api.colony.jobs.registry.IJobDataManager;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.compatibility.IFurnaceRecipes;
import no.monopixel.slimcolonies.api.configuration.Configuration;
import no.monopixel.slimcolonies.api.crafting.registry.CraftingType;
import no.monopixel.slimcolonies.api.crafting.registry.RecipeTypeEntry;
import no.monopixel.slimcolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import no.monopixel.slimcolonies.api.equipment.registry.EquipmentTypeEntry;
import no.monopixel.slimcolonies.api.eventbus.EventBus;
import no.monopixel.slimcolonies.api.quests.registries.QuestRegistries;
import no.monopixel.slimcolonies.api.research.IGlobalResearchTree;
import no.monopixel.slimcolonies.api.research.ModResearchEffects;
import no.monopixel.slimcolonies.api.research.ModResearchRequirements;

public interface ISlimColoniesAPI
{

    static ISlimColoniesAPI getInstance()
    {
        return SlimColoniesAPIProxy.getInstance();
    }

    IColonyManager getColonyManager();

    ICitizenDataManager getCitizenDataManager();

    IPathNavigateRegistry getPathNavigateRegistry();

    IBuildingDataManager getBuildingDataManager();

    IForgeRegistry<BuildingEntry> getBuildingRegistry();

    IForgeRegistry<BuildingExtensionEntry> getBuildingExtensionRegistry();

    IJobDataManager getJobDataManager();

    IForgeRegistry<JobEntry> getJobRegistry();

    IForgeRegistry<InteractionResponseHandlerEntry> getInteractionResponseHandlerRegistry();

    IGuardTypeDataManager getGuardTypeDataManager();

    IForgeRegistry<GuardType> getGuardTypeRegistry();

    IModelTypeRegistry getModelTypeRegistry();

    Configuration getConfig();

    IFurnaceRecipes getFurnaceRecipes();

    IInteractionResponseHandlerDataManager getInteractionResponseHandlerDataManager();

    IGlobalResearchTree getGlobalResearchTree();

    IForgeRegistry<ModResearchRequirements.ResearchRequirementEntry> getResearchRequirementRegistry();

    IForgeRegistry<ModResearchEffects.ResearchEffectEntry> getResearchEffectRegistry();

    // Research cost registry removed - no longer used

    IForgeRegistry<ColonyEventTypeRegistryEntry> getColonyEventRegistry();

    IForgeRegistry<ColonyEventDescriptionTypeRegistryEntry> getColonyEventDescriptionRegistry();

    IForgeRegistry<RecipeTypeEntry> getRecipeTypeRegistry();

    IForgeRegistry<CraftingType> getCraftingTypeRegistry();

    IForgeRegistry<QuestRegistries.RewardEntry> getQuestRewardRegistry();

    IForgeRegistry<QuestRegistries.ObjectiveEntry> getQuestObjectiveRegistry();

    IForgeRegistry<QuestRegistries.TriggerEntry> getQuestTriggerRegistry();

    IForgeRegistry<QuestRegistries.DialogueAnswerEntry> getQuestDialogueAnswerRegistry();

    // Happiness registry methods removed

    void onRegistryNewRegistry(NewRegistryEvent event);

    IForgeRegistry<EquipmentTypeEntry> getEquipmentTypeRegistry();

    EventBus getEventBus();
}
