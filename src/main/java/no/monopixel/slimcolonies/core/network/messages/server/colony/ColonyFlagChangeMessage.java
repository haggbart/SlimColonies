package no.monopixel.slimcolonies.core.network.messages.server.colony;

import no.monopixel.slimcolonies.api.IMinecoloniesAPI;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.eventbus.events.colony.ColonyFlagChangedModEvent;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.client.gui.WindowBannerPicker;
import no.monopixel.slimcolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_BANNER_PATTERNS;

/**
 * Message to update the colony flag once set in the {@link WindowBannerPicker}.
 */
public class ColonyFlagChangeMessage extends AbstractColonyServerMessage
{
    /**
     * The chosen list of patterns from the window
     */
    private ListTag patterns;

    /**
     * Default constructor
     **/
    public ColonyFlagChangeMessage() {super();}

    /**
     * Spawn a new change message
     *
     * @param colony      the colony the player changed the banner in
     * @param patternList the list of patterns they set in the banner picker
     */
    public ColonyFlagChangeMessage(IColony colony, ListTag patternList)
    {
        super(colony);

        this.patterns = patternList;
    }

    @Override
    protected void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer, IColony colony)
    {
        colony.setColonyFlag(patterns);
        IMinecoloniesAPI.getInstance().getEventBus().post(new ColonyFlagChangedModEvent(colony));
    }

    @Override
    protected void toBytesOverride(FriendlyByteBuf buf)
    {
        CompoundTag nbt = new CompoundTag();
        nbt.put(TAG_BANNER_PATTERNS, this.patterns);
        buf.writeNbt(nbt);
    }

    @Override
    protected void fromBytesOverride(FriendlyByteBuf buf)
    {
        CompoundTag nbt = buf.readNbt();
        if (nbt != null)
        {
            this.patterns = nbt.getList(TAG_BANNER_PATTERNS, Constants.TAG_COMPOUND);
        }
    }
}
