package com.minecolonies.core.entity.ai.workers.production.herders;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.StatsUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingCowboy;
import com.minecolonies.core.colony.jobs.JobCowboy;
import com.minecolonies.core.util.citizenutils.CitizenItemUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.StatisticsConstants.MILKING_ATTEMPTS;
import static com.minecolonies.core.colony.buildings.workerbuildings.BuildingCowboy.MILKING_AMOUNT;
/**
 * The AI behind the {@link JobCowboy} for Breeding, Killing and Milking Cows.
 */
public class EntityAIWorkCowboy extends AbstractEntityAIHerder<JobCowboy, BuildingCowboy>
{
    /**
     * Bucket metadata.
     */
    public static final String RENDER_META_BUCKET = "bucket";
    public static final String RENDER_META_BOWL   = "bowl";

    /**
     * Herd cow icon
     */
    private final static VisibleCitizenStatus HERD_COW =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/cowboy.png"), "com.minecolonies.gui.visiblestatus.cowboy");

    /**
     * Min wait between failed milking attempts.
     */
    private static final int MILK_COOL_DOWN = 10;

    private int milkCoolDown;
    private int stewCoolDown;

    /**
     * Creates the abstract part of the AI. Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkCowboy(@NotNull final JobCowboy job)
    {
        super(job);
        super.registerTargets(
          new AITarget(COWBOY_MILK, this::milkCows, 1),
          new AITarget(COWBOY_STEW, this::milkMooshrooms, 1)
        );
    }

    @Override
    protected void updateRenderMetaData()
    {
        String renderMeta = getState() == IDLE ? "" : RENDER_META_WORKING;
        if (worker.getCitizenInventoryHandler().hasItemInInventory(Items.BUCKET) || worker.getCitizenInventoryHandler().hasItemInInventory(ModItems.large_empty_bottle))
        {
            renderMeta += RENDER_META_BUCKET;
        }
        if (worker.getCitizenInventoryHandler().hasItemInInventory(Items.BOWL))
        {
            renderMeta += RENDER_META_BOWL;
        }
        worker.setRenderMetadata(renderMeta);
    }

    @Override
    public Class<BuildingCowboy> getExpectedBuildingClass()
    {
        return BuildingCowboy.class;
    }

    @Override
    public IAIState decideWhatToDo()
    {
        final IAIState result = super.decideWhatToDo();

        if (milkCoolDown > 0)
        {
            --milkCoolDown;
        }
        else if (building != null && building.getFirstModuleOccurance(BuildingCowboy.HerdingModule.class).canTryToMilk() && result.equals(START_WORKING))
        {
            return COWBOY_MILK;
        }

        if (stewCoolDown > 0)
        {
            --stewCoolDown;
        }
        else if (building != null && building.getFirstModuleOccurance(BuildingCowboy.HerdingModule.class).canTryToStew() && result.equals(START_WORKING))
        {
            return COWBOY_STEW;
        }

        return result;
    }

    @Override
    public @NotNull List<ItemStorage> getExtraItemsNeeded()
    {
        final List<ItemStorage> list = super.getExtraItemsNeeded();
        if (building != null && building.getFirstModuleOccurance(BuildingCowboy.HerdingModule.class).canTryToMilk() &&
              !searchForAnimals(a -> a instanceof Cow && !(a instanceof MushroomCow)).isEmpty())
        {
            list.add(new ItemStorage(building.getMilkInputItem().copy(), building.getSetting(MILKING_AMOUNT).getValue()));
        }
        if (building != null && building.getFirstModuleOccurance(BuildingCowboy.HerdingModule.class).canTryToStew() &&
              !searchForAnimals(a -> a instanceof MushroomCow).isEmpty())
        {
            list.add(new ItemStorage(Items.BOWL));
        }
        return list;
    }

    /**
     * Makes the Cowboy "Milk" the cows (Honestly all he does is swap an empty bucket for a milk bucket, there's no actual "Milk" method in {@link Cow}
     *
     * @return The next {@link IAIState}
     */
    private IAIState milkCows()
    {
        worker.getCitizenData().setVisibleStatus(HERD_COW);

        if (!worker.getCitizenInventoryHandler().hasItemInInventory(building.getMilkInputItem().getItem()))
        {
            if (InventoryUtils.hasBuildingEnoughElseCount(building, new ItemStorage(building.getMilkInputItem()), 1) > 0
                && walkToBuilding())
            {
                checkAndTransferFromHut(building.getMilkInputItem());
            }
            else
            {
                milkCoolDown = MILK_COOL_DOWN;
                return DECIDE;
            }
        }

        final Animal animal = searchForAnimals(a -> (a instanceof Cow || a instanceof Goat) && !(a instanceof MushroomCow) && !a.isBaby()).stream()
                          .findFirst().orElse(null);

        if (animal == null)
        {
            milkCoolDown = MILK_COOL_DOWN;
            return DECIDE;
        }

        if (equipItem(InteractionHand.MAIN_HAND, Collections.singletonList(new ItemStorage(building.getMilkInputItem().getItem(), building.getMilkInputItem().getCount()))) && !walkingToAnimal(animal))
        {
            if (InventoryUtils.addItemStackToItemHandler(worker.getInventoryCitizen(), building.getMilkOutputItem()))
            {
                building.getFirstModuleOccurance(BuildingCowboy.HerdingModule.class).onMilked();
                CitizenItemUtils.setHeldItem(worker, InteractionHand.MAIN_HAND, getItemSlot(building.getMilkOutputItem().getItem()));
                InventoryUtils.tryRemoveStackFromItemHandler(worker.getInventoryCitizen(), building.getMilkInputItem());

                final SoundEvent sound = animal instanceof Goat goat ? goat.getMilkingSound() : SoundEvents.COW_MILK;
                worker.queueSound(sound, animal.blockPosition(), 10, 0, 0.9f, worker.getRandom().nextFloat());
            }

            incrementActionsDoneAndDecSaturation();
            StatsUtil.trackStat(building, MILKING_ATTEMPTS, 1);
            worker.getCitizenExperienceHandler().addExperience(1.0);
            return INVENTORY_FULL;
        }

        return getState();
    }

