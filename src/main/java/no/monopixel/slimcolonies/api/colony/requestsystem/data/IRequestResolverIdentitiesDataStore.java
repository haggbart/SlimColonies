package no.monopixel.slimcolonies.api.colony.requestsystem.data;

import no.monopixel.slimcolonies.api.colony.requestsystem.request.IRequest;
import no.monopixel.slimcolonies.api.colony.requestsystem.resolver.IRequestResolver;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;

/**
 * The KV-Store for the requests and their identities. Extends the {@link IIdentitiesDataStore} with {@link IToken} as key type and {@link IRequest} as value type.
 */
public interface IRequestResolverIdentitiesDataStore extends IIdentitiesDataStore<IToken<?>, IRequestResolver<?>>
{
}
