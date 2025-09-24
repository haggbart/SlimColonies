package no.monopixel.slimcolonies.core.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.*;
import net.minecraft.network.FriendlyByteBuf;
import no.monopixel.slimcolonies.api.colony.buildings.modules.AbstractBuildingModule;
import org.jetbrains.annotations.NotNull;

/**
 * Server side colony connection module of a gatehouse. This is noop, gatehouse gets info from connection manager.
 */
public class ColonyConnectionModule extends AbstractBuildingModule
{
    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        // Nothing needed.
    }
}
