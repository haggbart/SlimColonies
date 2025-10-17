package no.monopixel.slimcolonies.core.colony.buildings.modules.settings;

import java.util.List;

/**
 * Stores the recipe setting for crafter workers.
 */
public class CrafterRecipeSetting extends StringSettingWithDesc
{
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
}
