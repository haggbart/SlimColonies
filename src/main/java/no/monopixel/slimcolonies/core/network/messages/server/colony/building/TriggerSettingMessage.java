package no.monopixel.slimcolonies.core.network.messages.server.colony.building;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.modules.settings.ISetting;
import no.monopixel.slimcolonies.api.colony.buildings.modules.settings.ISettingKey;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.colony.requestsystem.StandardFactoryController;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.modules.SettingsModule;
import no.monopixel.slimcolonies.core.colony.buildings.modules.settings.SettingKey;
import no.monopixel.slimcolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message handling setting triggering.
 */
public class TriggerSettingMessage extends AbstractBuildingServerMessage<AbstractBuilding>
{
    /**
     * The unique setting key.
     */
    private ResourceLocation key;

    /**
     * The value of the setting.
     */
    private ISetting value;

    /**
     * The module id
     */
    private int moduleID;

    /**
     * Empty standard constructor.
     */
    public TriggerSettingMessage()
    {
        super();
    }

    /**
     * Settings constructor.
     * @param building the building involving the setting.
     * @param key the unique key of it.
     * @param value the value of the setting.
     */
    public TriggerSettingMessage(final IBuildingView building, final ISettingKey<?> key, final ISetting value, final int moduleID)
    {
        super(building);
        this.key = key.getUniqueId();
        this.value = value;
        this.moduleID = moduleID;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        this.moduleID = buf.readInt();
        this.key = buf.readResourceLocation();
        this.value = StandardFactoryController.getInstance().deserialize(buf);
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(moduleID);
        buf.writeResourceLocation(this.key);
        StandardFactoryController.getInstance().serialize(buf, this.value);
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final AbstractBuilding building)
    {
        if (building.getModule(moduleID) instanceof SettingsModule module)
        {
            module.updateSetting(new SettingKey<>(this.value.getClass(), this.key), this.value, ctxIn.getSender());
        }
    }
}
