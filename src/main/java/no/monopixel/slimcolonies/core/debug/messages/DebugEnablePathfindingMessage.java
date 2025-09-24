package no.monopixel.slimcolonies.core.debug.messages;

import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.ICitizenDataView;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.core.debug.DebugPlayerManager;
import no.monopixel.slimcolonies.core.entity.pathfinding.PathfindingUtils;
import no.monopixel.slimcolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to toggle pathfinding debug for a specific citizen
 */
public class DebugEnablePathfindingMessage extends AbstractColonyServerMessage
{
    /**
     * Citizen id
     */
    private int id;

    /**
     * Whether to enable or disable pathfinding tracking
     */
    private boolean enable = false;

    public DebugEnablePathfindingMessage()
    {
        super();
    }

    public DebugEnablePathfindingMessage(final ICitizenDataView citizen, final boolean enable)
    {
        super(citizen.getColony());
        this.id = citizen.getId();
        this.enable = enable;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        this.id = buf.readInt();
        enable = buf.readBoolean();
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(id);
        buf.writeBoolean(enable);
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        final Player player = ctxIn.getSender();
        if (player == null || !DebugPlayerManager.hasDebugEnabled(player))
        {
            return;
        }

        final ICitizenData citizen = colony.getCitizenManager().getCivilian(id);
        if (citizen == null || !citizen.getEntity().isPresent())
        {
            return;
        }

        if (enable)
        {
            PathfindingUtils.trackingMap.put(player.getUUID(), citizen.getUUID());
        }
        else
        {
            PathfindingUtils.trackingMap.remove(player.getUUID());
        }
    }
}
