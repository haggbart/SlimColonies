package no.monopixel.slimcolonies.core.commands.colonycommands;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.core.commands.commandTypes.IMCCommand;
import no.monopixel.slimcolonies.core.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static no.monopixel.slimcolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_COLONY_DELETABLE_SUCCESS;
import static no.monopixel.slimcolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND;
import static no.monopixel.slimcolonies.core.commands.CommandArgumentNames.COLONYID_ARG;

// TODO: Unused, maybe drop or add an auto delete feature
public class CommandSetDeletable implements IMCOPCommand
{

    private static final String DELETEABLE_ARG = "deletable";

    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(() -> Component.translatable(COMMAND_COLONY_ID_NOT_FOUND, colonyID), true);
            return 0;
        }

        colony.setCanBeAutoDeleted(BoolArgumentType.getBool(context, DELETEABLE_ARG));
        context.getSource()
          .sendSuccess(() -> Component.translatable(COMMAND_COLONY_DELETABLE_SUCCESS, colonyID, BoolArgumentType.getBool(context, DELETEABLE_ARG)), true);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "setDeleteable";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .then(IMCCommand.newArgument(DELETEABLE_ARG, BoolArgumentType.bool()).executes(this::checkPreConditionAndExecute)));
    }
}
