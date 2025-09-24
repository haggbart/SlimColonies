package no.monopixel.slimcolonies.api.colony.requestsystem.management;

import no.monopixel.slimcolonies.api.colony.requestsystem.management.update.UpdateType;
import no.monopixel.slimcolonies.api.colony.requestsystem.manager.IRequestManager;

public interface IUpdateHandler
{
    IRequestManager getManager();

    void handleUpdate(final UpdateType type);

    int getCurrentVersion();
}
