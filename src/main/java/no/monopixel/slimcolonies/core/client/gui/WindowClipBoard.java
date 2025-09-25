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
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.MinimumStack;
import no.monopixel.slimcolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import no.monopixel.slimcolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
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

/**
 * ClipBoard window.
 */
public class WindowClipBoard extends AbstractWindowRequestTree
{
    /**
     * Resource suffix.
     */
    private static final String BUILD_TOOL_RESOURCE_SUFFIX = ":gui/windowclipboard.xml";

    /**
     * List of async request tokens.
     */
    private final List<IToken<?>> asyncRequest = new ArrayList<>();

    /**
     * The colony id.
     */
    private final IColonyView colony;

    /**
     * Hide or show not important requests.
     */
    private boolean hide = false;

    /**
     * Constructor of the clipboard GUI.
     *
     * @param colony the colony to check the requests for.
     */
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

    /**
     * Toggles the visibility of non-important requests and sends a message to
     * the server to save that setting on the clipboard item.
     *
     * @see ItemSettingMessage
     */
    private void toggleImportant()
    {
        this.hide = !this.hide;

        paintButtonState();

        ItemSettingMessage hideSetting = new ItemSettingMessage();
        hideSetting.setSetting(ItemClipboard.TAG_HIDEUNIMPORTANT, this.hide ? 1 : 0);
        Network.getNetwork().sendToServer(hideSetting);
    }


    /**
     * Paints the button state of the important toggle.
     *
     * This function finds the important toggle button and sets its colors based on the state of hide.
     * If hide is true, the button is set to green. Otherwise, it is set to red.
     */
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

        if (requestManager == null)
        {
            return ImmutableList.of();
        }

        try
        {
            final IPlayerRequestResolver resolver = requestManager.getPlayerResolver();
            final IRetryingRequestResolver retryingRequestResolver = requestManager.getRetryingRequestResolver();

            final Set<IToken<?>> requestTokens = new HashSet<>();
            requestTokens.addAll(resolver.getAllAssignedRequests());
            requestTokens.addAll(retryingRequestResolver.getAllAssignedRequests());

            for (final IToken<?> token : requestTokens)
            {
                IRequest<?> request = requestManager.getRequestForToken(token);

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
