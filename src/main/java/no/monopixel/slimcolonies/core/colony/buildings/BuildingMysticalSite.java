package no.monopixel.slimcolonies.core.colony.buildings;

import com.ldtteam.blockui.views.BOWindow;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyView;
import no.monopixel.slimcolonies.api.colony.buildings.IMysticalSite;
import no.monopixel.slimcolonies.core.client.gui.WindowHutMinPlaceholder;
import no.monopixel.slimcolonies.core.colony.buildings.views.AbstractBuildingView;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

public class BuildingMysticalSite extends AbstractBuilding implements IMysticalSite
{
    private static final String MYSTICAL_SITE = "mysticalsite";

    /**
     * Maximum building level
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * The constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingMysticalSite(@NotNull final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return MYSTICAL_SITE;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    /**
     * The client side representation of the building.
     */
    public static class View extends AbstractBuildingView
    {
        /**
         * Instantiates the view of the building.
         *
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public BOWindow getWindow()
        {
            return new WindowHutMinPlaceholder<>(this);
        }
    }
}
