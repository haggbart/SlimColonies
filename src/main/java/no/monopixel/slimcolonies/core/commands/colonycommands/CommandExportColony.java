package no.monopixel.slimcolonies.core.commands.colonycommands;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.core.commands.commandTypes.IMCCommand;
import no.monopixel.slimcolonies.core.commands.commandTypes.IMCOPCommand;
import no.monopixel.slimcolonies.core.util.BackUpHelper;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static no.monopixel.slimcolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_COLONY_EXPORT_SUCCESS;
import static no.monopixel.slimcolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND;
import static no.monopixel.slimcolonies.core.commands.CommandArgumentNames.COLONYID_ARG;

/**
 * Command to export a colony from a world save, exports region and backup file.
 */
public class CommandExportColony implements IMCOPCommand
{
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final int colonyId = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        BackUpHelper.backupColonyData();
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(() -> Component.translatable(COMMAND_COLONY_ID_NOT_FOUND, colonyId), true);
            return 0;
        }

        context.getSource().sendSuccess(() -> Component.translatable(COMMAND_COLONY_EXPORT_SUCCESS, BackUpHelper.exportColony(colony)), true);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "export";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1)).executes(this::checkPreConditionAndExecute));
    }
}
