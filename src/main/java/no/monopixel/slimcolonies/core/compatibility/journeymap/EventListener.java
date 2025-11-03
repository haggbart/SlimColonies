package no.monopixel.slimcolonies.core.compatibility.journeymap;

import journeymap.client.api.display.Context;
import journeymap.client.api.event.forge.EntityRadarUpdateEvent;
import journeymap.client.api.model.WrappedEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import no.monopixel.slimcolonies.api.ISlimColoniesAPI;
import no.monopixel.slimcolonies.api.colony.IColonyView;
import no.monopixel.slimcolonies.api.colony.jobs.ModJobs;
import no.monopixel.slimcolonies.api.colony.jobs.registry.IJobRegistry;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.api.eventbus.events.colony.ColonyViewUpdatedModEvent;
import no.monopixel.slimcolonies.core.entity.visitor.VisitorCitizen;
import no.monopixel.slimcolonies.core.event.ClientChunkUpdatedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;

import static no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen.DATA_JOB;
import static no.monopixel.slimcolonies.api.util.constant.Constants.MOD_ID;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.PARTIAL_JOURNEY_MAP_INFO;

public class EventListener
{
    private static final Style JOB_TOOLTIP = Style.EMPTY.withColor(ChatFormatting.YELLOW).withItalic(true);

    /**
     * Set of guard job ResourceLocations for fast type checking without instantiation.
     */
    private static final Set<ResourceLocation> GUARD_JOB_KEYS = Set.of(
        ModJobs.ARCHER_ID,
        ModJobs.KNIGHT_ID
    );

    @NotNull
    private final Journeymap jmap;

    /**
     * Shortens a full name to "FirstName LastInitial." format.
     * Examples: "Johan Nygård" -> "Johan N.", "Bob" -> "Bob"
     */
    private static String shortenName(@NotNull final String fullName)
    {
        final String[] nameParts = fullName.trim().split("\\s+", 2);
        if (nameParts.length < 2 || nameParts[1].isEmpty())
        {
            return fullName;
        }
        return nameParts[0] + " " + nameParts[1].charAt(0) + ".";
    }

    public EventListener(@NotNull final Journeymap jmap)
    {
        this.jmap = jmap;

        MinecraftForge.EVENT_BUS.register(this);
        ISlimColoniesAPI.getInstance().getEventBus().subscribe(ColonyViewUpdatedModEvent.class, this::onColonyViewUpdated);
    }

    @SubscribeEvent
    public void onPlayerLogout(@NotNull final ClientPlayerNetworkEvent.LoggingOut event)
    {
        ColonyDeathpoints.clear();
        this.jmap.getApi().removeAll(MOD_ID);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onChunkLoaded(@NotNull final ChunkEvent.Load event)
    {
        if (!event.getLevel().isClientSide())
        {
            return;
        }

        if (event.getLevel() instanceof Level)
        {
            final ResourceKey<Level> dimension = ((Level) event.getLevel()).dimension();

            ColonyDeathpoints.updateChunk(this.jmap, dimension, event.getChunk());
        }
    }

    @SubscribeEvent
    public void onColonyChunkDataUpdated(@NotNull final ClientChunkUpdatedEvent event)
    {
        final ResourceKey<Level> dimension = event.getChunk().getLevel().dimension();

        ColonyBorderMapping.updateChunk(this.jmap, dimension, event.getChunk());
    }

    public void onColonyViewUpdated(@NotNull final ColonyViewUpdatedModEvent event)
    {
        final IColonyView colony = event.getColony();
        final Set<BlockPos> graves = colony.getGraveManager().getGraves().keySet();

        ColonyDeathpoints.updateGraves(this.jmap, colony, graves);
    }

    @SubscribeEvent
    public void onUpdateEntityRadar(@NotNull final EntityRadarUpdateEvent event)
    {
        final WrappedEntity wrapper = event.getWrappedEntity();
        final LivingEntity entity = wrapper.getEntityLivingRef().get();

        if (entity instanceof AbstractEntityCitizen)
        {
            // Cache options lookup to avoid multiple Optional lookups
            final java.util.Optional<JourneymapOptions> options = this.jmap.getOptions();

            final boolean isVisitor = entity instanceof VisitorCitizen;
            MutableComponent jobName;

            if (isVisitor)
            {
                if (!JourneymapOptions.getShowVisitors(options))
                {
                    wrapper.setDisable(true);
                    return;
                }

                jobName = Component.translatable(PARTIAL_JOURNEY_MAP_INFO + "visitor");
            }
            else
            {
                final String jobId = entity.getEntityData().get(DATA_JOB);
                final JobEntry jobEntry = IJobRegistry.getInstance().getValue(ResourceLocation.parse(jobId));

                final boolean isGuard = jobEntry != null && GUARD_JOB_KEYS.contains(jobEntry.getKey());

                if (isGuard
                    ? !JourneymapOptions.getShowGuards(options)
                    : !JourneymapOptions.getShowCitizens(options))
                {
                    wrapper.setDisable(true);
                    return;
                }

                jobName = Component.translatable(jobEntry == null
                    ? PARTIAL_JOURNEY_MAP_INFO + "unemployed"
                    : jobEntry.getTranslationKey());
            }

            // Cache customName to avoid multiple lookups
            final Component fullName = entity.getCustomName();

            if (JourneymapOptions.getShowColonistTooltip(options))
            {
                if (fullName != null)
                {
                    wrapper.setEntityToolTips(Arrays.asList(fullName, jobName.setStyle(JOB_TOOLTIP)));
                }
            }

            final boolean isMinimap = event.getActiveUiState().ui.equals(Context.UI.Minimap);
            final boolean showName = isMinimap
                ? JourneymapOptions.getShowColonistNameMinimap(options)
                : JourneymapOptions.getShowColonistNameFullscreen(options);

            if (!showName)
            {
                wrapper.setCustomName("");
            }
            else if (fullName != null)
            {
                wrapper.setCustomName(shortenName(fullName.getString()));
            }

            if (!isVisitor && JourneymapOptions.getShowColonistTeamColour(options))
            {
                wrapper.setColor(entity.getTeamColor());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onClientTick(@NotNull final TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END)
        {
            return;
        }

        final Level world = Minecraft.getInstance().level;
        if (world != null)
        {
            ColonyBorderMapping.updatePending(this.jmap, world.dimension());
        }
    }
}
