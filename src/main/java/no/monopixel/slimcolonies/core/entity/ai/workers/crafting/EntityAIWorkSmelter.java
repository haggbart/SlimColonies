package no.monopixel.slimcolonies.core.entity.ai.workers.crafting;

import com.google.common.reflect.TypeToken;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import no.monopixel.slimcolonies.api.colony.interactionhandling.ChatPriority;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.IRequestable;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.StackList;
import no.monopixel.slimcolonies.api.crafting.IRecipeStorage;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.AITarget;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.IAIState;
import no.monopixel.slimcolonies.api.items.ModTags;
import no.monopixel.slimcolonies.api.util.InventoryUtils;
import no.monopixel.slimcolonies.api.util.ItemStackUtils;
import no.monopixel.slimcolonies.api.util.SoundUtils;
import no.monopixel.slimcolonies.api.util.StatsUtil;
import no.monopixel.slimcolonies.api.util.constant.translation.RequestSystemTranslationConstants;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingModules;
import no.monopixel.slimcolonies.core.colony.buildings.modules.FurnaceUserModule;
import no.monopixel.slimcolonies.core.colony.buildings.modules.ItemListModule;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingSmeltery;
import no.monopixel.slimcolonies.core.colony.interactionhandling.StandardInteraction;
import no.monopixel.slimcolonies.core.colony.jobs.JobSmelter;
import no.monopixel.slimcolonies.core.colony.requestable.SmeltableOre;
import no.monopixel.slimcolonies.core.entity.ai.workers.AbstractEntityAIUsesFurnace;
import no.monopixel.slimcolonies.core.network.messages.client.LocalizedParticleEffectMessage;
import no.monopixel.slimcolonies.core.util.WorkerUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static no.monopixel.slimcolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static no.monopixel.slimcolonies.api.util.constant.Constants.*;
import static no.monopixel.slimcolonies.api.util.constant.StatisticsConstants.ITEMS_SMELTED_DETAIL;
import static no.monopixel.slimcolonies.api.util.constant.StatisticsConstants.ORES_BROKEN;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.FURNACE_USER_NO_ORE;

/**
 * Smelter AI class.
 */
public class EntityAIWorkSmelter extends AbstractEntityAIUsesFurnace<JobSmelter, BuildingSmeltery>
{
    /**
     * Base xp gain for the smelter.
     */
    private static final double BASE_XP_GAIN = 5;

    /**
     * Value to identify the list of filterable ores.
     */
    public static final String ORE_LIST = "ores";

    /**
     * Constructor for the Smelter. Defines the tasks the cook executes.
     *
     * @param job a cook job to use.
     */
    public EntityAIWorkSmelter(@NotNull final JobSmelter job)
    {
        super(job);
        super.registerTargets(new AITarget(BREAK_ORES, this::breakOres, TICKS_SECOND));
        worker.setCanPickUpLoot(true);
    }

    /**
     * Break down ores until they are finished.
     *
     * @return the next state to go to.
     */
    private IAIState breakOres()
    {
        final BuildingSmeltery.OreBreakingModule module = building.getModule(BuildingModules.SMELTER_OREBREAK);
        final IRecipeStorage currentRecipeStorage = module.getFirstFulfillableRecipe(ItemStackUtils::isEmpty, 1, false);

        if (currentRecipeStorage == null)
        {
            return IDLE;
        }

        final ItemStack inputItem = currentRecipeStorage.getCleanedInput().stream()
                                      .map(ItemStorage::getItemStack)
                                      .findFirst().orElse(ItemStack.EMPTY);

        if (inputItem.isEmpty())
        {
            return IDLE;
        }
        Component statsName = inputItem.getHoverName();
        int quantity = inputItem.getCount();

        WorkerUtil.faceBlock(building.getPosition(), worker);

        if (!module.fullFillRecipe(currentRecipeStorage))
        {
            return IDLE;
        }
        else
        {
            worker.decreaseSaturationForContinuousAction();
            StatsUtil.trackStatByName(building, ORES_BROKEN, statsName, quantity);
            worker.getCitizenExperienceHandler().addExperience(0.2);
        }

        Network.getNetwork()
          .sendToTrackingEntity(new LocalizedParticleEffectMessage(inputItem, building.getID()), worker);
        Network.getNetwork()
          .sendToTrackingEntity(new LocalizedParticleEffectMessage(inputItem, building.getID().below()), worker);

        worker.setItemInHand(InteractionHand.MAIN_HAND, inputItem);
        worker.swing(InteractionHand.MAIN_HAND);
        SoundUtils.playSoundAtCitizen(world, building.getID(), SoundEvents.LEASH_KNOT_BREAK);

        return getState();
    }

