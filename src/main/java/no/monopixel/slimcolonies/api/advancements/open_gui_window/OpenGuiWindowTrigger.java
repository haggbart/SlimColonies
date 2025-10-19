package no.monopixel.slimcolonies.api.advancements.open_gui_window;

import com.google.gson.JsonObject;
import no.monopixel.slimcolonies.api.advancements.AbstractCriterionTrigger;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Triggered when a blockui window is opened
 */
public class OpenGuiWindowTrigger extends AbstractCriterionTrigger<OpenGuiWindowListeners, OpenGuiWindowCriterionInstance>
{
    public OpenGuiWindowTrigger()
    {
        super(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, Constants.CRITERION_OPEN_GUI_WINDOW), OpenGuiWindowListeners::new);
    }

    /**
     * Triggers the listener checks if there are any listening in
     * @param player the player the check regards
     * @param windowResource the window id that was just opened
     */
    public void trigger(final ServerPlayer player, final String windowResource)
    {
        final OpenGuiWindowListeners listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger(windowResource);
        }
    }

    @NotNull
    @Override
    public OpenGuiWindowCriterionInstance createInstance(@NotNull final JsonObject jsonObject, @NotNull final DeserializationContext conditionArrayParser)
    {
        return OpenGuiWindowCriterionInstance.deserializeFromJson(jsonObject, conditionArrayParser);
    }
}
