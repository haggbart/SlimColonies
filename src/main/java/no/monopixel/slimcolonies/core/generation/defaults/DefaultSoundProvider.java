package no.monopixel.slimcolonies.core.generation.defaults;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import no.monopixel.slimcolonies.api.colony.jobs.ModJobs;
import no.monopixel.slimcolonies.api.sounds.EventType;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static no.monopixel.slimcolonies.api.sounds.ModSoundEvents.CITIZEN_SOUND_EVENT_PREFIX;
import static no.monopixel.slimcolonies.core.generation.SoundsJson.createSoundJson;

public class DefaultSoundProvider implements DataProvider
{
    private final PackOutput packOutput;
    private JsonObject sounds;

    public DefaultSoundProvider(@NotNull final PackOutput packOutput)
    {
        this.packOutput = packOutput;
    }

    @Override
    @NotNull
    public CompletableFuture<?> run(@NotNull final CachedOutput cache)
    {
        sounds = new JsonObject();
        final File soundFolder = this.packOutput.getOutputFolder()
                                   .getParent()
                                   .getParent()
                                   .getParent()
                                   .resolve("main")
                                   .resolve("resources")
                                   .resolve("assets")
                                   .resolve(Constants.MOD_ID)
                                   .resolve("sounds")
                                   .resolve("mob")
                                   .resolve("citizen")
                                   .toFile();
        final List<ResourceLocation> mainTypes = new ArrayList<>(ModJobs.getJobs());
        mainTypes.remove(ModJobs.placeHolder.getId());
        mainTypes.add(new ResourceLocation(Constants.MOD_ID, "unemployed"));
        mainTypes.add(new ResourceLocation(Constants.MOD_ID, "visitor"));

        if (soundFolder.isDirectory())
        {
            final File[] list = soundFolder.listFiles();
            for (final File file : list)
            {
                final String name = file.getName();
                if (name.equals("child"))
                {
                    continue;
                }

                if (file.isDirectory())
                {
                    final List<String> soundList = new ArrayList<>();
                    final File[] subList = file.listFiles();

                    for (final File soundFile : subList)
                    {
                        final String soundName = soundFile.getName();
                        soundList.add(Constants.MOD_ID + ":mob/citizen/" + name + "/" + soundName.replace(".ogg", ""));
                    }

                    for (final ResourceLocation job : mainTypes)
                    {
                        for (final EventType event : EventType.values())
                        {
                            sounds.add(CITIZEN_SOUND_EVENT_PREFIX + job.getPath() + "." + name + "." + event.getId(),
                              createSoundJson("neutral", getDefaultProperties(), soundList));
                        }
                    }
                }
            }
        }

        final List<String> childSounds = new ArrayList<>();
        childSounds.add(Constants.MOD_ID + ":mob/citizen/child/laugh1");
        childSounds.add(Constants.MOD_ID + ":mob/citizen/child/laugh2");

        for (final EventType soundEvents : EventType.values())
        {
            sounds.add(CITIZEN_SOUND_EVENT_PREFIX + "child.male." + soundEvents.name().toLowerCase(Locale.US), createSoundJson("neutral", getDefaultProperties(), childSounds));
            sounds.add(CITIZEN_SOUND_EVENT_PREFIX + "child.female." + soundEvents.name().toLowerCase(Locale.US), createSoundJson("neutral", getDefaultProperties(), childSounds));
        }


        sounds.add("mob.citizen.snore", createSoundJson("neutral", getDefaultProperties(), ImmutableList.of(Constants.MOD_ID + ":mob/citizen/snore")));

        sounds.add("tile.sawmill.saw", createSoundJson("neutral", getDefaultProperties(), ImmutableList.of(Constants.MOD_ID + ":tile/sawmill/saw")));


        return DataProvider.saveStable(cache, sounds, getPath());
    }

    protected Path getPath()
    {
        return this.packOutput
                 .getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                 .resolve(Constants.MOD_ID)
                 .resolve("sounds.json");
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Default Sound Json Provider";
    }

    private JsonObject getDefaultProperties()
    {
        JsonObject properties = new JsonObject();
        properties.addProperty("stream", false);
        return properties;
    }
}
