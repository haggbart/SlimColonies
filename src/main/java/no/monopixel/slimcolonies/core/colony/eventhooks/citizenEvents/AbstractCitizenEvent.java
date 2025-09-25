package no.monopixel.slimcolonies.core.colony.eventhooks.citizenEvents;

import no.monopixel.slimcolonies.api.colony.colonyEvents.descriptions.ICitizenEventDescription;
import no.monopixel.slimcolonies.api.util.BlockPosUtil;
import no.monopixel.slimcolonies.core.colony.eventhooks.AbstractEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.*;

/**
 * Event for something happening to a citizen.
 */
public abstract class AbstractCitizenEvent extends AbstractEvent implements ICitizenEventDescription
{

    private BlockPos eventPos;
    private String citizenName;

    /**
     * Creates a new citizen event.
     */
    public AbstractCitizenEvent()
    {

    }

    /**
     * Creates a new citizen event.
     *
     * @param eventPos    the position of the hut block of the building.
     * @param citizenName the name of the building.
     */
    public AbstractCitizenEvent(final boolean includeInSummary, final BlockPos eventPos, final String citizenName)
    {
        super(includeInSummary);
        this.eventPos = eventPos;
        this.citizenName = citizenName;
    }

    @Override
    public BlockPos getEventPos()
    {
        return eventPos;
    }

    @Override
    public void setEventPos(BlockPos eventPos)
    {
        this.eventPos = eventPos;
    }

    @Override
    public String getCitizenName()
    {
        return citizenName;
    }

    @Override
    public void setCitizenName(String citizenName)
    {
        this.citizenName = citizenName;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag compound = super.serializeNBT();
        BlockPosUtil.write(compound, TAG_EVENT_POS, eventPos);
        compound.putString(TAG_CITIZEN_NAME, citizenName);
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundTag compound)
    {
        super.deserializeNBT(compound);
        eventPos = BlockPosUtil.read(compound, TAG_EVENT_POS);
        citizenName = compound.getString(TAG_CITIZEN_NAME);
    }

    @Override
    public void serialize(FriendlyByteBuf buf)
    {
        super.serialize(buf);
        buf.writeBlockPos(eventPos);
        buf.writeUtf(citizenName);
    }

    @Override
    public void deserialize(FriendlyByteBuf buf)
    {
        super.deserialize(buf);
        eventPos = buf.readBlockPos();
        citizenName = buf.readUtf();
    }

    @Override
    public String toString()
    {
        return toDisplayString();
    }
}
