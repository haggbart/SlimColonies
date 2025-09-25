package no.monopixel.slimcolonies.core.client.gui.map;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.View;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import no.monopixel.slimcolonies.core.network.messages.client.colony.ColonyListMessage;

/**
 * Represents a colony by size, returns the used image corresponding for each size.
 */
public enum ColonySize
{
    SMALL("slimcolonies:gui/map/colonysmall.xml", 25),
    MEDIUM("slimcolonies:gui/map/colonymedium.xml", 75),
    LARGE("slimcolonies:gui/map/colonylarge.xml", 5000);

    private final String imagePath;
    private final int    maxCitizens;

    ColonySize(final String imagePath, final int maxCitizens)
    {
        this.imagePath = imagePath;
        this.maxCitizens = maxCitizens;
    }

    public static View createViewForInfo(final ColonyListMessage.ColonyInfo colony)
    {
        final View colonyPane = new View();

        final ColonySize size = getSizeByCount(colony.getCitizencount());

        Loader.createFromXMLFile(new ResourceLocation(size.imagePath), colonyPane);

        final Pane background = colonyPane.findPaneByID("background");
        colonyPane.setSize(background.getWidth(), background.getHeight());

        final Text colonyName = colonyPane.findPaneOfTypeByID("textcontent", Text.class);
        colonyName.setText(Component.literal(colony.getName()));

        return colonyPane;
    }

    public static ColonySize getSizeByCount(final int count)
    {
        for (ColonySize size : values())
        {
            if (count < size.maxCitizens)
            {
                return size;
            }
        }
        return SMALL;
    }
}
