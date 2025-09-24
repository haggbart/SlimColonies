package no.monopixel.slimcolonies.core.network.messages.server.colony.building.fields;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingExtensionsModule;
import no.monopixel.slimcolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to change the assignmentMode of the fields of the farmer.
 */
public class AssignmentModeMessage extends AbstractBuildingServerMessage<IBuilding>
{
    private int id;
    private boolean assignmentMode;

    /**
     * Empty standard constructor.
     */
    public AssignmentModeMessage()
    {
        super();
    }

    /**
     * Creates object for the assignmentMode
     *
     * @param assignmentMode assignmentMode of the particular farmer.
     * @param building       the building we're executing on.
     */
    public AssignmentModeMessage(@NotNull final IBuildingView building, final boolean assignmentMode , final int runtimeID)
    {
        super(building);
        this.assignmentMode = assignmentMode;
        this.id = runtimeID;
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(id);
        buf.writeBoolean(assignmentMode);
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        id = buf.readInt();
        assignmentMode = buf.readBoolean();
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        if (building.hasModule(BuildingExtensionsModule.class))
        {
            ((BuildingExtensionsModule)building.getModule(id)).setAssignManually(assignmentMode);
        }
    }
}
