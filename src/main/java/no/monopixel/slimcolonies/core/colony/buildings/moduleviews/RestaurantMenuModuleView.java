package no.monopixel.slimcolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import no.monopixel.slimcolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.api.util.constant.translation.RequestSystemTranslationConstants;
import no.monopixel.slimcolonies.core.client.gui.modules.RestaurantMenuModuleWindow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static no.monopixel.slimcolonies.core.colony.buildings.modules.RestaurantMenuModule.STOCK_PER_LEVEL;
import static no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingCook.FOOD_EXCLUSION_LIST;

/**
 * Client side version of food menu.
 */
public class RestaurantMenuModuleView extends AbstractBuildingModuleView
{
    /**
     * The menu.
     */
    private final List<ItemStorage> menu = new ArrayList<>();

    @Override
    public void deserialize(final @NotNull FriendlyByteBuf buf)
    {
        menu.clear();
        final int size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            menu.add(new ItemStorage(buf.readItem()));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BOWindow getWindow()
    {
        return new RestaurantMenuModuleWindow(buildingView, this);
    }

    @Override
    public ResourceLocation getIconResourceLocation()
    {
        return new ResourceLocation(Constants.MOD_ID, "textures/gui/modules/" + FOOD_EXCLUSION_LIST + ".png");
    }

    @Override
    public String getDesc()
    {
        return RequestSystemTranslationConstants.REQUESTS_TYPE_FOOD;
    }

    /**
     * Get the menu for the restaurant.
     * @return the menu.
     */
    public List<ItemStorage> getMenu()
    {
        return menu;
    }

    public boolean hasReachedLimit()
    {
        return menu.size() >= buildingView.getBuildingLevel() * STOCK_PER_LEVEL;
    }
}
