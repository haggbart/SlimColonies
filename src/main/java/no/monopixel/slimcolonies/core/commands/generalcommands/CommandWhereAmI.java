package no.monopixel.slimcolonies.core.commands.generalcommands;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.util.BlockPosUtil;
import no.monopixel.slimcolonies.api.util.MessageUtils;
import no.monopixel.slimcolonies.api.util.constant.translation.CommandTranslationConstants;
import no.monopixel.slimcolonies.core.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;

public class CommandWhereAmI implements IMCCommand
{
    /**
     * What happens when the command is executed
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final Entity sender = context.getSource().getEntity();

        final BlockPos playerPos = sender.blockPosition();
        final IColony colony = IColonyManager.getInstance().getClosestColony(sender.getCommandSenderWorld(), playerPos);

        if (colony == null)
        {
            MessageUtils.format(CommandTranslationConstants.COMMAND_WHERE_AM_I_NO_COLONY).sendTo((Player) sender);
            return 0;
        }
        final BlockPos center = colony.getCenter();
        final double distance = BlockPosUtil.getDistance2D(center, new BlockPos(playerPos.getX(), center.getY(), playerPos.getZ()));

        if (!IColonyManager.getInstance().isCoordinateInAnyColony(sender.getCommandSenderWorld(), playerPos))
        {
            MessageUtils.format(CommandTranslationConstants.COMMAND_WHERE_AM_I_COLONY_CLOSE, distance).sendTo((Player) sender);
            return 0;
        }

        final String colonyName = colony.getName();
        final String id = Integer.toString(colony.getID());

        MessageUtils.format(CommandTranslationConstants.COMMAND_WHERE_AM_I_IN_COLONY, colonyName, id, distance).sendTo((Player) sender);

        return 0;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "whereami";
    }
}
