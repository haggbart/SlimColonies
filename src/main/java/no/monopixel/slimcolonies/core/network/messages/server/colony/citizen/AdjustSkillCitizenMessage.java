package no.monopixel.slimcolonies.core.network.messages.server.colony.citizen;

import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.ICitizenDataView;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.api.entity.citizen.Skill;
import no.monopixel.slimcolonies.api.util.Log;
import no.monopixel.slimcolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Adjust the skill level of the citizen.
 */
public class AdjustSkillCitizenMessage extends AbstractColonyServerMessage
{
    /**
     * The id of the citizen.
     */
    private int citizenId;

    /**
     * The skill quantity.
     */
    private int quantity;

    /**
     * The skill to alter.
     */
    private Skill skill;

    /**
     * Empty constructor used when registering the
     */
    public AdjustSkillCitizenMessage()
    {
        super();
    }

    /**
     * Creates a skill alteration message.
     *
     * @param citizenDataView Citizen of the request.
     * @param quantity        of item needed to be transfered
     * @param skill           the skill to alter.
     * @param colony          the colony of the network message
     */
    public AdjustSkillCitizenMessage(final IColony colony, @NotNull final ICitizenDataView citizenDataView, final int quantity, final Skill skill)
    {
        super(colony);
        this.citizenId = citizenDataView.getId();
        this.quantity = quantity;
        this.skill = skill;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        citizenId = buf.readInt();
        quantity = buf.readInt();
        skill = Skill.values()[buf.readInt()];
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(citizenId);
        buf.writeInt(quantity);
        buf.writeInt(skill.ordinal());
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        final ICitizenData citizenData = colony.getCitizenManager().getCivilian(citizenId);
        if (citizenData == null)
        {
            Log.getLogger().warn("AdjustSkillCitizenMessage citizenData is null");
            return;
        }

        final Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getEntity();
        if (!optionalEntityCitizen.isPresent())
        {
            Log.getLogger().warn("AdjustSkillCitizenMessage entity citizen is null");
            return;
        }

        final Player player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        final boolean isCreative = player.isCreative();
        if (!isCreative)
        {
            Log.getLogger().warn("AdjustSkillCitizenMessage player must be creative.");
            return;
        }

        citizenData.getCitizenSkillHandler().incrementLevel(skill, quantity);
        citizenData.markDirty(0);
    }
}
