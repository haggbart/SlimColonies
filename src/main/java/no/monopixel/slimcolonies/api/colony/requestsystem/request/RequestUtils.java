package no.monopixel.slimcolonies.api.colony.requestsystem.request;

import no.monopixel.slimcolonies.api.colony.requestsystem.manager.IRequestManager;
import no.monopixel.slimcolonies.api.colony.requestsystem.resolver.IRequestResolver;
import no.monopixel.slimcolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import no.monopixel.slimcolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;

/**
 * Util class for requests
 */
public class RequestUtils
{
    /**
     * Private constructor to hide the implicit one.
     */
    private RequestUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Checks if the request for this token requires player interaction
     *
     * @param token   Request token to check
     * @param manager Request manager
     */
    public static boolean requestChainNeedsPlayer(final IToken<?> token, final IRequestManager manager)
    {
        final IRequest<?> request = manager.getRequestForToken(token);
        if (request == null)
        {
            return false;
        }

        if (request.hasChildren())
        {
            for (final IToken<?> childToken : request.getChildren())
            {
                if (requestChainNeedsPlayer(childToken, manager))
                {
                    return true;
                }
            }
        }
        else
        {
            final IRequestResolver<?> resolver = manager.getResolverForRequest(token);
            return request.getState() == RequestState.IN_PROGRESS && (resolver instanceof IPlayerRequestResolver || resolver instanceof IRetryingRequestResolver);
        }

        return false;
    }
}