    /**
     * Makes the Cowboy "Milk" the mooshrooms
     *
     * @return The next {@link IAIState}
     */
    private IAIState milkMooshrooms()
    {
        worker.getCitizenData().setVisibleStatus(HERD_COW);

        if (!worker.getCitizenInventoryHandler().hasItemInInventory(Items.BOWL))
        {
            if (InventoryUtils.hasBuildingEnoughElseCount(building, new ItemStorage(new ItemStack(Items.BOWL, 1)), 1) > 0
                && walkToBuilding())
            {
                checkAndTransferFromHut(new ItemStack(Items.BOWL, 1));
            }
            else
            {
                stewCoolDown = MILK_COOL_DOWN;
                return DECIDE;
            }
        }

        final MushroomCow mooshroom = searchForAnimals(a -> a instanceof MushroomCow && !a.isBaby()).stream()
                                        .map(a -> (MushroomCow) a).findFirst().orElse(null);

        if (mooshroom == null)
        {
            stewCoolDown = MILK_COOL_DOWN;
            return DECIDE;
        }

        if (equipItem(InteractionHand.MAIN_HAND, Collections.singletonList(new ItemStorage(Items.BOWL))) && !walkingToAnimal(mooshroom))
        {
            final FakePlayer fakePlayer = FakePlayerFactory.getMinecraft((ServerLevel) worker.level);
            fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL));
            if (mooshroom.mobInteract(fakePlayer, InteractionHand.MAIN_HAND).equals(InteractionResult.CONSUME))
            {
                if (InventoryUtils.addItemStackToItemHandler(worker.getInventoryCitizen(), fakePlayer.getMainHandItem()))
                {
                    building.getFirstModuleOccurance(BuildingCowboy.HerdingModule.class).onStewed();
                    CitizenItemUtils.setHeldItem(worker, InteractionHand.MAIN_HAND, getItemSlot(fakePlayer.getMainHandItem().getItem()));
                    InventoryUtils.tryRemoveStackFromItemHandler(worker.getInventoryCitizen(), new ItemStack(Items.BOWL));
                    worker.queueSound(SoundEvents.MOOSHROOM_MILK, mooshroom.blockPosition(), 10, 0, 0.9f, worker.getRandom().nextFloat());
                }
                fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }

            incrementActionsDoneAndDecSaturation();
            StatsUtil.trackStat(building, MILKING_ATTEMPTS, 1);
            worker.getCitizenExperienceHandler().addExperience(1.0);
            return INVENTORY_FULL;
        }

        return getState();
    }

    @Override
    public double getButcheringAttackDamage()
    {
        return Math.max(1.0, getPrimarySkillLevel() / 10.0);
    }

    @Override
    protected IAIState breedAnimals()
    {
        worker.getCitizenData().setVisibleStatus(HERD_COW);
        return super.breedAnimals();
    }

    @Override
    protected IAIState butcherAnimals()
    {
        worker.getCitizenData().setVisibleStatus(HERD_COW);
        return super.butcherAnimals();
    }
}
