package no.monopixel.slimcolonies.api.colony.buildings.registry;

import no.monopixel.slimcolonies.api.IMinecoloniesAPI;
import net.minecraftforge.registries.IForgeRegistry;

public interface IBuildingRegistry
{

    static IForgeRegistry<BuildingEntry> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getBuildingRegistry();
    }
}
