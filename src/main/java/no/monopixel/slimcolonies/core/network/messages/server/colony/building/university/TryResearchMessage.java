package no.monopixel.slimcolonies.core.network.messages.server.colony.building.university;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.research.IGlobalResearch;
import no.monopixel.slimcolonies.api.research.IGlobalResearchTree;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingUniversity;
import no.monopixel.slimcolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message for the research execution.
 */
public class TryResearchMessage extends AbstractBuildingServerMessage<BuildingUniversity>
{
    /**
     * Id of research to try research.
     */
    private ResourceLocation researchId;

    /**
     * Id of research to try research.
     */
    private ResourceLocation branch;

    /**
     * If the request is a reset.
     */
    private boolean reset;

    /**
     * Default constructor for forge
     */
    public TryResearchMessage() {super();}

    /**
     * Construct a message to attempt to research.
     *
     * @param researchId the research id.
     * @param branch     the research branch.
     * @param building   the building we're executing on.
     */
    public TryResearchMessage(final IBuildingView building, @NotNull final ResourceLocation researchId, final ResourceLocation branch, final boolean reset)
    {
        super(building);
        this.researchId = researchId;
        this.branch = branch;
        this.reset = reset;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        researchId = buf.readResourceLocation();
        branch = buf.readResourceLocation();
        reset = buf.readBoolean();
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(researchId);
        buf.writeResourceLocation(branch);
        buf.writeBoolean(reset);
    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingUniversity building)
    {
        final Player player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        final IGlobalResearch research = IGlobalResearchTree.getInstance().getResearch(branch, researchId);
        if(reset)
        {
            if(colony.getResearchManager().getResearchTree().getResearch(branch, researchId) != null)
            {
                colony.getResearchManager().getResearchTree().attemptResetResearch(player, colony, colony.getResearchManager().getResearchTree().getResearch(branch, researchId));
            }
        }
        else
        {
            if((research.canResearch(building.getBuildingLevel() == building.getMaxBuildingLevel() ? Integer.MAX_VALUE : building.getBuildingLevel(), colony.getResearchManager().getResearchTree()))
                 || player.isCreative())
            {
                colony.getResearchManager().getResearchTree().attemptBeginResearch(player, colony, research);
            }
        }
    }
}
