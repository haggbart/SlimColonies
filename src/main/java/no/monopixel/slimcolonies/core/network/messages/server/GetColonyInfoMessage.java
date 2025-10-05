package no.monopixel.slimcolonies.core.network.messages.server;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.network.IMessage;
import no.monopixel.slimcolonies.api.util.BlockPosUtil;
import no.monopixel.slimcolonies.api.util.MessageUtils;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.SlimColonies;
import no.monopixel.slimcolonies.core.colony.Colony;
import no.monopixel.slimcolonies.core.network.messages.client.OpenCantFoundColonyWarningMessage;
import no.monopixel.slimcolonies.core.network.messages.client.OpenColonyFoundingCovenantMessage;
import no.monopixel.slimcolonies.core.network.messages.client.OpenDeleteAbandonColonyMessage;
import no.monopixel.slimcolonies.core.network.messages.client.OpenReactivateColonyMessage;
import no.monopixel.slimcolonies.core.tileentities.TileEntityColonyBuilding;
import org.jetbrains.annotations.Nullable;

import static no.monopixel.slimcolonies.api.util.constant.BuildingConstants.DEACTIVATED;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.HUT_BLOCK_MISSING_BUILDING;
import static no.monopixel.slimcolonies.core.SlimColonies.getConfig;

/**
 * Message for asking the server for some colony info before creation.
 */
public class GetColonyInfoMessage implements IMessage
{
    /**
     * Position the player wants to found the colony at.
     */
    BlockPos pos;

    public GetColonyInfoMessage()
    {
        super();
    }

    public GetColonyInfoMessage(final BlockPos pos)
    {
        this.pos = pos;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        pos = buf.readBlockPos();
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
        final ServerPlayer sender = ctxIn.getSender();
        final Level world = ctxIn.getSender().level;

        if (sender == null)
        {
            return;
        }

        if (IColonyManager.getInstance().getColonyByPosFromWorld(world, pos) instanceof Colony)
        {
            MessageUtils.format(HUT_BLOCK_MISSING_BUILDING).sendTo(sender);
            return;
        }

        if (IColonyManager.getInstance().getIColonyByOwner(world, sender) instanceof Colony colony)
        {
            Network.getNetwork().sendToPlayer(new OpenDeleteAbandonColonyMessage(pos, colony.getName(), colony.getCenter(), colony.getID()), sender);
            return;
        }

        final IColony nextColony = IColonyManager.getInstance().getClosestColony(world, pos);
        if (IColonyManager.getInstance().isFarEnoughFromColonies(world, pos))
        {
            final double spawnDistance = Math.sqrt(BlockPosUtil.getDistanceSquared2D(pos, world.getSharedSpawnPos()));
            if (spawnDistance < SlimColonies.getConfig().getServer().minDistanceFromWorldSpawn.get())
            {
                Network.getNetwork()
                    .sendToPlayer(new OpenCantFoundColonyWarningMessage(Component.translatable("no.monopixel.slimcolonies.core.founding.tooclosetospawn",
                        (int) (SlimColonies.getConfig().getServer().minDistanceFromWorldSpawn.get() - spawnDistance)), pos, true), sender);
            }
            else if (spawnDistance > SlimColonies.getConfig().getServer().maxDistanceFromWorldSpawn.get())
            {
                Network.getNetwork()
                    .sendToPlayer(new OpenCantFoundColonyWarningMessage(Component.translatable("no.monopixel.slimcolonies.core.founding.toofarfromspawn",
                        (int) (spawnDistance - SlimColonies.getConfig().getServer().maxDistanceFromWorldSpawn.get())), pos, true), sender);
            }
            else if (world.getBlockEntity(pos) instanceof TileEntityColonyBuilding townhall && townhall.getPositionedTags().containsKey(BlockPos.ZERO)
                && townhall.getPositionedTags().get(BlockPos.ZERO).contains(DEACTIVATED))
            {
                Network.getNetwork()
                    .sendToPlayer(new OpenReactivateColonyMessage(nextColony == null ? "" : nextColony.getName(),
                        nextColony == null
                            ? Integer.MAX_VALUE
                            : (int) BlockPosUtil.getDistance(nextColony.getCenter(), pos) - (getConfig().getServer().initialColonySize.get() << 4),
                        pos), sender);
            }
            else
            {
                Network.getNetwork()
                    .sendToPlayer(new OpenColonyFoundingCovenantMessage(nextColony == null ? "" : nextColony.getName(),
                        nextColony == null
                            ? Integer.MAX_VALUE
                            : (int) BlockPosUtil.getDistance(nextColony.getCenter(), pos) - (getConfig().getServer().initialColonySize.get() << 4),
                        pos), sender);
            }
        }
        else
        {
            if (nextColony == null)
            {
                return;
            }

            final int blockRange = Math.max(SlimColonies.getConfig().getServer().minColonyDistance.get(), getConfig().getServer().initialColonySize.get()) << 4;
            final int distance = (int) BlockPosUtil.getDistance(pos, nextColony.getCenter());

            Network.getNetwork()
                .sendToPlayer(new OpenCantFoundColonyWarningMessage(Component.translatable("no.monopixel.slimcolonies.core.founding.tooclosetocolony",
                    Math.max(100, blockRange - distance)), pos, false), sender);
        }
    }
}
