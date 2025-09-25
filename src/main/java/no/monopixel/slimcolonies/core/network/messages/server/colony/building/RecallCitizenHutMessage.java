package no.monopixel.slimcolonies.core.network.messages.server.colony.building;

import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.api.util.EntityUtils;
import no.monopixel.slimcolonies.api.util.Log;
import no.monopixel.slimcolonies.api.util.MessageUtils;
import no.monopixel.slimcolonies.core.colony.buildings.views.AbstractBuildingView;
import no.monopixel.slimcolonies.core.network.messages.server.AbstractBuildingServerMessage;
import no.monopixel.slimcolonies.core.util.TeleportHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.WARNING_CITIZEN_RECALL_FAILED;

/**
 * Used to handle citizen recalls to their hut.
 */
public class RecallCitizenHutMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * Empty public constructor.
     */
    public RecallCitizenHutMessage()
    {
        super();
    }

    /**
     * Creates a message to recall all citizens to their hut.
     *
     * @param building {@link AbstractBuildingView}
     */
    public RecallCitizenHutMessage(@NotNull final AbstractBuildingView building)
    {
        super(building);
    }

    @Override
    protected void onExecute(@NotNull final NetworkEvent.Context ctxIn, final boolean isLogicalServer, @NotNull final IColony colony, @NotNull final IBuilding building)
    {
        final BlockPos location = building.getPosition();
        final Level world = colony.getWorld();
        for (final ICitizenData citizenData : building.getAllAssignedCitizen())
        {
            Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getEntity();
            if (!optionalEntityCitizen.isPresent())
            {
                Log.getLogger().warn(String.format("Citizen #%d:%d has gone AWOL, respawning them!", colony.getID(), citizenData.getId()));
                citizenData.setNextRespawnPosition(EntityUtils.getSpawnPoint(world, location));
                citizenData.updateEntityIfNecessary();
                optionalEntityCitizen = citizenData.getEntity();
            }

            if (optionalEntityCitizen.isPresent() && !TeleportHelper.teleportCitizen(optionalEntityCitizen.get(), world, location))
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

    @Override
    protected void toBytesOverride(final FriendlyByteBuf buf)
    {

    }

    @Override
    protected void fromBytesOverride(final FriendlyByteBuf buf)
    {

    }
}
