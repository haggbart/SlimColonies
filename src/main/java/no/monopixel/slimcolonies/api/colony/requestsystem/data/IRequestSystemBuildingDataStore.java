package no.monopixel.slimcolonies.api.colony.requestsystem.data;

import com.google.common.reflect.TypeToken;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.IRequest;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;

import java.util.Collection;
import java.util.Map;

public interface IRequestSystemBuildingDataStore extends IDataStore
{
    Map<TypeToken<?>, Collection<IToken<?>>> getOpenRequestsByRequestableType();

    Map<Integer, Collection<IToken<?>>> getOpenRequestsByCitizen();

    Map<Integer, Collection<IToken<?>>> getCompletedRequestsByCitizen();

    Map<IToken<?>, Integer> getCitizensByRequest();

    /**
     * Move request from building (-1) to citizen and mark synchronous.
     * @param citizenData the citizen to move it to.
     * @param request the request to move.
     */
    void moveToSyncCitizen(ICitizenData citizenData, IRequest<?> request);
}
