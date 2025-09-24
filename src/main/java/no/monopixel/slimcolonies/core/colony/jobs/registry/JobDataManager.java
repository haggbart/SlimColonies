package no.monopixel.slimcolonies.core.colony.jobs.registry;

import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.ICitizenDataView;
import no.monopixel.slimcolonies.api.colony.IColonyView;
import no.monopixel.slimcolonies.api.colony.jobs.IJob;
import no.monopixel.slimcolonies.api.colony.jobs.IJobView;
import no.monopixel.slimcolonies.api.colony.jobs.ModJobs;
import no.monopixel.slimcolonies.api.colony.jobs.registry.IJobDataManager;
import no.monopixel.slimcolonies.api.colony.jobs.registry.IJobRegistry;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.util.Log;
import no.monopixel.slimcolonies.api.util.constant.NbtTagConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class JobDataManager implements IJobDataManager
{
    @Nullable
    @Override
    public IJob<?> createFrom(
      final ICitizenData citizen, @NotNull final CompoundTag compound)
    {
        String jobTypeName = compound.getString(NbtTagConstants.TAG_JOB_TYPE);

        final ResourceLocation jobType =
          compound.contains(NbtTagConstants.TAG_JOB_TYPE) ? new ResourceLocation(jobTypeName) : ModJobs.PLACEHOLDER_ID;

        if (jobType == null)
        {
            Log.getLogger().error(String.format("Unknown job type '%s'.", jobTypeName), new Exception());
            return null;
        }

        JobEntry jobEntry = IJobRegistry.getInstance().getValue(jobType);

        if (jobEntry == null)
        {
            Log.getLogger().error(String.format("Unknown job entry for type '%s'.", jobTypeName), new Exception());
            return null;
        }

        final IJob<?> job = Optional.ofNullable(jobEntry).map(r -> r.produceJob(citizen)).orElse(null);

        if (job != null)
        {
            try
            {
                job.deserializeNBT(compound);
            }
            catch (final RuntimeException ex)
            {
                Log.getLogger().error(String.format("A Job %s has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
                  jobType), ex);
                return null;
            }
        }
        else
        {
            Log.getLogger().warn(String.format("Unknown Job type '%s' or missing constructor of proper format.", jobType));
        }

        return job;
    }

    @Override
    public IJobView createViewFrom(
      final IColonyView colony, final ICitizenDataView citizenDataView, final FriendlyByteBuf networkBuffer)
    {
        final ResourceLocation jobName = new ResourceLocation(networkBuffer.readUtf(32767));
        final JobEntry entry = IJobRegistry.getInstance().getValue(jobName);

        if (entry == null)
        {
            Log.getLogger().error(String.format("Unknown job type '%s'.", jobName), new Exception());
            return null;
        }

        final IJobView view = entry.getJobViewProducer().get().apply(colony, citizenDataView);

        if (view != null)
        {
            view.deserialize(networkBuffer);
        }

        return view;
    }
}
