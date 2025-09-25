package no.monopixel.slimcolonies.api.colony.workorders;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;


public interface IServerWorkOrder extends IWorkOrder
{
    /**
     * Checks if the workOrder has changed.
     *
     * @return true if so.
     */
    boolean isDirty();

    /**
     * Resets the changed variable.
     */
    void resetChange();

    /**
     * Is this WorkOrder still valid? If not, it will be deleted.
     * <p>
     *
     * @param colony The colony that owns the Work Order
     * @return True if the WorkOrder is still valid, or False if it should be deleted
     */
    
    boolean isValid(IColony colony);

    /**
     * Read the WorkOrder data from the CompoundTag.
     *
     * @param compound NBT Tag compound
     * @param manager  the workManager calling this method.
     */
    void read(@NotNull CompoundTag compound, IWorkManager manager);

    /**
     * Save the Work Order to an CompoundTag.
     *
     * @param compound NBT tag compount
     */
    void write(@NotNull CompoundTag compound);

    /**
     * Writes the workOrders data to a byte buf for transition.
     *
     * @param buf Buffer to write to
     */
    void serializeViewNetworkData(@NotNull FriendlyByteBuf buf);

    /**
     * Executed when a work order is added.
     * <p>
     * Override this when something need to be done when the work order is added
     *
     * @param colony         in which the work order exist
     * @param readingFromNbt if being read from NBT.
     */
    void onAdded(IColony colony, boolean readingFromNbt);

    /**
     * Whether the workorder can be built by the given building.
     *
     * @param building the building.
     * @return
     */
    boolean canBuild(IBuilding building);
}
