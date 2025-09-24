package no.monopixel.slimcolonies.core.colony.jobs;

import no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingModules;
import net.minecraft.resources.ResourceLocation;
import no.monopixel.slimcolonies.api.client.render.modeltype.ModModelTypes;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.core.entity.ai.workers.production.EntityAIWorkLumberjack;
import no.monopixel.slimcolonies.core.entity.ai.workers.util.Tree;
import no.monopixel.slimcolonies.core.util.AttributeModifierUtils;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static no.monopixel.slimcolonies.api.util.constant.CitizenConstants.SKILL_BONUS_ADD;
import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_TREE;

/**
 * The Lumberjack job class.
 */
public class JobLumberjack extends AbstractJobCrafter<EntityAIWorkLumberjack, JobLumberjack>
{
    /**
     * Walking speed bonus per level
     */
    public static final double BONUS_SPEED_PER_LEVEL = 0.003;

    /**
     * The tree this lumberjack is currently working on.
     */
    @Nullable
    private Tree tree;

    /**
     * Create a lumberjack job.
     *
     * @param entity the lumberjack.
     */
    public JobLumberjack(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();
        @NotNull final CompoundTag treeTag = new CompoundTag();

        if (tree != null)
        {
            tree.write(treeTag);
        }

        compound.put(TAG_TREE, treeTag);
        return compound;
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.LUMBERJACK_ID;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        if (compound.contains(TAG_TREE))
        {
            tree = Tree.read(compound.getCompound(TAG_TREE));
            if (!tree.isTree())
            {
                tree = null;
            }
        }
    }

    @Override
    public void onLevelUp()
    {
        if (getCitizen().getEntity().isPresent())
        {
            final AbstractEntityCitizen worker = getCitizen().getEntity().get();
            final AttributeModifier speedModifier = new AttributeModifier(SKILL_BONUS_ADD, (getCitizen().getCitizenSkillHandler().getLevel(getCitizen().getWorkBuilding().getModule(
              BuildingModules.FORESTER_WORK).getSecondarySkill()) / 2.0) * BONUS_SPEED_PER_LEVEL, AttributeModifier.Operation.ADDITION);
            AttributeModifierUtils.addModifier(worker, speedModifier, Attributes.MOVEMENT_SPEED);
        }
    }

    /**
     * Get the current tree the lumberjack is cutting.
     *
     * @return the tree.
     */
    @Nullable
    public Tree getTree()
    {
        return tree;
    }

    /**
     * Set the tree he is currently cutting.
     *
     * @param tree the tree.
     */
    public void setTree(@Nullable final Tree tree)
    {
        this.tree = tree;
    }

    @NotNull
    @Override
    public EntityAIWorkLumberjack generateAI()
    {
        return new EntityAIWorkLumberjack(this);
    }
}
