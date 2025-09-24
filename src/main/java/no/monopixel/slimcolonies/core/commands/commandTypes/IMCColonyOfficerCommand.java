package no.monopixel.slimcolonies.core.commands.commandTypes;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.util.MessageUtils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import static no.monopixel.slimcolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND;
import static no.monopixel.slimcolonies.core.commands.CommandArgumentNames.COLONYID_ARG;

/**
 * Commands which may be used by officers and owners or OP only.
 */
public interface IMCColonyOfficerCommand extends IMCCommand
{
    /**
     * Executes pre-checks before issuing the command. Checks for the senders type and OP rights.
     */
    @Override
    default boolean checkPreCondition(final CommandContext<CommandSourceStack> context)
    {
        if (context.getSource().hasPermission(OP_PERM_LEVEL))
        {
            return true;
        }


        final Entity sender = context.getSource().getEntity();
        if (!(sender instanceof Player))
        {
            return false;
        }

        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            MessageUtils.format(COMMAND_COLONY_ID_NOT_FOUND, colonyID).sendTo((Player) sender);
            return false;
        }

        // Check colony permissions
        return IMCCommand.isPlayerOped((Player) sender) || colony.getPermissions().getRank((Player) sender).isColonyManager();
    }
}
