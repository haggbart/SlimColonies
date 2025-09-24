package no.monopixel.slimcolonies.core.network.messages.server.colony;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.network.IMessage;
import no.monopixel.slimcolonies.api.util.MessageUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.MESSAGE_INFO_COLONY_DESTROY_SUCCESS;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.MESSAGE_INFO_COLONY_NOT_FOUND;

/**
 * Message for deleting an owned colony
 */
public class ColonyDeleteOwnMessage implements IMessage
{
    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {

    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {

    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final ServerPlayer player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        final IColony colony = IColonyManager.getInstance().getIColonyByOwner(player.level, player);
        if (colony != null)
        {
            IColonyManager.getInstance().deleteColonyByDimension(colony.getID(), false, colony.getDimension());
            MessageUtils.format(MESSAGE_INFO_COLONY_DESTROY_SUCCESS).sendTo(player);
        }
        else
        {
            MessageUtils.format(MESSAGE_INFO_COLONY_NOT_FOUND).sendTo(player);
        }
    }
}
