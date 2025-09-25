package no.monopixel.slimcolonies.core.network.messages.server.colony.building;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Picks up the building block with the level.
 */
public class BuildPickUpMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * Empty constructor used when registering the
     */
    public BuildPickUpMessage()
    {
        super();
    }

    /**
     * Creates a build request
     *
     * @param building the building we're executing on.
     */
    public BuildPickUpMessage(@NotNull final IBuildingView building)
    {
        super(building);
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        final Player player = ctxIn.getSender();
        building.pickUp(player);
    }
}
