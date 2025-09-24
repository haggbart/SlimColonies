package no.monopixel.slimcolonies.api;

import no.monopixel.slimcolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import no.monopixel.slimcolonies.api.colony.ICitizenDataManager;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import no.monopixel.slimcolonies.api.colony.buildings.registry.IBuildingDataManager;
import no.monopixel.slimcolonies.api.colony.colonyEvents.registry.ColonyEventDescriptionTypeRegistryEntry;
import no.monopixel.slimcolonies.api.colony.colonyEvents.registry.ColonyEventTypeRegistryEntry;
import no.monopixel.slimcolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries.BuildingExtensionEntry;
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
// Happiness imports removed
import no.monopixel.slimcolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import no.monopixel.slimcolonies.api.equipment.registry.EquipmentTypeEntry;
import no.monopixel.slimcolonies.api.eventbus.EventBus;
import no.monopixel.slimcolonies.api.quests.registries.QuestRegistries;
import no.monopixel.slimcolonies.api.research.IGlobalResearchTree;
import no.monopixel.slimcolonies.api.research.ModResearchEffects;
import no.monopixel.slimcolonies.api.research.ModResearchRequirements;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;

public final class MinecoloniesAPIProxy implements IMinecoloniesAPI
{
    private static final MinecoloniesAPIProxy ourInstance = new MinecoloniesAPIProxy();

    private IMinecoloniesAPI apiInstance;

    public static MinecoloniesAPIProxy getInstance()
    {
        return ourInstance;
    }

    private MinecoloniesAPIProxy()
    {
    }

    public void setApiInstance(final IMinecoloniesAPI apiInstance)
    {
        this.apiInstance = apiInstance;
    }

    @Override
    public IColonyManager getColonyManager()
    {
        return apiInstance.getColonyManager();
    }

    @Override
    public ICitizenDataManager getCitizenDataManager()
    {
        return apiInstance.getCitizenDataManager();
    }


    @Override
    public IPathNavigateRegistry getPathNavigateRegistry()
    {
        return apiInstance.getPathNavigateRegistry();
    }

    @Override
    public IBuildingDataManager getBuildingDataManager()
    {
        return apiInstance.getBuildingDataManager();
    }

    @Override
    public IForgeRegistry<BuildingEntry> getBuildingRegistry()
    {
        return apiInstance.getBuildingRegistry();
    }

    @Override
    public IForgeRegistry<BuildingExtensionEntry> getBuildingExtensionRegistry()
    {
        return apiInstance.getBuildingExtensionRegistry();
    }

    @Override
    public IJobDataManager getJobDataManager()
    {
        return apiInstance.getJobDataManager();
    }

    @Override
    public IForgeRegistry<JobEntry> getJobRegistry()
    {
        return apiInstance.getJobRegistry();
    }

    @Override
    public IForgeRegistry<InteractionResponseHandlerEntry> getInteractionResponseHandlerRegistry()
    {
        return apiInstance.getInteractionResponseHandlerRegistry();
    }

    @Override
    public IGuardTypeDataManager getGuardTypeDataManager()
    {
        return apiInstance.getGuardTypeDataManager();
    }

    @Override
    public IForgeRegistry<GuardType> getGuardTypeRegistry()
    {
        return apiInstance.getGuardTypeRegistry();
    }

    @Override
    public IModelTypeRegistry getModelTypeRegistry()
    {
        return apiInstance.getModelTypeRegistry();
    }

    @Override
    public Configuration getConfig()
    {
        return apiInstance.getConfig();
    }

    @Override
    public IFurnaceRecipes getFurnaceRecipes()
    {
        return apiInstance.getFurnaceRecipes();
    }

    @Override
    public IInteractionResponseHandlerDataManager getInteractionResponseHandlerDataManager()
    {
        return apiInstance.getInteractionResponseHandlerDataManager();
    }

    @Override
    public IGlobalResearchTree getGlobalResearchTree()
    {
        return apiInstance.getGlobalResearchTree();
    }

    @Override
    public IForgeRegistry<ModResearchRequirements.ResearchRequirementEntry> getResearchRequirementRegistry() {return apiInstance.getResearchRequirementRegistry();}

    @Override
    public IForgeRegistry<ModResearchEffects.ResearchEffectEntry> getResearchEffectRegistry() {return apiInstance.getResearchEffectRegistry();}

    // Research cost registry removed - no longer used

    @Override
    public IForgeRegistry<ColonyEventTypeRegistryEntry> getColonyEventRegistry()
    {
        return apiInstance.getColonyEventRegistry();
    }

    @Override
    public IForgeRegistry<ColonyEventDescriptionTypeRegistryEntry> getColonyEventDescriptionRegistry()
    {
        return apiInstance.getColonyEventDescriptionRegistry();
    }

    @Override
    public IForgeRegistry<RecipeTypeEntry> getRecipeTypeRegistry()
    {
        return apiInstance.getRecipeTypeRegistry();
    }

    @Override
    public IForgeRegistry<CraftingType> getCraftingTypeRegistry()
    {
        return apiInstance.getCraftingTypeRegistry();
    }

    @Override
    public IForgeRegistry<QuestRegistries.RewardEntry> getQuestRewardRegistry()
    {
        return apiInstance.getQuestRewardRegistry();
    }

    @Override
    public IForgeRegistry<QuestRegistries.ObjectiveEntry> getQuestObjectiveRegistry()
    {
        return apiInstance.getQuestObjectiveRegistry();
    }

    @Override
    public IForgeRegistry<QuestRegistries.TriggerEntry> getQuestTriggerRegistry()
    {
        return apiInstance.getQuestTriggerRegistry();
    }

    @Override
    public IForgeRegistry<QuestRegistries.DialogueAnswerEntry> getQuestDialogueAnswerRegistry()
    {
        return apiInstance.getQuestDialogueAnswerRegistry();
    }

    // Happiness registry methods removed

    @Override
    public void onRegistryNewRegistry(final NewRegistryEvent event)
    {
        apiInstance.onRegistryNewRegistry(event);
    }

    @Override
    public IForgeRegistry<EquipmentTypeEntry> getEquipmentTypeRegistry()
    {
        return apiInstance.getEquipmentTypeRegistry();
    }

    @Override
    public EventBus getEventBus()
    {
        return apiInstance.getEventBus();
    }
}
