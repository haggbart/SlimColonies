package no.monopixel.slimcolonies.core.colony.requestsystem.requesters;

import no.monopixel.slimcolonies.api.colony.requestsystem.manager.IRequestManager;
import no.monopixel.slimcolonies.api.colony.requestsystem.requester.IRequester;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface IBuildingBasedRequester extends IRequester
{
    /**
     * Get the building.
     *
     * @param manager the manager.
     * @param token   the token.
     * @return the IRequester or empty.
     */
    Optional<IRequester> getBuilding(@NotNull final IRequestManager manager, @NotNull final IToken<?> token);
}
