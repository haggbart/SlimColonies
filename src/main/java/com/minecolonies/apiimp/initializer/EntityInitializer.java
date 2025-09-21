package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.entity.other.MinecoloniesMinecart;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.entity.visitor.VisitorCitizen;
import com.minecolonies.core.entity.mobs.EntityMercenary;
import com.minecolonies.core.entity.other.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.CitizenConstants.CITIZEN_HEIGHT;
import static com.minecolonies.api.util.constant.CitizenConstants.CITIZEN_WIDTH;
import static com.minecolonies.api.util.constant.Constants.*;

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

            ModEntities.MERCENARY = build(registry, "mercenary",
              EntityType.Builder.of(EntityMercenary::new, MobCategory.CREATURE)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));


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

            ModEntities.DRUID_POTION = build(registry, "druidpotion",
              EntityType.Builder.<DruidPotionEntity>of(DruidPotionEntity::new, MobCategory.MISC)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY_FISHHOOK)
                .sized(0.25F, 0.25F)
                .setShouldReceiveVelocityUpdates(true));


            ModEntities.SPEAR = build(registry, "spear",
              EntityType.Builder.<SpearEntity>of(SpearEntity::new, MobCategory.MISC)
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
