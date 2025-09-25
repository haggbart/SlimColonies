package no.monopixel.slimcolonies.core.colony.workorders;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.workorders.WorkOrderType;
import no.monopixel.slimcolonies.api.util.MessageUtils;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingBuilder;
import no.monopixel.slimcolonies.core.entity.ai.workers.util.ConstructionTapeHelper;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.MESSAGE_NEW_DECORATION_REQUEST;
/**
 * A work order that the build can take to build plantation fields.
 */
public class WorkOrderPlantationField extends AbstractWorkOrder
{
    /**
     * Unused constructor for reflection.
     */
    public WorkOrderPlantationField()
    {
        super();
    }

    private WorkOrderPlantationField(
      String packName, String path, final String translationKey, WorkOrderType workOrderType, BlockPos location, int rotation, boolean isMirrored, int currentLevel,
      int targetLevel)
    {
        super(packName, path, translationKey, workOrderType, location, rotation, isMirrored, currentLevel, targetLevel);
    }

    public static WorkOrderPlantationField create(
      @NotNull final WorkOrderType type,
      final String packName,
      final String path,
      final String translationKey,
      final BlockPos location,
      final int rotation,
      final boolean mirror,
      final int currentLevel)
    {
        int targetLevel = 1;
        if (type == WorkOrderType.REMOVE)
        {
            targetLevel = 0;
        }

        return new WorkOrderPlantationField(
          packName,
          path,
          translationKey,
          type,
          location,
          rotation,
          mirror,
          currentLevel,
          targetLevel);
    }

    @Override
    public boolean canBuild(final IBuilding building)
    {
        return building instanceof BuildingBuilder;
    }

    /**
     * Check if a citizen may accept this workOrder while ignoring the distance to the build location.
     * <p>
     * @param building    the building that is assigned.
     * @param position    the position of the citizen's work hut.
     * @param level       the level of that work hut.
     * @return true if the citizen may accept this work order.
     */
    
    @Override
    public boolean canBuildIgnoringDistance(final @NotNull IBuilding building, final BlockPos position, final int level)
    {
        return canBuild(building);
    }

    @Override
    public boolean isValid(final IColony colony)
    {
        return super.isValid(colony) && this.getStructurePath() != null && !this.getStructurePath().isEmpty();
    }

    @Override
    public void onAdded(final IColony colony, final boolean readingFromNbt)
    {
        super.onAdded(colony, readingFromNbt);
        if (!readingFromNbt && colony != null && colony.getWorld() != null)
        {
            ConstructionTapeHelper.placeConstructionTape(this, colony.getWorld(), colony);
            MessageUtils.format(MESSAGE_NEW_DECORATION_REQUEST, colony.getName()).sendTo(colony).forManagers();
        }
    }

    @Override
    public void onRemoved(final IColony colony)
    {
        super.onRemoved(colony);
        ConstructionTapeHelper.removeConstructionTape(this, colony.getWorld());
    }
}
