package no.monopixel.slimcolonies.core.client.gui.townhall;

import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonHandler;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.views.BOWindow;
import no.monopixel.slimcolonies.api.colony.IColonyView;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.colony.ColonyView;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import static no.monopixel.slimcolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for a town hall name entry.
 */
public class WindowTownHallNameEntry extends BOWindow implements ButtonHandler
{
    private static final String TOWNHALL_NAME_RESOURCE_SUFFIX = ":gui/townhall/windowtownhallnameentry.xml";

    private final IColonyView colony;

    /**
     * Constructor for a town hall rename entry window.
     *
     * @param c {@link ColonyView}
     */
    public WindowTownHallNameEntry(final IColonyView c)
    {
        super(new ResourceLocation(Constants.MOD_ID + TOWNHALL_NAME_RESOURCE_SUFFIX));
        this.colony = c;
    }

    @Override
    public void onOpened()
    {
        findPaneOfTypeByID(INPUT_NAME, TextField.class).setText(Component.translatable(colony.getName().toLowerCase(Locale.US)).getString());
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (button.getID().equals(BUTTON_DONE))
        {
            final String name = findPaneOfTypeByID(INPUT_NAME, TextField.class).getText();
            if (!name.isEmpty())
            {
                colony.setName(name);
            }
        }
        else if (!button.getID().equals(BUTTON_CANCEL))
        {
            return;
        }

        if (colony.getTownHall() != null)
        {
            colony.getTownHall().openGui(false);
        }
    }
}
