package no.monopixel.slimcolonies.core.colony.buildings.modules;

import com.ldtteam.blockui.views.BOWindow;
import no.monopixel.slimcolonies.api.colony.IColonyView;
import no.monopixel.slimcolonies.api.colony.buildings.modules.AbstractBuildingModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.IDefinesCoreBuildingStatsModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.stat.IStat;
import no.monopixel.slimcolonies.core.client.gui.huts.WindowHutLiving;
import no.monopixel.slimcolonies.core.colony.buildings.views.LivingBuildingView;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * The class of the citizen hut.
 */
public class HomeBuildingModule extends AbstractBuildingModule implements IDefinesCoreBuildingStatsModule
{
    @Override
    public IStat<Integer> getMaxInhabitants()
    {
        return (prev) -> Math.max(prev, building.getBuildingLevel());
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends LivingBuildingView
    {
        /**
         * Creates an instance of the citizen hut window.
         *
         * @param c the colonyView.
         * @param l the position the hut is at.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public BOWindow getWindow()
        {
            return new WindowHutLiving(this);
        }
    }
}
