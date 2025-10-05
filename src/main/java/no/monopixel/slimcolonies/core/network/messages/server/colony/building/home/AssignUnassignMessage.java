package no.monopixel.slimcolonies.core.network.messages.server.colony.building.home;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import no.monopixel.slimcolonies.api.ISlimColoniesAPI;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.core.colony.buildings.DefaultBuildingInstance;
import no.monopixel.slimcolonies.core.colony.buildings.modules.AbstractAssignedCitizenModule;
import no.monopixel.slimcolonies.core.colony.buildings.modules.LivingBuildingModule;
import no.monopixel.slimcolonies.core.colony.buildings.modules.WorkerBuildingModule;
import no.monopixel.slimcolonies.core.network.messages.server.AbstractBuildingServerMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Message class which manages the messages assigning or unassigning of citizens.
 */
public class AssignUnassignMessage extends AbstractBuildingServerMessage<DefaultBuildingInstance>
{
    /**
     * If assigning (true) else unassigning.
     */
    private boolean assign;

    /**
     * The citizen to assign/unassigning.
     */
    private int citizenID;

    /**
     * The job entry.
     */
    private JobEntry jobEntry;

    /**
     * Empty public constructor.
     */
    public AssignUnassignMessage()
    {
        super();
    }

    /**
     * Creates object for the player to assigning or unassigning a citizen.
     *
     * @param building  view of the building to read data from
     * @param assign    assign or unassigning the citizens
     * @param citizenID the id of the citizen to fill the job.
     * @param entry     the job entry.
     */
    public AssignUnassignMessage(@NotNull final IBuildingView building, final boolean assign, final int citizenID, final JobEntry entry)
    {
        super(building);
        this.assign = assign;
        this.citizenID = citizenID;
        this.jobEntry = entry;
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        assign = buf.readBoolean();
        citizenID = buf.readInt();
        if (buf.readBoolean())
        {
            jobEntry = buf.readRegistryId();
        }
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBoolean(assign);
        buf.writeInt(citizenID);
        if (jobEntry == null)
        {
            buf.writeBoolean(false);
        }
        else
        {
            buf.writeBoolean(true);
            buf.writeRegistryId(ISlimColoniesAPI.getInstance().getJobRegistry(), jobEntry);
        }
    }

    @Override
    public boolean errorIfCastFails()
    {
        return false;
    }

    @Override
    public void onExecute(
        final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final DefaultBuildingInstance building)
    {
        final ICitizenData citizen = colony.getCitizenManager().getCivilian(citizenID);
        final AbstractAssignedCitizenModule module;
        if (jobEntry == null)
        {
            module = building.getFirstModuleOccurance(LivingBuildingModule.class);
        }
        else
        {
            module = building.getModuleMatching(WorkerBuildingModule.class, m -> m.getJobEntry() == jobEntry);
        }

        if (assign && !module.isFull() && !building.equals(citizen.getHomeBuilding()))
        {
            if (citizen.getHomeBuilding() != null)
            {
                citizen.getHomeBuilding().getFirstModuleOccurance(LivingBuildingModule.class).removeCitizen(citizen);
            }
            module.assignCitizen(citizen);
        }
        else if (module.hasAssignedCitizen(citizen))
        {
            module.removeCitizen(citizen);
        }
    }
}
