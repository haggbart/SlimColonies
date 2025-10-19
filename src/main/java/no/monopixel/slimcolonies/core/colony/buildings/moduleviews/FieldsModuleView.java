package no.monopixel.slimcolonies.core.colony.buildings.moduleviews;

import no.monopixel.slimcolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.api.colony.buildingextensions.IBuildingExtension;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.network.messages.server.colony.building.fields.AssignFieldMessage;
import no.monopixel.slimcolonies.core.network.messages.server.colony.building.fields.AssignmentModeMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.monopixel.slimcolonies.api.util.constant.translation.GuiTranslationConstants.BUILDING_TAB_FIELDS;
import static no.monopixel.slimcolonies.api.util.constant.translation.GuiTranslationConstants.FIELD_LIST_WARN_EXCEEDS_FIELD_COUNT;

/**
 * Client side version of the abstract class to list all fields (assigned) to a building.
 */
public abstract class FieldsModuleView extends AbstractBuildingModuleView
{
    /**
     * Checks if fields should be assigned manually.
     */
    private boolean shouldAssignFieldManually;

    /**
     * The maximum amount of fields the building can support.
     */
    private int maxFieldCount;

    /**
     * Cooldown data: field ID -> timestamp when field was last reset.
     */
    private final Map<IBuildingExtension.ExtensionId, Long> fieldCooldowns = new HashMap<>();

    /**
     * Current game time (in ticks) from the server.
     */
    private long currentGameTime;

    /**
     * Cooldown duration in minutes (from server config).
     */
    private int cooldownMinutes;

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        shouldAssignFieldManually = buf.readBoolean();
        maxFieldCount = buf.readInt();

        // Deserialize cooldown data
        fieldCooldowns.clear();
        final int cooldownCount = buf.readInt();
        for (int i = 0; i < cooldownCount; i++)
        {
            final IBuildingExtension.ExtensionId id = IBuildingExtension.ExtensionId.deserializeNBT(buf.readNbt());
            final long timestamp = buf.readLong();
            fieldCooldowns.put(id, timestamp);
        }

