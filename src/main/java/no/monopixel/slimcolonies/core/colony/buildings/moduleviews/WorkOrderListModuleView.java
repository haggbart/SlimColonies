package no.monopixel.slimcolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import no.monopixel.slimcolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.client.gui.modules.WorkOrderModuleWindow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Client side version of the abstract class for building that handle workorders.
 */
public class WorkOrderListModuleView extends AbstractBuildingModuleView
{
    /**
     * The tool of the worker.
     */
    public WorkOrderListModuleView()
    {
        super();
    }

    @Override
    public String getDesc()
    {
        return "no.monopixel.slimcolonies.coremod.gui.townhall.workorders";
    }

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BOWindow getWindow()
    {
        return new WorkOrderModuleWindow(Constants.MOD_ID + ":gui/layouthuts/layoutworkorders.xml", buildingView, this);
    }

    @Override
    public ResourceLocation getIconResourceLocation()
    {
        return new ResourceLocation(Constants.MOD_ID, "textures/gui/modules/info.png");
    }
}
