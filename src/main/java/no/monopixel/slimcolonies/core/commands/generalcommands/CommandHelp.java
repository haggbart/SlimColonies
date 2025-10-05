package no.monopixel.slimcolonies.core.commands.generalcommands;

import no.monopixel.slimcolonies.core.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.ForgeHooks;

import static no.monopixel.slimcolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_HELP_INFO_DISCORD;
import static no.monopixel.slimcolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_HELP_INFO_WIKI;

public class CommandHelp implements IMCCommand
{

    private static final String wikiUrl    = "TBA";
    private static final String discordUrl = "https://discord.gg/bfKpMTTuFt";

    /**
     * What happens when the command is executed
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final Entity sender = context.getSource().getEntity();
        if (!(sender instanceof Player))
        {
            return 0;
        }

        context.getSource().sendSuccess(() -> Component.translatable(COMMAND_HELP_INFO_WIKI), true);
        context.getSource().sendSuccess(() -> ((MutableComponent) ForgeHooks.newChatWithLinks(wikiUrl)).append(Component.literal("\n")), true);
        context.getSource().sendSuccess(() -> Component.translatable(COMMAND_HELP_INFO_DISCORD), true);
        context.getSource().sendSuccess(() -> ForgeHooks.newChatWithLinks(discordUrl), true);

        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "help";
    }
}