        // Read game time and config
        currentGameTime = buf.readLong();
        cooldownMinutes = buf.readInt();
    }

    @Override
    public ResourceLocation getIconResourceLocation()
    {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/modules/field.png");
    }

    @Override
    public String getDesc()
    {
        return BUILDING_TAB_FIELDS;
    }

    /**
     * Should the citizen be assigned manually to the fields.
     *
     * @return true if yes.
     */
    public boolean assignFieldManually()
    {
        return shouldAssignFieldManually;
    }

    /**
     * Sets the assignedFieldManually in the view.
     *
     * @param assignFieldManually variable to set.
     */
    public void setAssignFieldManually(final boolean assignFieldManually)
    {
        this.shouldAssignFieldManually = assignFieldManually;
        Network.getNetwork().sendToServer(new AssignmentModeMessage(buildingView, assignFieldManually, getProducer().getRuntimeID()));
    }

    /**
     * Assign a given field to the current worker.
     *
     * @param field the field to assign.
     */
    public void assignField(final IBuildingExtension field)
    {
        if (buildingView != null && canAssignField(field))
        {
            Network.getNetwork().sendToServer(new AssignFieldMessage(buildingView, field, true, getProducer().getRuntimeID()));

            final WorkerBuildingModuleView buildingModuleView = buildingView.getModuleViewMatching(WorkerBuildingModuleView.class, view -> true);
            if (buildingModuleView != null)
            {
                field.setBuilding(buildingView.getID());
            }
        }
    }

    /**
     * Check to see if a new field can be assigned to the worker.
     *
     * @param field the field which is being added.
     * @return true if so.
     */
    public final boolean canAssignField(IBuildingExtension field)
    {
        return getOwnedFields().size() < maxFieldCount && canAssignFieldOverride(field);
    }

    /**
     * Getter of all owned fields.
     *
     * @return an unmodifiable list.
     */
    @NotNull
    public List<IBuildingExtension> getOwnedFields()
    {
        return getFields().stream()
                 .filter(field -> buildingView.getID().equals(field.getBuildingId()))
                 .distinct()
                 .sorted(Comparator.comparingInt(f -> f.getSqDistance(buildingView)))
                 .toList();
    }

    /**
     * Additional checks to see if this field can be assigned to the building.
     *
     * @param field the field which is being added.
     * @return true if so.
     */
    protected abstract boolean canAssignFieldOverride(IBuildingExtension field);

    /**
     * Getter of all fields that are either free, or taken by the current building.
     *
     * @return an unmodifiable list.
     */
    @NotNull
    public List<IBuildingExtension> getFields()
    {
        return getFieldsInColony().stream()
                 .filter(field -> !field.isTaken() || buildingView.getID().equals(field.getBuildingId()))
                 .distinct()
                 .sorted(Comparator.comparingInt(f -> f.getSqDistance(buildingView)))
                 .toList();
    }

    /**
     * Obtains the list of fields from the colony.
     *
     * @return the list of field instances.
     */
    protected abstract List<IBuildingExtension> getFieldsInColony();

    /**
     * Free a field from the current worker.
     *
     * @param field the field to free.
     */
    public void freeField(final IBuildingExtension field)
    {
        if (buildingView != null)
        {
            Network.getNetwork().sendToServer(new AssignFieldMessage(buildingView, field, false, getProducer().getRuntimeID()));

            final WorkerBuildingModuleView buildingModuleView = buildingView.getModuleViewMatching(WorkerBuildingModuleView.class, view -> true);
            if (buildingModuleView != null)
            {
                field.resetOwningBuilding();
            }
        }
    }

    /**
     * Get a warning text component for the specific field whenever this field cannot be assigned for any reason.
     *
     * @param field the field in question.
     * @return a text component that should be shown if there is a problem for the specific field, else null.
     */
    @Nullable
    public MutableComponent getFieldWarningTooltip(IBuildingExtension field)
    {
        if (getOwnedFields().size() >= maxFieldCount)
        {
            return Component.translatable(FIELD_LIST_WARN_EXCEEDS_FIELD_COUNT);
        }
        return null;
    }

    /**
     * Get the maximum allowed field count.
     *
     * @return the max field count.
     */
    public int getMaxFieldCount()
    {
        return maxFieldCount;
    }

    /**
     * Check if a field is currently on cooldown.
     *
     * @param field the field to check
     * @return true if the field is on cooldown (resting), false if ready to work
     */
    public boolean isFieldOnCooldown(final IBuildingExtension field)
    {
        if (!fieldCooldowns.containsKey(field.getId()))
        {
            return false; // Never worked - not on cooldown
        }

        final long resetTime = fieldCooldowns.get(field.getId());
        final long cooldownTicks = cooldownMinutes * 60L * 20L;

        // Use client-side game time for real-time accuracy
        final long clientGameTime = net.minecraft.client.Minecraft.getInstance().level.getGameTime();

        return (clientGameTime - resetTime) < cooldownTicks;
    }

    /**
     * Get the remaining cooldown time for a field in seconds.
     *
     * @param field the field to check
     * @return remaining cooldown time in seconds, or 0 if not on cooldown
     */
    public int getRemainingCooldownSeconds(final IBuildingExtension field)
    {
        if (!fieldCooldowns.containsKey(field.getId()))
        {
            return 0;
        }

        final long resetTime = fieldCooldowns.get(field.getId());
        final long cooldownTicks = cooldownMinutes * 60L * 20L;

        // Use client-side game time for real-time accuracy
        final long clientGameTime = net.minecraft.client.Minecraft.getInstance().level.getGameTime();
        final long elapsedTicks = clientGameTime - resetTime;
        final long remainingTicks = cooldownTicks - elapsedTicks;

        if (remainingTicks <= 0)
        {
            return 0;
        }

        // Convert ticks to seconds (20 ticks = 1 second)
        return (int) (remainingTicks / 20L);
    }
}
