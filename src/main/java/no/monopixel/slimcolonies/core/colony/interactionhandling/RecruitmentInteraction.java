package no.monopixel.slimcolonies.core.colony.interactionhandling;

import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.BOWindow;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import no.monopixel.slimcolonies.api.IMinecoloniesAPI;
import no.monopixel.slimcolonies.api.colony.*;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.interactionhandling.IChatPriority;
import no.monopixel.slimcolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import no.monopixel.slimcolonies.api.colony.interactionhandling.ModInteractionResponseHandlers;
import no.monopixel.slimcolonies.api.eventbus.events.colony.citizens.CitizenAddedModEvent;
import no.monopixel.slimcolonies.api.util.MessageUtils;
import no.monopixel.slimcolonies.api.util.StatsUtil;
import no.monopixel.slimcolonies.api.util.Tuple;
import no.monopixel.slimcolonies.api.util.constant.Constants;

import java.util.Collections;
import java.util.List;

import static no.monopixel.slimcolonies.api.util.constant.StatisticsConstants.VISITORS_RECRUITED;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.*;
import static no.monopixel.slimcolonies.api.util.constant.WindowConstants.CHAT_LABEL_ID;

/**
 * Interaction for recruiting visitors
 */
public class RecruitmentInteraction extends ServerCitizenInteraction
{
    /**
     * The icon's res location which is displayed for this interaction
     */
    private static final ResourceLocation icon = new ResourceLocation(Constants.MOD_ID, "textures/icons/recruiticon.png");

    /**
     * The recruit answer
     */
    private static final Tuple<Component, Component> recruitAnswer = new Tuple<>(Component.translatable("com.minecolonies.coremod.gui.chat.recruit"), null);

    @SuppressWarnings("unchecked")
    private static final Tuple<Component, Component>[] responses = (Tuple<Component, Component>[]) new Tuple[] {
        new Tuple<>(Component.translatable("com.minecolonies.coremod.gui.chat.showstats"), null),
        recruitAnswer,
        new Tuple<>(Component.translatable("com.minecolonies.coremod.gui.chat.notnow"), null)};

    public RecruitmentInteraction(final ICitizen data)
    {
        super(data);
    }

    public RecruitmentInteraction(
        final Component inquiry,
        final IChatPriority priority)
    {
        super(inquiry, true, priority, d -> true, null, responses);
    }

    @Override
    public List<IInteractionResponseHandler> genChildInteractions()
    {
        return Collections.emptyList();
    }

    @Override
    public String getType()
    {
        return ModInteractionResponseHandlers.RECRUITMENT.getPath();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onWindowOpened(final BOWindow window, final ICitizenDataView dataView)
    {
        if (dataView instanceof IVisitorViewData)
        {
            final IColonyView colony = (IColonyView) dataView.getColony();

            final boolean hasSpace = colony.getCitizens().size() < colony.getCitizenCountLimit();

            String baseText = dataView.getName() + ": " + this.getInquiry().getString();

            if (!hasSpace)
            {
                baseText += "\n\n" + Component.translatable("com.minecolonies.coremod.gui.chat.nospacerecruit").getString();
            }

            window.findPaneOfTypeByID(CHAT_LABEL_ID, Text.class).setText(Component.literal(baseText));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onClientResponseTriggered(final int responseId, final Player player, final ICitizenDataView data, final BOWindow window)
    {
        return super.onClientResponseTriggered(responseId, player, data, window);
    }

    @Override
    public void onServerResponseTriggered(final int responseId, final Player player, final ICitizenData data)
    {
        final Component response = getPossibleResponses().get(responseId);
        if (response.equals(recruitAnswer.getA()) && data instanceof IVisitorData)
        {
            IColony colony = data.getColony();
            if (colony.getCitizenManager().getCurrentCitizenCount() < colony.getCitizenManager().getPotentialMaxCitizens())
            {
                // Recruits visitor as new citizen and respawns entity
                colony.getVisitorManager().removeCivilian(data);
                data.setHomeBuilding(null);
                data.setJob(null);

                final IBuilding tavern = colony.getBuildingManager().getFirstBuildingMatching(b -> b.getBuildingType() == ModBuildings.tavern.get());
                StatsUtil.trackStat(tavern, VISITORS_RECRUITED, 1);
                colony.getStatisticsManager().increment(VISITORS_RECRUITED, colony.getDay());

                // Create and read new citizen
                ICitizenData newCitizen = colony.getCitizenManager().createAndRegisterCivilianData();
                newCitizen.deserializeNBT(data.serializeNBT());
                newCitizen.setParents("", "");
                newCitizen.setLastPosition(data.getLastPosition());

                // Exchange entities
                newCitizen.updateEntityIfNecessary();
                data.getEntity().ifPresent(e -> e.remove(Entity.RemovalReason.DISCARDED));

                if (data.hasCustomTexture())
                {
                    MessageUtils.format(MESSAGE_RECRUITMENT_SUCCESS_CUSTOM, data.getName()).sendTo(colony).forAllPlayers();
                }
                else
                {
                    MessageUtils.format(MESSAGE_RECRUITMENT_SUCCESS, data.getName()).sendTo(colony).forAllPlayers();
                }

                IMinecoloniesAPI.getInstance()
                    .getEventBus()
                    .post(new CitizenAddedModEvent(newCitizen, CitizenAddedModEvent.CitizenAddedSource.HIRED));
            }
            else
            {
                MessageUtils.format(WARNING_NO_COLONY_SPACE).sendTo(player);
            }
        }
    }

    @Override
    public ResourceLocation getInteractionIcon()
    {
        return icon;
    }
}
