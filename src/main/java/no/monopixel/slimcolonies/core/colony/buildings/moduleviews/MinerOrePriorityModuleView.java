package no.monopixel.slimcolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import no.monopixel.slimcolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static no.monopixel.slimcolonies.core.colony.buildings.modules.MinerOrePriorityModule.ORES_PER_LEVEL;

/**
 * Client side version of miner ore priority module.
 */
public class MinerOrePriorityModuleView extends AbstractBuildingModuleView
{
    private final List<ItemStorage> priorityOres = new ArrayList<>();

    @Override
    public void deserialize(final @NotNull FriendlyByteBuf buf)
    {
        priorityOres.clear();
        final int size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            priorityOres.add(new ItemStorage(buf.readItem()));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BOWindow getWindow()
    {
        return new no.monopixel.slimcolonies.core.client.gui.modules.MinerOrePriorityModuleWindow(buildingView, this);
    }

    @Override
    public ResourceLocation getIconResourceLocation()
    {
        return new ResourceLocation(Constants.MOD_ID, "textures/gui/modules/ores.png");
    }

    @Override
    public String getDesc()
    {
        return "no.monopixel.slimcolonies.coremod.gui.workerhuts.miner.orepriority.desc";
    }

    public List<ItemStorage> getPriorityOres()
    {
        return priorityOres;
    }

    public boolean hasReachedLimit()
    {
        return priorityOres.size() >= buildingView.getBuildingLevel() * ORES_PER_LEVEL;
    }
}
