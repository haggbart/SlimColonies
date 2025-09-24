package no.monopixel.slimcolonies.core.commands.colonycommands.requestsystem;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.core.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static no.monopixel.slimcolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_REQUEST_SYSTEM_RESET_ALL_SUCCESS;

public class CommandRSResetAll implements IMCOPCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        for (final IColony colony : IColonyManager.getInstance().getAllColonies())
        {
            colony.getRequestManager().reset();
        }
        context.getSource().sendSuccess(() -> Component.translatable(COMMAND_REQUEST_SYSTEM_RESET_ALL_SUCCESS), true);

        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "requestsystem-reset-all";
    }
}
