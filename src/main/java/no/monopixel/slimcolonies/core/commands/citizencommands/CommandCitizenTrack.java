package no.monopixel.slimcolonies.core.commands.citizencommands;

import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.api.util.constant.translation.CommandTranslationConstants;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.commands.commandTypes.IMCColonyOfficerCommand;
import no.monopixel.slimcolonies.core.commands.commandTypes.IMCCommand;
import no.monopixel.slimcolonies.core.entity.pathfinding.PathfindingUtils;
import no.monopixel.slimcolonies.core.network.messages.client.SyncPathMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static no.monopixel.slimcolonies.core.commands.CommandArgumentNames.CITIZENID_ARG;
import static no.monopixel.slimcolonies.core.commands.CommandArgumentNames.COLONYID_ARG;

/**
 * Displays information about a chosen citizen in a chosen colony.
 */
public class CommandCitizenTrack implements IMCColonyOfficerCommand
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
        if (!(sender instanceof Player))
        {
            return 1;
        }

        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, sender == null ? Level.OVERWORLD : context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND, colonyID), true);
            return 0;
        }

        final ICitizenData citizenData = colony.getCitizenManager().getCivilian(IntegerArgumentType.getInteger(context, CITIZENID_ARG));

        if (citizenData == null)
        {
            context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_NOT_FOUND), true);
            return 0;
        }

        final Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getEntity();

        if (!optionalEntityCitizen.isPresent())
        {
            context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_NOT_LOADED), true);
            return 0;
        }
        final AbstractEntityCitizen entityCitizen = optionalEntityCitizen.get();

        if (PathfindingUtils.trackingMap.getOrDefault(sender.getUUID(), UUID.randomUUID()).equals(entityCitizen.getUUID()))
        {
            context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_ENTITY_TRACK_DISABLED), true);
            PathfindingUtils.trackingMap.remove(sender.getUUID());
            Network.getNetwork()
              .sendToPlayer(new SyncPathMessage(new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>()), (ServerPlayer) sender);
        }
        else
        {
            context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_ENTITY_TRACK_ENABLED), true);
            PathfindingUtils.trackingMap.put(sender.getUUID(), entityCitizen.getUUID());
        }


        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "trackPath";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .then(IMCCommand.newArgument(CITIZENID_ARG, IntegerArgumentType.integer(1)).executes(this::checkPreConditionAndExecute)));
    }
}
