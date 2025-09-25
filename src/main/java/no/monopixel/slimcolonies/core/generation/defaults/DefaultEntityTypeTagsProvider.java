package no.monopixel.slimcolonies.core.generation.defaults;

import no.monopixel.slimcolonies.api.items.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static no.monopixel.slimcolonies.api.util.constant.Constants.MOD_ID;

public class DefaultEntityTypeTagsProvider extends EntityTypeTagsProvider
{
    public DefaultEntityTypeTagsProvider(final PackOutput output,
      final CompletableFuture<HolderLookup.Provider> lookupProvider,
      @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(final HolderLookup.Provider holder)
    {
        tag(ModTags.hostile).add(EntityType.SLIME);
        tag(ModTags.mobAttackBlacklist).add(EntityType.ENDERMAN, EntityType.LLAMA);
        tag(ModTags.freeToInteractWith).addOptional(new ResourceLocation("corpse", "corpse"));

    }
}
