package no.monopixel.slimcolonies.core.colony.requestsystem.resolvers.core;

import no.monopixel.slimcolonies.api.colony.requestsystem.location.ILocation;
import no.monopixel.slimcolonies.api.colony.requestsystem.manager.IRequestManager;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.IRequest;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.IRequestable;
import no.monopixel.slimcolonies.api.colony.requestsystem.resolver.IRequestResolver;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import static no.monopixel.slimcolonies.api.util.constant.RSConstants.CONST_DEFAULT_RESOLVER_PRIORITY;

/**
 * ----------------------- Not Documented Object ---------------------
 */
public abstract class AbstractRequestResolver<R extends IRequestable> implements IRequestResolver<R>
{
    @NotNull
    private final ILocation location;

    @NotNull
    private final IToken<?> token;

    public AbstractRequestResolver(@NotNull final ILocation location, @NotNull final IToken<?> token)
    {
        this.location = location;
        this.token = token;
    }

    @Override
    public IToken<?> getId()
    {
        return token;
    }

    @NotNull
    @Override
    public ILocation getLocation()
    {
        return location;
    }

    @Override
    public int getSuitabilityMetric(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request)
    {
        return 0;
    }

    @NotNull
    @Override
    public MutableComponent getRequesterDisplayName(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        return Component.literal("Request System");
    }

    @Override
    public int getPriority()
    {
        return CONST_DEFAULT_RESOLVER_PRIORITY;
    }
}
