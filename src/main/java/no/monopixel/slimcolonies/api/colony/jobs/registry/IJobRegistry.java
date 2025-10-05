package no.monopixel.slimcolonies.api.colony.jobs.registry;

import net.minecraftforge.registries.IForgeRegistry;
import no.monopixel.slimcolonies.api.ISlimColoniesAPI;

public interface IJobRegistry
{
    static IForgeRegistry<JobEntry> getInstance()
    {
        return ISlimColoniesAPI.getInstance().getJobRegistry();
    }
}
