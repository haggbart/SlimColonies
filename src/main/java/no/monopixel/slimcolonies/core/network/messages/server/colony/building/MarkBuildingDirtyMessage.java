package no.monopixel.slimcolonies.core.network.messages.server.colony.building;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Send a message to the server to mark the building as dirty. Created: January 20, 2017
 *
 * @author xavierh
 */
public class MarkBuildingDirtyMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * Empty constructor used when registering the
     */
    public MarkBuildingDirtyMessage()
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

    public MarkBuildingDirtyMessage(final IBuildingView building)
    {
        super(building);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        building.markDirty();
    }
}
