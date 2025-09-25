package no.monopixel.slimcolonies.api.colony.jobs.registry;

import no.monopixel.slimcolonies.api.IMinecoloniesAPI;
import net.minecraftforge.registries.IForgeRegistry;

public interface IJobRegistry
{
    static IForgeRegistry<JobEntry> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getJobRegistry();
    }
}
