package no.monopixel.slimcolonies.core.network.messages.client.colony;

import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.network.IMessage;
import no.monopixel.slimcolonies.core.colony.ColonyView;
import no.monopixel.slimcolonies.core.colony.buildings.views.AbstractBuildingView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Add or Update a AbstractBuilding.View to a ColonyView on the client.
 */
public class OpenBuildingUIMessage implements IMessage
{
    private int          colonyId;
    private BlockPos     buildingId;

    /**
     * Dimension of the colony.
     */
    private ResourceKey<Level> dimension;

    /**
     * Empty constructor used when registering the
     */
    public OpenBuildingUIMessage()
    {
        super();
    }

    /**
     * Creates a message to handle colony views.
     *
     * @param building AbstractBuilding to add or update a view.
     */
    public OpenBuildingUIMessage(@NotNull final IBuilding building)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
        buf.writeUtf(dimension.location().toString());
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        if (IColonyManager.getInstance().getColonyView(colonyId, dimension) instanceof ColonyView colonyView && colonyView.getBuilding(buildingId) instanceof AbstractBuildingView buildingView)
        {
            buildingView.openGui(false);
        }
    }
}
