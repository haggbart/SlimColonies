package no.monopixel.slimcolonies.api.colony.requestsystem.request;

import no.monopixel.slimcolonies.api.colony.requestsystem.manager.IRequestManager;
import no.monopixel.slimcolonies.api.colony.requestsystem.resolver.IRequestResolver;
import no.monopixel.slimcolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import no.monopixel.slimcolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;

import java.util.HashSet;
import java.util.Set;

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

    /**
     * Gathers all request tokens that are currently assigned to player or retrying resolvers.
     * This is used to find requests that require player attention.
     * Filters out orphaned tokens (tokens without corresponding requests).
     *
     * @param manager the request manager
     * @return a set of all pending request tokens
     */
    public static Set<IToken<?>> getAllPendingRequestTokens(final IRequestManager manager)
    {
        final IPlayerRequestResolver resolver = manager.getPlayerResolver();
        final IRetryingRequestResolver retryingRequestResolver = manager.getRetryingRequestResolver();

        final Set<IToken<?>> requestTokens = new HashSet<>();
        requestTokens.addAll(resolver.getAllAssignedRequests());
        requestTokens.addAll(retryingRequestResolver.getAllAssignedRequests());

        // TODO: This filter is a workaround for orphaned tokens in legacy save data. Can be removed in a future version once old saves are no longer supported.
        // Filter out orphaned tokens (stale tokens without actual requests)
        requestTokens.removeIf(token -> manager.getRequestForToken(token) == null);

        return requestTokens;
    }
}
