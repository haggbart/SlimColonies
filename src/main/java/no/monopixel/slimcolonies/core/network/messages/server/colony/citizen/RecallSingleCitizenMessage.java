package no.monopixel.slimcolonies.core.network.messages.server.colony.citizen;

import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.api.util.MessageUtils;
import no.monopixel.slimcolonies.core.network.messages.server.AbstractBuildingServerMessage;
import no.monopixel.slimcolonies.core.util.TeleportHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.WARNING_CITIZEN_RECALL_FAILED;

/**
 * Recalls the citizen to the location.
 */
public class RecallSingleCitizenMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * The citizen id.
     */
    private int citizenId;

    /**
     * Empty public constructor.
     */
    public RecallSingleCitizenMessage()
    {
        super();
    }

    /**
     * Object creation for the recall.
     *
     * @param building  View of the building the citizen should be teleported to.
     * @param citizenid the id of the citizen.
     */
    public RecallSingleCitizenMessage(final IBuildingView building, final int citizenid)
    {
        super(building);
        this.citizenId = citizenid;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

        citizenId = buf.readInt();
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

        buf.writeInt(citizenId);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        final ICitizenData citizenData = colony.getCitizenManager().getCivilian(citizenId);
        citizenData.setLastPosition(building.getPosition());
        Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getEntity();
        if (!optionalEntityCitizen.isPresent())
        {
            citizenData.updateEntityIfNecessary();
            optionalEntityCitizen = citizenData.getEntity();
        }

        if (optionalEntityCitizen.isPresent() && optionalEntityCitizen.get().getTicksExisted() == 0)
        {
            citizenData.updateEntityIfNecessary();
        }

        final BlockPos loc = building.getID();
        if (optionalEntityCitizen.isPresent() && !TeleportHelper.teleportCitizen(optionalEntityCitizen.get(), colony.getWorld(), loc))
        {
            final Player player = ctxIn.getSender();
            if (player == null)
            {
                return;
            }

            MessageUtils.format(WARNING_CITIZEN_RECALL_FAILED).sendTo(player);
        }
    }
}
