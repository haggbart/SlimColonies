package no.monopixel.slimcolonies.core.generation.defaults;

import no.monopixel.slimcolonies.api.entity.ModEntities;
import no.monopixel.slimcolonies.api.util.DamageSourceKeys;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.JsonCodecProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static no.monopixel.slimcolonies.api.util.constant.Constants.MOD_ID;

public class DefaultDamageTypeProvider extends JsonCodecProvider<DamageType>
{
    public DefaultDamageTypeProvider(@NotNull final PackOutput packOutput,
                                     @NotNull final ExistingFileHelper existingFileHelper)
    {
        super(packOutput, existingFileHelper, MOD_ID, JsonOps.INSTANCE, PackType.SERVER_DATA, "damage_type", DamageType.CODEC, getDamageTypes());
    }

    private static Map<ResourceLocation, DamageType> getDamageTypes()
    {
        return Map.ofEntries(
                Map.entry(DamageSourceKeys.CONSOLE.location(), damage("console")),
                Map.entry(DamageSourceKeys.DEFAULT.location(), damage("default")),
                Map.entry(DamageSourceKeys.DESPAWN.location(), damage("despawn")),
                Map.entry(DamageSourceKeys.NETHER.location(), damage("nether")),

                Map.entry(DamageSourceKeys.GUARD.location(), damage("entity.slimcolonies.guard")),
                Map.entry(DamageSourceKeys.GUARD_PVP.location(), damage("entity.slimcolonies.guardpvp")),
                Map.entry(DamageSourceKeys.SLAP.location(), damage("entity.slimcolonies.slap")),
                Map.entry(DamageSourceKeys.STUCK_DAMAGE.location(), damage("entity.slimcolonies.stuckdamage")),
                Map.entry(DamageSourceKeys.TRAINING.location(), damage("entity.slimcolonies.training")),
                Map.entry(DamageSourceKeys.WAKEY.location(), damage("entity.slimcolonies.wakeywakey")),

                Map.entry(DamageSourceKeys.VISITOR.location(), entityDamage(ModEntities.VISITOR))
          );
    }

    @NotNull
    private static DamageType entityDamage(@NotNull final EntityType<?> entityType)
    {
        return damage(entityType.getDescriptionId());
    }

    @NotNull
    private static DamageType damage(@NotNull final String msgId)
    {
        return new DamageType(msgId, DamageScaling.ALWAYS, 0.1F);
    }
}
