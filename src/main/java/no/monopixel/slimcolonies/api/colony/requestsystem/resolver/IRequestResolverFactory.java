package no.monopixel.slimcolonies.api.colony.requestsystem.resolver;

import no.monopixel.slimcolonies.api.colony.requestsystem.factory.IFactory;
import no.monopixel.slimcolonies.api.colony.requestsystem.location.ILocation;

/**
 * Interface describing an object that is capable of constructing a specific {@link IRequestResolver}
 *
 * @param <Resolver> The type of {@link IRequestResolver} this factory can produce.
 */
public interface IRequestResolverFactory<Resolver extends IRequestResolver<?>> extends IFactory<ILocation, Resolver>
{
}
