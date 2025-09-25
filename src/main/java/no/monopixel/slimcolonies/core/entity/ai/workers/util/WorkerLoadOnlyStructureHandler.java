package no.monopixel.slimcolonies.core.entity.ai.workers.util;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.util.PlacementSettings;
import no.monopixel.slimcolonies.api.util.LoadOnlyStructureHandler;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import no.monopixel.slimcolonies.core.colony.jobs.AbstractJobStructure;
import no.monopixel.slimcolonies.core.entity.ai.workers.AbstractEntityAIStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Load only structure handler just to get dimensions etc from structures, not for placement specific for worker usage.
 */
@SuppressWarnings("removal")
public final class WorkerLoadOnlyStructureHandler<J extends AbstractJobStructure<?, J>, B extends AbstractBuildingStructureBuilder> extends LoadOnlyStructureHandler
{
    /**
     * The structure AI handling this task.
     */
    private final AbstractEntityAIStructure<J, B> structureAI;

    /**
     * The minecolonies specific worker load only structure placer.
     *
     * @param world          the world.
     * @param pos            the pos it is placed at.
     * @param blueprint      the blueprint.
     * @param settings       the placement settings.
     * @param fancyPlacement if fancy or complete.
     */
    public WorkerLoadOnlyStructureHandler(
        final Level world, final BlockPos pos, final Blueprint blueprint, final PlacementSettings settings, final boolean fancyPlacement,
        final AbstractEntityAIStructure<J, B> entityAIStructure)
    {
        super(world, pos, blueprint, settings, fancyPlacement);
        this.structureAI = entityAIStructure;
    }

    @Override
    public BlockState getSolidBlockForPos(final BlockPos blockPos)
    {
        return structureAI.getSolidSubstitution(blockPos);
    }

    @Override
    public BlockState getSolidBlockForPos(final BlockPos worldPos, @Nullable final Function<BlockPos, BlockState> virtualBlocks)
    {
        return structureAI.getSolidSubstitution(worldPos);
    }
}
