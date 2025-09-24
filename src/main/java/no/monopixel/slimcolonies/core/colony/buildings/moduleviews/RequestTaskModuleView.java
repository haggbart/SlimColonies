package no.monopixel.slimcolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import no.monopixel.slimcolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.client.gui.modules.WindowHutRequestTaskModule;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Request task module to display tasks in the UI.
 */
public abstract class RequestTaskModuleView extends AbstractBuildingModuleView
{
    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BOWindow getWindow()
    {
        return new WindowHutRequestTaskModule(buildingView, Constants.MOD_ID + ":gui/layouthuts/layouttasklist.xml");
    }

    @Override
    public ResourceLocation getIconResourceLocation()
    {
        return new ResourceLocation(Constants.MOD_ID, "textures/gui/modules/info.png");
    }

    @Override
    public String getDesc()
    {
        return "no.monopixel.slimcolonies.coremod.gui.workerhuts.crafter.tasks";
    }

    /**
     * Get the specific task list.
     *
     * @return the task list.
     */
    public abstract List<IToken<?>> getTasks();
}
