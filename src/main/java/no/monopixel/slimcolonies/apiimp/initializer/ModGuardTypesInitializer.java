package no.monopixel.slimcolonies.apiimp.initializer;

import no.monopixel.slimcolonies.api.colony.guardtype.GuardType;
import no.monopixel.slimcolonies.api.colony.guardtype.registry.ModGuardTypes;
import no.monopixel.slimcolonies.api.colony.jobs.ModJobs;
import no.monopixel.slimcolonies.api.entity.citizen.Skill;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.colony.jobs.JobKnight;
import no.monopixel.slimcolonies.core.colony.jobs.JobRanger;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

import static no.monopixel.slimcolonies.api.util.constant.translation.JobTranslationConstants.*;

public final class ModGuardTypesInitializer
{
    public final static DeferredRegister<GuardType> DEFERRED_REGISTER = DeferredRegister.create(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "guardtypes"), Constants.MOD_ID);

    private ModGuardTypesInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModGuardTypesInitializer but this is a Utility class.");
    }

    static
    {
        ModGuardTypes.knight = DEFERRED_REGISTER.register(ModGuardTypes.KNIGHT_ID.getPath(), () -> new GuardType.Builder()
                                 .setJobTranslationKey(JOB_KNIGHT)
                                 .setButtonTranslationKey(JOB_KNIGHT_BUTTON)
                                 .setPrimarySkill(Skill.Adaptability)
                                 .setSecondarySkill(Skill.Stamina)
                                 .setWorkerSoundName("archer")
                                 .setJobEntry(() -> ModJobs.knight.get())
                                 .setRegistryName(ModGuardTypes.KNIGHT_ID)
                                 .setClazz(JobKnight.class)
                                 .createGuardType());

        ModGuardTypes.ranger = DEFERRED_REGISTER.register(ModGuardTypes.RANGER_ID.getPath(), () -> new GuardType.Builder()
                                 .setJobTranslationKey(JOB_RANGER)
                                 .setButtonTranslationKey(JOB_RANGER_BUTTON)
                                 .setPrimarySkill(Skill.Agility)
                                 .setSecondarySkill(Skill.Adaptability)
                                 .setWorkerSoundName("archer")
                                 .setJobEntry(() -> ModJobs.archer.get())
                                 .setRegistryName(ModGuardTypes.RANGER_ID)
                                 .setClazz(JobRanger.class)
                                 .createGuardType());

    }
}
