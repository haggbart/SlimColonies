package no.monopixel.slimcolonies.apiimp.initializer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import no.monopixel.slimcolonies.api.entity.ModEntities;
import no.monopixel.slimcolonies.api.entity.other.MinecoloniesMinecart;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.entity.citizen.EntityCitizen;
import no.monopixel.slimcolonies.core.entity.other.CustomArrowEntity;
import no.monopixel.slimcolonies.core.entity.other.NewBobberEntity;
import no.monopixel.slimcolonies.core.entity.other.SittingEntity;
import no.monopixel.slimcolonies.core.entity.visitor.VisitorCitizen;
import org.jetbrains.annotations.Nullable;

import static no.monopixel.slimcolonies.api.util.constant.CitizenConstants.CITIZEN_HEIGHT;
import static no.monopixel.slimcolonies.api.util.constant.CitizenConstants.CITIZEN_WIDTH;
import static no.monopixel.slimcolonies.api.util.constant.Constants.*;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityInitializer
{
    public static void setupEntities(RegisterEvent event)
    {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.ENTITY_TYPES))
        {
            final @Nullable IForgeRegistry<EntityType<?>> registry = event.getForgeRegistry();

            ModEntities.CITIZEN = build(registry, "citizen",
                EntityType.Builder.of(EntityCitizen::new, MobCategory.CREATURE)
                    .setTrackingRange(ENTITY_TRACKING_RANGE)
                    .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                    .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                    .setShouldReceiveVelocityUpdates(true));

            ModEntities.FISHHOOK = build(registry, "fishhook",
                EntityType.Builder.<NewBobberEntity>of(NewBobberEntity::new, MobCategory.MISC)
                    .setTrackingRange(ENTITY_TRACKING_RANGE)
                    .setUpdateInterval(ENTITY_UPDATE_FREQUENCY_FISHHOOK)
                    .sized(0.25F, 0.25F)
                    .setShouldReceiveVelocityUpdates(true)
                    .setCustomClientFactory(NewBobberEntity::new));

            ModEntities.VISITOR = build(registry, "visitor", EntityType.Builder.of(VisitorCitizen::new, MobCategory.CREATURE)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                .setShouldReceiveVelocityUpdates(true));

            ModEntities.SITTINGENTITY = build(registry, "sittingentity",
                EntityType.Builder.<SittingEntity>of(SittingEntity::new, MobCategory.MISC)
                    .setTrackingRange(ENTITY_TRACKING_RANGE)
                    .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                    .sized(0F, 0.5F));

            ModEntities.MINECART = build(registry, "mcminecart",
                EntityType.Builder.of(MinecoloniesMinecart::new, MobCategory.MISC)
                    .setTrackingRange(ENTITY_TRACKING_RANGE)
                    .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                    .sized(0.98F, 0.7F));

            ModEntities.MC_NORMAL_ARROW = build(registry, "mcnormalarrow",
                EntityType.Builder.of(CustomArrowEntity::new, MobCategory.MISC)
                    .setTrackingRange(ENTITY_TRACKING_RANGE)
                    .setUpdateInterval(ENTITY_UPDATE_FREQUENCY_FISHHOOK)
                    .sized(0.5F, 0.5F)
                    .setShouldReceiveVelocityUpdates(true));
        }
    }

    private static <T extends Entity> EntityType<T> build(IForgeRegistry<EntityType<?>> registry, final String key, final EntityType.Builder<T> builder)
    {
        EntityType<T> entity = builder.build(Constants.MOD_ID + ":" + key);
        registry.register(new ResourceLocation(Constants.MOD_ID + ":" + key), entity);
        return entity;
    }

    @SubscribeEvent
    public static void registerEntities(final RegisterEvent event)
    {
        setupEntities(event);
    }
}
