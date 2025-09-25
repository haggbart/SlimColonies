package no.monopixel.slimcolonies.api.colony.buildings.workerbuildings;

import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import no.monopixel.slimcolonies.api.tileentities.AbstractTileEntityWareHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IWareHouse extends IBuilding
{
    /**
     * Check if deliveryman is allowed to access warehouse.
     *
     * @param buildingWorker the data of the deliveryman.
     * @return true if able to.
     */
    boolean canAccessWareHouse(ICitizenData buildingWorker);

    /**
     * Upgrade all containers by 9 slots.
     *
     * @param world the world object.
     */
    void upgradeContainers(Level world);

    /**
     * Returns the tile entity that belongs to the colony building.
     *
     * @return {@link AbstractTileEntityColonyBuilding} object of the building.
     */
    @Override
    AbstractTileEntityWareHouse getTileEntity();

    /**
     * Check if a container position belongs to the warehouse.
     *
     * @param inDimensionLocation the location.
     * @return true if so.
     */
    boolean hasContainerPosition(BlockPos inDimensionLocation);
}
