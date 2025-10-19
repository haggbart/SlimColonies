package no.monopixel.slimcolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import no.monopixel.slimcolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import no.monopixel.slimcolonies.api.colony.buildings.modules.settings.ISetting;
import no.monopixel.slimcolonies.api.colony.buildings.modules.settings.ISettingKey;
import no.monopixel.slimcolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import no.monopixel.slimcolonies.api.colony.requestsystem.StandardFactoryController;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.client.gui.modules.SettingsModuleWindow;
import no.monopixel.slimcolonies.core.colony.buildings.modules.settings.SettingKey;
import no.monopixel.slimcolonies.core.network.messages.server.colony.building.TriggerSettingMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Module containing all settings (client side).
 */
public class SettingsModuleView extends AbstractBuildingModuleView implements ISettingsModuleView
{
    /**
     * Map of setting id (string) to generic setting.
     */
    final Map<ISettingKey<? extends ISetting>, ISetting> settings = new LinkedHashMap<>();

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        final Map<ISettingKey<?>, ISetting> tempSettings = new LinkedHashMap<>();
        final int size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            final ResourceLocation key = buf.readResourceLocation();
            final ISetting setting = StandardFactoryController.getInstance().deserialize(buf);
            if (setting != null)
            {
                final SettingKey<?> settingsKey = new SettingKey<>(setting.getClass(), key);
                tempSettings.put(settingsKey, setting);
                settings.putIfAbsent(settingsKey, setting);
            }
        }

        for (final Map.Entry<ISettingKey<? extends ISetting>, ISetting> entry : new ArrayList<>(settings.entrySet()))
        {
            final ISetting syncSetting = tempSettings.get(entry.getKey());
            if (syncSetting == null)
            {
                settings.remove(entry.getKey());
            }
            else if (entry.getValue() != syncSetting)
            {
                entry.getValue().updateSetting(syncSetting);
                entry.getValue().copyValue(syncSetting);
            }
        }
    }

    /**
     * Get the full settings map.
     *
     * @return the list of string key and ISetting value.
     */
    public List<ISettingKey<? extends ISetting>> getSettingsToShow()
    {
        List<ISettingKey<? extends ISetting>> filteredSettings = new ArrayList<>();
        for (Map.Entry<ISettingKey<? extends ISetting>, ISetting> setting : settings.entrySet())
        {
            if (setting.getValue().isActive(this) || !setting.getValue().shouldHideWhenInactive())
            {
                filteredSettings.add(setting.getKey());
            }
        }
        return filteredSettings;
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends ISetting> T getSetting(final ISettingKey<T> key)
    {
        return (T) settings.getOrDefault(key, null);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BOWindow getWindow()
    {
        return new SettingsModuleWindow(Constants.MOD_ID + ":gui/layouthuts/layoutsettings.xml", buildingView, this);
    }

    @Override
    public ResourceLocation getIconResourceLocation()
    {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/modules/settings.png");
    }

    @Override
    public String getDesc()
    {
        return "no.monopixel.slimcolonies.coremod.gui.workerhuts.settings";
    }

    @Override
    public void trigger(final ISettingKey<?> key)
    {
        final ISetting setting = settings.get(key);
        if (setting.isActive(this))
        {
            setting.trigger();
            Network.getNetwork().sendToServer(new TriggerSettingMessage(buildingView, key, setting, getProducer().getRuntimeID()));
        }
    }
}
