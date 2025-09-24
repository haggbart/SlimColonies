package no.monopixel.slimcolonies.apiimp.initializer;

import no.monopixel.slimcolonies.api.research.ModResearchRequirements;
import no.monopixel.slimcolonies.api.research.requirements.BuildingAlternatesResearchRequirement;
import no.monopixel.slimcolonies.api.research.requirements.BuildingResearchRequirement;
import no.monopixel.slimcolonies.api.research.requirements.ResearchResearchRequirement;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static no.monopixel.slimcolonies.api.research.ModResearchRequirements.*;

/**
 * Registry initializer for the {@link ModResearchRequirements}.
 */
public class ModResearchRequirementInitializer
{
    public final static DeferredRegister<ResearchRequirementEntry> DEFERRED_REGISTER =
        DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "researchrequirementtypes"), Constants.MOD_ID);
    static
    {
        buildingResearchRequirement = create(BUILDING_RESEARCH_REQ_ID, BuildingResearchRequirement::new, BuildingResearchRequirement::new);
        buildingAlternatesResearchRequirement = create(BUILDING_ALTERNATES_RESEARCH_REQ_ID, BuildingAlternatesResearchRequirement::new, BuildingAlternatesResearchRequirement::new);
        buildingSingleResearchRequirement = create(BUILDING_SINGLE_RESEARCH_REQ_ID, BuildingResearchRequirement::new, BuildingResearchRequirement::new);

        researchResearchRequirement = create(RESEARCH_RESEARCH_REQ_ID, ResearchResearchRequirement::new, ResearchResearchRequirement::new);
    }
    private ModResearchRequirementInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModResearchRequirementInitializer but this is a Utility class.");
    }

    /**
     * Utility method to aid in the creation of a research requirement.
     *
     * @param registryName the registry name for this entry.
     * @param readFromNBT  function to read this item from json.
     * @param readFromJson function to read this item from NBT.
     * @return the finalized registry object.
     */
    private static RegistryObject<ResearchRequirementEntry> create(
        final ResourceLocation registryName,
        final ReadFromNBTFunction readFromNBT,
        final ReadFromJsonFunction readFromJson)
    {
        return DEFERRED_REGISTER.register(registryName.getPath(), () -> new ResearchRequirementEntry(registryName, readFromNBT, readFromJson));
    }
}
