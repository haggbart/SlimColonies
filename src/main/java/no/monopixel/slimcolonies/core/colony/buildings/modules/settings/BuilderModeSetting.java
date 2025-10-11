package no.monopixel.slimcolonies.core.colony.buildings.modules.settings;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.placement.StructureIterators;
import net.minecraft.network.chat.Component;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Stores the builder mode setting.
 */
public class BuilderModeSetting extends StringSetting
{
    /**
     * Create the builder mode setting.
     */
    public BuilderModeSetting()
    {
        super(StructureIterators.getKeySet().stream().sorted(String::compareToIgnoreCase).toList(), 0);
        set(Structurize.getConfig().getServer().iteratorType.get());
    }

    /**
     * Create the builder mode setting.
     *
     * @param value the list of possible settings.
     * @param curr  the current setting.
     */
    public BuilderModeSetting(final List<String> value, final int curr)
    {
        super(StructureIterators.getKeySet().stream().sorted(String::compareToIgnoreCase).toList(), 0);
        set(value.get(curr));
    }

    @NotNull
    public static String getActualValue(@NotNull final IBuilding building)
    {
        return building.getSettingValueOrDefault(BuildingBuilder.BUILDING_MODE, Structurize.getConfig().getServer().iteratorType.get());
    }

    @Override
    protected Component getDisplayText()
    {
        return Component.translatable("com.ldtteam.structurize.iterators." + getSettings().get(getCurrentIndex()));
    }

    @Override
    public Component getToolTipText()
    {
        return Component.translatable("com.ldtteam.structurize.iterators." + getSettings().get(getCurrentIndex()) + ".tooltip");
    }
}
