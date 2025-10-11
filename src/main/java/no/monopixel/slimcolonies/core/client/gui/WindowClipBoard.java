package no.monopixel.slimcolonies.core.client.gui;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.ldtteam.blockui.Color;
import com.ldtteam.blockui.controls.Button;
import no.monopixel.slimcolonies.api.colony.ICitizenDataView;
import no.monopixel.slimcolonies.api.colony.IColonyView;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.colony.requestsystem.manager.IRequestManager;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.IRequest;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.RequestState;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.RequestUtils;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.MinimumStack;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;
import no.monopixel.slimcolonies.api.util.Log;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.items.ItemClipboard;
import no.monopixel.slimcolonies.core.network.messages.server.ItemSettingMessage;
import no.monopixel.slimcolonies.core.network.messages.server.colony.UpdateRequestStateMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

import org.jetbrains.annotations.NotNull;

import java.util.*;

import static no.monopixel.slimcolonies.api.util.constant.WindowConstants.CLIPBOARD_TOGGLE;

public class WindowClipBoard extends AbstractWindowRequestTree
{
    private static final String BUILD_TOOL_RESOURCE_SUFFIX = ":gui/windowclipboard.xml";
    private final List<IToken<?>> asyncRequest = new ArrayList<>();
    private final IColonyView colony;
    private boolean hide;

    public WindowClipBoard(final IColonyView colony, boolean hidestate)
    {
        super(null, Constants.MOD_ID + BUILD_TOOL_RESOURCE_SUFFIX, colony);
        this.colony = colony;
        this.hide = hidestate;

        for (final ICitizenDataView view : this.colony.getCitizens().values())
        {
            if (view.getJobView() != null)
            {
                asyncRequest.addAll(view.getJobView().getAsyncRequests());
            }
        }

        registerButton(CLIPBOARD_TOGGLE, this::toggleImportant);
        paintButtonState();
    }

    private void toggleImportant()
    {
        this.hide = !this.hide;

        paintButtonState();

        ItemSettingMessage hideSetting = new ItemSettingMessage();
        hideSetting.setSetting(ItemClipboard.TAG_HIDEUNIMPORTANT, this.hide ? 1 : 0);
        Network.getNetwork().sendToServer(hideSetting);
    }

    private void paintButtonState()
    {
        final Button importantToggle = findPaneOfTypeByID("important", Button.class);

        if (this.hide)
        {
            importantToggle.setColors(Color.getByName("green", 0));
        }
        else
        {
            importantToggle.setColors(Color.getByName("red", 0));
        }
    }

    @Override
    public ImmutableList<IRequest<?>> getOpenRequestsFromBuilding(final IBuildingView building)
    {
        final ArrayList<IRequest<?>> requests = Lists.newArrayList();

        if (colony == null)
        {
            return ImmutableList.of();
        }

        final IRequestManager requestManager = colony.getRequestManager();

        try
        {
            final Set<IToken<?>> requestTokens = RequestUtils.getAllPendingRequestTokens(requestManager);

            for (final IToken<?> token : requestTokens)
            {
                IRequest<?> request = requestManager.getRequestForToken(token);

                // Skip null requests (stale tokens)
                if (request == null)
                {
                    continue;
                }

                if (hide && request.getType().equals(TypeToken.of(MinimumStack.class)))
                {
                    continue;
                }

                while (request != null && request.hasParent())
                {
                    request = requestManager.getRequestForToken(request.getParent());
                }

                if (request != null && !requests.contains(request))
                {
                    requests.add(request);
                }
            }

            if (hide)
            {
                requests.removeIf(req -> asyncRequest.contains(req.getId()));
            }

            final BlockPos playerPos = Minecraft.getInstance().player.blockPosition();
            requests.sort(Comparator.comparing((IRequest<?> request) -> request.getRequester().getLocation().getInDimensionLocation()
                    .distSqr(new Vec3i(playerPos.getX(), playerPos.getY(), playerPos.getZ())))
                .thenComparingInt((IRequest<?> request) -> request.getId().hashCode()));
        }
        catch (Exception e)
        {
            Log.getLogger().warn("Exception trying to retreive requests:", e);
            requestManager.reset();
            return ImmutableList.of();
        }

        return ImmutableList.copyOf(requests);
    }

    @Override
    public boolean fulfillable(final IRequest<?> tRequest)
    {
        return false;
    }

    @Override
    protected void cancel(@NotNull final IRequest<?> request)
    {
        Network.getNetwork().sendToServer(new UpdateRequestStateMessage(colony, request.getId(), RequestState.CANCELLED, null));
    }
}
