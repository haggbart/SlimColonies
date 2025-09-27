package no.monopixel.slimcolonies.core.network.messages.server.colony;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.ICivilianData;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.core.colony.CitizenData;
import no.monopixel.slimcolonies.core.network.messages.server.AbstractColonyServerMessage;

import java.util.Random;

/**
 * Message to set the colony name style.
 */
public class ColonyNameStyleMessage extends AbstractColonyServerMessage
{
    /**
     * The chosen style.
     */
    private String style;

    /**
     * Default constructor
     **/
    public ColonyNameStyleMessage()
    {
        super();
    }

    /**
     * Change the colony name style from the client to the serverside.
     *
     * @param colony the colony the player changed the style in.
     * @param style  the list of patterns they set in the banner picker
     */
    public ColonyNameStyleMessage(final IColony colony, final String style)
    {
        super(colony);
        this.style = style;
    }

    @Override
    protected void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer, IColony colony)
    {
        colony.setNameStyle(style);

        final Random random = new Random();

        for (final ICitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            updateCivilianName(citizen, random, colony);
        }

        for (final ICivilianData visitor : colony.getVisitorManager().getCivilianDataMap().values())
        {
            updateCivilianName(visitor, random, colony);
        }
    }

    private static void updateCivilianName(final ICivilianData civilian, final Random random, final IColony colony)
    {
        if (civilian != null)
        {
            final String newName = CitizenData.generateName(random, civilian.isFemale(), colony, colony.getCitizenNameFile());
            civilian.setName(newName);
            civilian.getEntity().ifPresent(entity -> entity.setCustomName(Component.literal(newName)));
        }
    }

    @Override
    protected void toBytesOverride(FriendlyByteBuf buf)
    {
        buf.writeUtf(style);
    }

    @Override
    protected void fromBytesOverride(FriendlyByteBuf buf)
    {
        this.style = buf.readUtf(32767);
    }
}
