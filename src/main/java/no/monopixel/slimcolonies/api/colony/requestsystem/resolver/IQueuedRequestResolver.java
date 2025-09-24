package no.monopixel.slimcolonies.api.colony.requestsystem.resolver;

import com.google.common.collect.ImmutableList;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.IRequestable;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;

public interface IQueuedRequestResolver<R extends IRequestable> extends IRequestResolver<R>
{
    /**
     * Method to get a list of all assigned tokens to this resolver.
     *
     * @return A list of all assigned tokens.
     */
    ImmutableList<IToken<?>> getAllAssignedRequests();

    /**
     * Called when the request system this is part of gets reset.
     */
    void onSystemReset();
}
