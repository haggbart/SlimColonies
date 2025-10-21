package no.monopixel.slimcolonies.core.colony.requestsystem.resolvers;

import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.colony.requestsystem.location.ILocation;
import no.monopixel.slimcolonies.api.colony.requestsystem.manager.IRequestManager;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.IRequest;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.IDeliverable;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.IRequestable;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import no.monopixel.slimcolonies.api.colony.requestsystem.requester.IRequester;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;
import no.monopixel.slimcolonies.api.crafting.IRecipeStorage;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.modules.WorkerBuildingModule;
import no.monopixel.slimcolonies.core.colony.buildings.moduleviews.WorkerBuildingModuleView;
import no.monopixel.slimcolonies.core.colony.requestsystem.resolvers.core.AbstractCraftingRequestResolver;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static no.monopixel.slimcolonies.api.util.constant.RSConstants.CONST_CRAFTING_RESOLVER_PRIORITY;

/**
 * A crafting resolver which takes care of 3x3 crafts which are crafted by a crafter worker.
 */
public class PublicWorkerCraftingRequestResolver extends AbstractCraftingRequestResolver
{
    /**
     * Initializing constructor.
     *
     * @param location the location of the resolver.
     * @param token    its id.
     */
    public PublicWorkerCraftingRequestResolver(@NotNull final ILocation location, @NotNull final IToken<?> token, final JobEntry entry)
    {
        super(location, token, entry, true);
    }

    @Nullable
    @Override
    public List<IRequest<?>> getFollowupRequestForCompletion(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> completedRequest)
    {
        //Noop. The production resolver already took care of that.
        return null;
    }

    @Override
    public void onAssignedRequestBeingCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
    }

    @Override
    public void onAssignedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {

    }

    @Override
    public void onRequestedRequestComplete(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        /*
         * Nothing to be done.
         */
    }

    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        //NOOP
    }

    @NotNull
    @Override
    public MutableComponent getRequesterDisplayName(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        final IRequester requester = manager.getColony().getRequesterBuildingForPosition(getLocation().getInDimensionLocation());
        if (requester instanceof IBuildingView)
        {
            final WorkerBuildingModuleView moduleView = ((IBuildingView) requester).getModuleViewMatching(WorkerBuildingModuleView.class, m -> m.getJobEntry() == getJobEntry());
            if (moduleView != null)
            {
                return Component.translatable(moduleView.getJobEntry().getTranslationKey());
            }
        }
        if (requester instanceof IBuilding)
        {
            final WorkerBuildingModule module = ((IBuilding) requester).getModuleMatching(WorkerBuildingModule.class, m -> m.getJobEntry() == getJobEntry());
            return Component.translatable(module.getJobEntry().getTranslationKey());
        }
        return super.getRequesterDisplayName(manager, request);
    }

    @Override
    public int getPriority()
    {
        return CONST_CRAFTING_RESOLVER_PRIORITY;
    }

    @Override
    public boolean canBuildingCraftRecipe(@NotNull final AbstractBuilding building, final IRecipeStorage recipeStorage)
    {
        return recipeStorage != null;
    }

    @Override
    protected IRequestable createNewRequestableForStack(final ItemStack stack, final int count, final int minCount, final IToken<?> recipeStorage)
    {
        return new PublicCrafting(stack, count, minCount, recipeStorage);
    }
}
