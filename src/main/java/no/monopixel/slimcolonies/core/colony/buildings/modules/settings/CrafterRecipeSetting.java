package no.monopixel.slimcolonies.core.colony.buildings.modules.settings;

import no.monopixel.slimcolonies.api.colony.buildings.modules.ISettingsModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static no.monopixel.slimcolonies.api.research.util.ResearchConstants.RECIPE_MODE;

/**
 * Stores the recipe setting for crafter workers.
 */
public class CrafterRecipeSetting extends StringSettingWithDesc
{
    /**
     * Reason display constants.
     */
    public static final String NEEDS_RESEARCH_REASON     = "no.monopixel.slimcolonies.coremod.settings.reason.needsresearch";
    public static final String WAREHOUSE_MASTER_RESEARCH = "no.monopixel.slimcolonies.research.technology.warehousemaster.name";

    /**
     * Different setting possibilities.
     */
    public static final String PRIORITY  = "no.monopixel.slimcolonies.core.crafting.setting.priority";
    public static final String MAX_STOCK = "no.monopixel.slimcolonies.core.crafting.setting.maxstock";

    /**
     * Create a new guard task list setting.
     */
    public CrafterRecipeSetting()
    {
        super(PRIORITY, MAX_STOCK);
    }

    /**
     * Create a new string list setting.
     *
     * @param settings     the overall list of settings.
     * @param currentIndex the current selected index.
     */
    public CrafterRecipeSetting(final List<String> settings, final int currentIndex)
    {
        super(settings, currentIndex);
    }

    @Override
    public boolean isActive(final ISettingsModule module)
    {
        return module.getBuilding().getColony().getResearchManager().getResearchEffects().getEffectStrength(RECIPE_MODE) > 0;
    }

    @Override
    @Nullable
    public Component getInactiveReason()
    {
        return Component.translatable(NEEDS_RESEARCH_REASON, Component.translatable(WAREHOUSE_MASTER_RESEARCH));
    }

    @Override
    public boolean isActive(final ISettingsModuleView module)
    {
        return module.getColony().getResearchManager().getResearchEffects().getEffectStrength(RECIPE_MODE) > 0;
    }
}
