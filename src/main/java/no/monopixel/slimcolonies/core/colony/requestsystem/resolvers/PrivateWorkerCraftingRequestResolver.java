package no.monopixel.slimcolonies.core.colony.requestsystem.resolvers;

import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.colony.requestsystem.location.ILocation;
import no.monopixel.slimcolonies.api.colony.requestsystem.manager.IRequestManager;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.IRequest;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.IDeliverable;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.IRequestable;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.crafting.PrivateCrafting;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;
import no.monopixel.slimcolonies.api.crafting.IRecipeStorage;
import no.monopixel.slimcolonies.api.util.constant.TranslationConstants;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.colony.requestsystem.resolvers.core.AbstractCraftingRequestResolver;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static no.monopixel.slimcolonies.api.util.constant.RSConstants.CONST_CRAFTING_RESOLVER_PRIORITY;

/**
 * A crafting resolver which takes care of 2x2 crafts which are crafted by the requesting worker.
 */
public class PrivateWorkerCraftingRequestResolver extends AbstractCraftingRequestResolver
{
    public PrivateWorkerCraftingRequestResolver(@NotNull final ILocation location, @NotNull final IToken<?> token, @NotNull final JobEntry entry)
    {
        super(location, token, entry,false);
    }

    @Nullable
    @Override
    public List<IRequest<?>> getFollowupRequestForCompletion(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> completedRequest)
    {
        //No followup needed, crafting already completed at the requesting building / worker.
        return null;
    }

    @Override
    public void onAssignedRequestBeingCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        return;
    }

    @Override
    public void onAssignedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {

    }

    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {

    }

    @NotNull
    @Override
    public MutableComponent getRequesterDisplayName(@NotNull final IRequestManager manager, @NotNull IRequest<?> request)
    {
        if (request.hasParent())
        {
            request = manager.getRequestForToken(request.getParent());
        }
        else
        {
            return Component.translatable(TranslationConstants.COM_MINECOLONIES_PRIVATE_CRAFTING_RESOLVER_NAME);
        }

        if (request == null)
        {
            return Component.translatable(TranslationConstants.COM_MINECOLONIES_PRIVATE_CRAFTING_RESOLVER_NAME);
        }

        return request.getRequester().getRequesterDisplayName(manager, request)
                 .append(Component.literal(" ("))
                 .append(Component.translatable(TranslationConstants.COM_MINECOLONIES_PRIVATE_CRAFTING_RESOLVER_NAME))
                 .append(Component.literal(")"));
    }

    @Override
    public int getPriority()
    {
        return CONST_CRAFTING_RESOLVER_PRIORITY;
    }

    @Override
    public boolean canBuildingCraftRecipe(@NotNull final AbstractBuilding building, final IRecipeStorage recipe)
    {
        return recipe != null && (recipe.getIntermediate() == null || recipe.getIntermediate() == Blocks.AIR);
    }

    @Override
    protected IRequestable createNewRequestableForStack(final ItemStack stack, final int count, final int minCount, final IToken<?> recipeStorage)
    {
        return new PrivateCrafting(stack, count, minCount, recipeStorage);
    }
}
