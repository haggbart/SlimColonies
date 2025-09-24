package no.monopixel.slimcolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import no.monopixel.slimcolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.tileentities.TileEntityGrave;
import no.monopixel.slimcolonies.core.client.gui.modules.GraveyardManagementWindow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GraveyardManagementModuleView extends AbstractBuildingModuleView
{
    /**
     * Contains a view object of all the graves in the colony.
     */
    @NotNull
    private List<BlockPos> graves = new ArrayList<>();

    /**
     * Contains a view object of all the restingCitizen in the colony.
     */
    @NotNull
    private List<String> restingCitizen = new ArrayList<>();

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        graves = new ArrayList<>();
        final int size = buf.readInt();
        for (int i = 1; i <= size; i++)
        {
            @NotNull final BlockPos pos = buf.readBlockPos();
            graves.add(pos);
        }

        restingCitizen = new ArrayList<>();
        final int sizeRIP = buf.readInt();
        for (int i = 1; i <= sizeRIP; i++)
        {
            restingCitizen.add(buf.readUtf());
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BOWindow getWindow()
    {
        return new GraveyardManagementWindow(buildingView, this);
    }

    @Override
    public ResourceLocation getIconResourceLocation()
    {
        return new ResourceLocation(Constants.MOD_ID, "textures/gui/modules/grave.png");
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.gui.workerhuts.enchanter.workers";
    }


    /**
     * Getter of the graves list.
     *
     * @return an unmodifiable List.
     */
    @NotNull
    public List<BlockPos> getGraves()
    {
        return graves;
    }

    /**
     * Clean the list of graves if a grave is missing from the world.
     */
    public void cleanGraves()
    {
        for (final BlockPos grave : new ArrayList<>(graves))
        {
            final BlockEntity entity = buildingView.getColony().getWorld().getBlockEntity(grave);
            if (!(entity instanceof TileEntityGrave))
            {
                graves.remove(grave);
            }
        }
    }

    /**
     * Getter of the restingCitizen list.
     *
     * @return an unmodifiable List.
     */
    @NotNull
    public List<String> getRestingCitizen()
    {
        return Collections.unmodifiableList(restingCitizen);
    }
}
