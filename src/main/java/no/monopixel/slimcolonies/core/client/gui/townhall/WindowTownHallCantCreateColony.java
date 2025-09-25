package no.monopixel.slimcolonies.core.client.gui.townhall;

import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Text;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.client.gui.AbstractWindowSkeleton;
import no.monopixel.slimcolonies.core.network.messages.server.PickupBlockMessage;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;

import static no.monopixel.slimcolonies.api.util.constant.Constants.MOD_ID;
import static no.monopixel.slimcolonies.api.util.constant.WindowConstants.*;

/**
 * UI to notify the player that a colony can't be created here.
 */
public class WindowTownHallCantCreateColony extends AbstractWindowSkeleton
{
    /**
     * Townhall position
     */
    private BlockPos pos;

    public WindowTownHallCantCreateColony(final BlockPos pos, final MutableComponent warningMsg, final boolean displayConfigTooltip)
    {
        super(MOD_ID + TOWNHALL_CANT_CREATE_GUI);
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        this.pos = pos;
        registerButton(BUTTON_CANCEL, this::close);
        registerButton(BUTTON_PICKUP_BUILDING, this::pickup);
        final Text text = this.findPaneOfTypeByID("text1", Text.class);
        text.setText(warningMsg);
        if (displayConfigTooltip)
        {
            PaneBuilders.singleLineTooltip(Component.translatable("no.monopixel.slimcolonies.core.configsetting"), text);
        }
    }

    /**
     * When the pickup building button was clicked.
     */
    private void pickup()
    {
        Network.getNetwork().sendToServer(new PickupBlockMessage(pos));
        close();
    }
}
