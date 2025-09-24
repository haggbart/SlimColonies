package no.monopixel.slimcolonies.core.colony.buildings.modules;

import java.util.List;

import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.managers.interfaces.IStatisticsManager;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingBarracks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import static no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingModules.STATS_MODULE;

/**
 * Building statistic module.
 */
public class BarracksStatisticsModule extends BuildingStatisticsModule
{
    /**
     * Serializes the composite stats of all towers associated with this barracks
     *
     * @param buf      the buffer to write to
     * @param fullSync whether to serialize the full stats or only the dirty ones
     */
    @Override
    public void serializeToView(final FriendlyByteBuf buf, final boolean fullSync)
    {
        this.getBuildingStatisticsManager().clear();
        List<BlockPos> towers = ((BuildingBarracks) building).getTowers();

        for (final BlockPos towerPos : towers)
        {
            IBuilding tower = building.getColony().getBuildingManager().getBuilding(towerPos);
            if (tower != null)
            {
                BuildingStatisticsModule towerStats = tower.getModule(STATS_MODULE);
                IStatisticsManager.aggregateStats(this.getBuildingStatisticsManager(), towerStats.getBuildingStatisticsManager());
            }
        }

        this.getBuildingStatisticsManager().serialize(buf, fullSync);
    }
}
