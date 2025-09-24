package no.monopixel.slimcolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import no.monopixel.slimcolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.client.gui.modules.EnchanterStationModuleWindow;
import no.monopixel.slimcolonies.core.network.messages.server.colony.building.enchanter.EnchanterWorkerSetMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EnchanterStationsModuleView extends AbstractBuildingModuleView
{
    /**
     * List of buildings the enchanter gathers experience from.
     */
    private List<BlockPos> buildingToGatherFrom = new ArrayList<>();

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        final int size = buf.readInt();
        buildingToGatherFrom.clear();
        for (int i = 0; i < size; i++)
        {
            buildingToGatherFrom.add(buf.readBlockPos());
        }
    }

    /**
     * Getter for the list.
     *
     * @return the list.
     */
    public List<BlockPos> getBuildingsToGatherFrom()
    {
        return buildingToGatherFrom;
    }

    /**
     * Add a new worker to gather xp from.
     *
     * @param blockPos the pos of the building.
     */
    public void addWorker(final BlockPos blockPos)
    {
        buildingToGatherFrom.add(blockPos);
        Network.getNetwork().sendToServer(new EnchanterWorkerSetMessage(buildingView, blockPos, true));
    }

    /**
     * Remove a worker to stop gathering from.
     *
     * @param blockPos the pos of that worker.
     */
    public void removeWorker(final BlockPos blockPos)
    {
        buildingToGatherFrom.remove(blockPos);
        Network.getNetwork().sendToServer(new EnchanterWorkerSetMessage(buildingView, blockPos, false));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BOWindow getWindow()
    {
        return new EnchanterStationModuleWindow(buildingView, this);
    }

    @Override
    public ResourceLocation getIconResourceLocation()
    {
        return new ResourceLocation(Constants.MOD_ID, "textures/gui/modules/entity.png");
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.gui.workerhuts.enchanter.workers";
    }
}
