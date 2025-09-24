package no.monopixel.slimcolonies.core.network.messages.server.colony.building.worker;

import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.api.util.EntityUtils;
import no.monopixel.slimcolonies.api.util.Log;
import no.monopixel.slimcolonies.api.util.MessageUtils;
import no.monopixel.slimcolonies.core.network.messages.server.AbstractBuildingServerMessage;
import no.monopixel.slimcolonies.core.util.TeleportHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.WARNING_CITIZEN_RECALL_FAILED;

/**
 * Recalls the citizen to the hut. Created: May 26, 2014
 *
 * @author Colton
 */
public class RecallCitizenMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * Empty public constructor.
     */
    public RecallCitizenMessage()
    {
        super();
    }

    @Override
    protected void toBytesOverride(final FriendlyByteBuf buf)
    {

    }

    @Override
    protected void fromBytesOverride(final FriendlyByteBuf buf)
    {

    }

    public RecallCitizenMessage(final IBuildingView building)
    {
        super(building);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        final List<ICitizenData> citizens = new ArrayList<>(building.getAllAssignedCitizen());
        for (int i = 0; i < building.getAllAssignedCitizen().size(); i++)
        {
            Optional<AbstractEntityCitizen> optionalEntityCitizen = citizens.get(i).getEntity();
            final ICitizenData citizenData = citizens.get(i);
            if (!optionalEntityCitizen.isPresent())
            {
                if (citizenData != null)
                {
                    Log.getLogger().warn(String.format("Citizen #%d:%d has gone AWOL, respawning them!", colony.getID(), citizenData.getId()));
                    citizenData.setNextRespawnPosition(EntityUtils.getSpawnPoint(colony.getWorld(), building.getPosition()));
                    citizenData.updateEntityIfNecessary();
                }
                else
                {
                    Log.getLogger().warn("Citizen is AWOL and citizenData is null!");
                    return;
                }
            }
            else if (optionalEntityCitizen.get().getTicksExisted() == 0)
            {
                citizenData.getEntity().ifPresent(e -> e.remove(Entity.RemovalReason.DISCARDED));
                citizenData.updateEntityIfNecessary();
            }

            final BlockPos loc = building.getPosition();
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
}
