package no.monopixel.slimcolonies.core.commands.colonycommands;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.core.commands.commandTypes.IMCColonyOfficerCommand;
import no.monopixel.slimcolonies.core.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import static no.monopixel.slimcolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND;
import static no.monopixel.slimcolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_OWNER_CHANGE_SUCCESS;
import static no.monopixel.slimcolonies.core.commands.CommandArgumentNames.COLONYID_ARG;

public class CommandSetAbandoned implements IMCColonyOfficerCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final Entity sender = context.getSource().getEntity();

        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(() -> Component.translatable(COMMAND_COLONY_ID_NOT_FOUND, colonyID), true);
            return 0;
        }

        boolean addOfficer = false;
        if (sender != null && (colony.getPermissions().getRank((Player) sender).isColonyManager()))
        {
            addOfficer = true;
        }

        colony.getPermissions().setOwnerAbandoned();

        if (addOfficer)
        {
            colony.getPermissions().addPlayer(((Player) sender).getGameProfile(), colony.getPermissions().getRankOfficer());
        }

        context.getSource().sendSuccess(() -> Component.translatable(COMMAND_OWNER_CHANGE_SUCCESS, "[abandoned]", colony.getName()), true);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "setAbandoned";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1)).executes(this::checkPreConditionAndExecute));
    }
}
