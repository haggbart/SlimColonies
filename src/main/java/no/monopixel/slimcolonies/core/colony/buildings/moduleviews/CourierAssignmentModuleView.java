package no.monopixel.slimcolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import no.monopixel.slimcolonies.api.colony.ICitizenDataView;
import no.monopixel.slimcolonies.api.colony.buildings.HiringMode;
import no.monopixel.slimcolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import no.monopixel.slimcolonies.api.colony.buildings.modules.IAssignmentModuleView;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.colony.jobs.ModJobs;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.client.gui.modules.SpecialAssignmentModuleWindow;
import no.monopixel.slimcolonies.core.network.messages.server.colony.building.CourierHiringModeMessage;
import no.monopixel.slimcolonies.core.network.messages.server.colony.building.HireFireMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Module view for courier assignment.
 */
public class CourierAssignmentModuleView extends AbstractBuildingModuleView implements IAssignmentModuleView
{
    /**
     * List of the worker ids.
     */
    private final Set<Integer> workerIDs = new HashSet<>();

    /**
     * The hiring mode of the building.
     */
    private HiringMode hiringMode;

    /**
     * Max number of miners.
     */
    private int maxSize;

    @Override
    public List<Integer> getAssignedCitizens()
    {
        return new ArrayList<>(workerIDs);
    }

    @Override
    public void addCitizen(final @NotNull ICitizenDataView citizen)
    {
        workerIDs.add(citizen.getId());
        Network.getNetwork().sendToServer(new HireFireMessage(buildingView, true, citizen.getId(), getProducer().getRuntimeID()));
    }

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        final int size = buf.readInt();
        workerIDs.clear();
        for (int i = 0; i < size; i++)
        {
            workerIDs.add(buf.readInt());
        }

        this.hiringMode = HiringMode.values()[buf.readInt()];
        this.maxSize = buf.readInt();
    }

    @Override
    public ResourceLocation getIconResourceLocation()
    {
        return new ResourceLocation(Constants.MOD_ID, "textures/gui/modules/entity.png");
    }

    @Override
    public String getDesc()
    {
        return "no.monopixel.slimcolonies.coremod.gui.workerhuts.warehouse.couriers";
    }

    @Override
    public boolean isPageVisible()
    {
        return true;
    }

    @Override
    public void removeCitizen(final @NotNull ICitizenDataView citizen)
    {
        workerIDs.remove(citizen.getId());
        Network.getNetwork().sendToServer(new HireFireMessage(buildingView, false, citizen.getId(), getProducer().getRuntimeID()));
    }

    @Override
    public HiringMode getHiringMode()
    {
        return hiringMode;
    }

    @Override
    public void setHiringMode(final HiringMode hiringMode)
    {
        this.hiringMode = hiringMode;
        Network.getNetwork().sendToServer(new CourierHiringModeMessage(buildingView, hiringMode, getProducer().getRuntimeID()));
    }

    @Override
    public boolean canAssign(ICitizenDataView data)
    {
        for (final IBuildingView bView : buildingView.getColony().getBuildings())
        {
            final CourierAssignmentModuleView view = bView.getModuleViewMatching(CourierAssignmentModuleView.class, m -> !m.buildingView.getId().equals(buildingView.getId()));
            if (view != null && view.getAssignedCitizens().contains(data.getId()))
            {
                return false;
            }
        }

        return !data.isChild() && data.getJobView() != null && data.getJobView().getEntry() == ModJobs.delivery.get();
    }

    @Override
    public int getMaxInhabitants()
    {
        return this.buildingView.getBuildingLevel() * 2;
    }

    @NotNull
    @Override
    public BOWindow getWindow()
    {
        return new SpecialAssignmentModuleWindow(buildingView, Constants.MOD_ID + ":gui/layouthuts/layoutcourierassignment.xml");
    }

    @Override
    public boolean isFull()
    {
        return getAssignedCitizens().size() >= getMaxInhabitants();
    }

    @Override
    public JobEntry getJobEntry()
    {
        return ModJobs.delivery.get();
    }
}