    @Override
    public Class<BuildingSmeltery> getExpectedBuildingClass()
    {
        return BuildingSmeltery.class;
    }

    /**
     * Gather bars from the furnace and double or triple them by chance.
     *
     * @param furnace the furnace to retrieve from.
     */
    @Override
    protected void extractFromFurnace(final FurnaceBlockEntity furnace)
    {
        StatsUtil.trackStatFromFurnace(building, ITEMS_SMELTED_DETAIL, furnace, RESULT_SLOT);

        InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandler(
          new InvWrapper(furnace), RESULT_SLOT,
          worker.getInventoryCitizen());

        worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        this.incrementActionsDoneAndDecSaturation();
    }

    @Override
    protected IRequestable getSmeltAbleClass()
    {
        return new SmeltableOre(STACKSIZE * building.getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces().size());
    }

    @Override
    protected IAIState checkForImportantJobs()
    {
        if (!ItemStackUtils.isEmpty(worker.getMainHandItem()))
        {
            worker.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }

        if (!worker.getInventoryCitizen().hasSpace())
        {
            return INVENTORY_FULL;
        }

        final ICraftingBuildingModule module = building.getFirstModuleOccurance(BuildingSmeltery.OreBreakingModule.class);

        final IRecipeStorage currentRecipeStorage = module.getFirstFulfillableRecipe(ItemStackUtils::isEmpty, 1, false);

        if (currentRecipeStorage == null)
        {
            return super.checkForImportantJobs();
        }

        return BREAK_ORES;
    }

    /**
     * Check if a stack is a smeltable ore.
     *
     * @param stack the stack to test.
     * @return true if so.
     */
    @Override
    protected boolean isSmeltable(final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack) || !ItemStackUtils.IS_SMELTABLE.and(itemStack -> IColonyManager.getInstance().getCompatibilityManager().isOre(stack)).test(stack))
        {
            return false;
        }
        if (stack.is(ModTags.breakable_ore))
        {
            return false;
        }
        return !building.getModuleMatching(ItemListModule.class, m -> m.getId().equals(ORE_LIST)).isItemInList(new ItemStorage(stack));
    }

    @Override
    public void requestSmeltable()
    {
        if (!building.hasWorkerOpenRequestsOfType(worker.getCitizenData().getId(), TypeToken.of(getSmeltAbleClass().getClass())) &&
              !building.hasWorkerOpenRequestsFiltered(worker.getCitizenData().getId(),
                req -> req.getShortDisplayString().getSiblings().contains(Component.translatable(RequestSystemTranslationConstants.REQUESTS_TYPE_SMELTABLE_ORE))))
        {
            final List<ItemStorage> allowedItems = building.getModuleMatching(ItemListModule.class, m -> m.getId().equals(ORE_LIST)).getList();
            if (allowedItems.isEmpty())
            {
                worker.getCitizenData().createRequestAsync(getSmeltAbleClass());
            }
            else
            {
                final List<ItemStack> requests = IColonyManager.getInstance().getCompatibilityManager().getSmeltableOres().stream()
                                                   .filter(storage -> !allowedItems.contains(storage))
                                                   .map(ItemStorage::getItemStack)
                                                   .collect(Collectors.toList());

                if (requests.isEmpty())
                {
                    if (worker.getCitizenData() != null)
                    {
                        worker.getCitizenData()
                          .triggerInteraction(new StandardInteraction(Component.translatable(FURNACE_USER_NO_ORE), ChatPriority.BLOCKING));
                    }
                }
                else
                {
                    worker.getCitizenData()
                      .createRequestAsync(new StackList(requests,
                        RequestSystemTranslationConstants.REQUESTS_TYPE_SMELTABLE_ORE,
                        STACKSIZE * building.getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces().size(),
                        1));
                }
            }
        }
    }
}
