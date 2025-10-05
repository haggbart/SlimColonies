package no.monopixel.slimcolonies.api.colony.buildings.registry;

import net.minecraftforge.registries.IForgeRegistry;
import no.monopixel.slimcolonies.api.ISlimColoniesAPI;

public interface IBuildingRegistry
{

    static IForgeRegistry<BuildingEntry> getInstance()
    {
        return ISlimColoniesAPI.getInstance().getBuildingRegistry();
    }
}
