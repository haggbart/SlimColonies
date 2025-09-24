package com.minecolonies.core.colony.interactionhandling;

import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.interactionhandling.IChatPriority;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers;
import com.minecolonies.api.eventbus.events.colony.citizens.CitizenAddedModEvent;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.StatsUtil;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.util.constant.StatisticsConstants.VISITORS_ABSCONDED;
import static com.minecolonies.api.util.constant.StatisticsConstants.VISITORS_RECRUITED;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.CHAT_LABEL_ID;

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

    /**
     * Chance for a bad visitor
     */
    private static final int BAD_VISITOR_CHANCE = 2;

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

            window.findPaneOfTypeByID(CHAT_LABEL_ID, Text.class).setText(PaneBuilders.textBuilder()
                .append(Component.literal(dataView.getName() + ": "))
                .append(this.getInquiry())
                .emptyLines(1)
                .appendNL(Component.translatable(
                    colony.getCitizens().size() < colony.getCitizenCountLimit() ? "com.minecolonies.coremod.gui.chat.recruit.free"
                        : "com.minecolonies.coremod.gui.chat.nospacerecruit.free"))
                .appendNL(Component.literal(""))
                .getText());
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
                    
                    if (colony.getWorld().random.nextInt(100) <= BAD_VISITOR_CHANCE)
                    {
                        StatsUtil.trackStat(tavern, VISITORS_ABSCONDED, 1);
                        colony.getStatisticsManager().increment(VISITORS_ABSCONDED, colony.getDay());

                        MessageUtils.format(MESSAGE_RECRUITMENT_RAN_OFF, data.getName()).sendTo(colony).forAllPlayers();
                        return;
                    }
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
