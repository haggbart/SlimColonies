package no.monopixel.slimcolonies.core.client.render.worldevent;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.BlueprintHandler;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.colony.IColonyView;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.items.ModItems;
import no.monopixel.slimcolonies.api.util.BlockPosUtil;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuildingGuards;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static no.monopixel.slimcolonies.api.util.constant.Constants.STORAGE_STYLE;
import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_POS;

public class ColonyPatrolPointRenderer
{
    /**
     * Cached wayPointBlueprint.
     */
    private static BlueprintPreviewData partolPointTemplate;

    private static Future<Blueprint> pendingTemplate;

    /**
     * Renders the guard scepter objects into the world.
     *
     * @param ctx rendering context
     */
    static void render(final WorldEventContext ctx)
    {
        if (ctx.mainHandItem.getItem() != ModItems.scepterGuard || !ctx.mainHandItem.hasTag())
        {
            return;
        }

        final CompoundTag itemStackNbt = ctx.mainHandItem.getTag();
        final IColonyView colony = IColonyManager.getInstance().getColonyView(itemStackNbt.getInt(TAG_ID), ctx.clientLevel.dimension());

        if (colony == null)
        {
            return;
        }

        final IBuildingView guardTowerView = colony.getBuilding(BlockPosUtil.read(itemStackNbt, TAG_POS));
        if (guardTowerView == null)
        {
            return;
        }

        if (pendingTemplate == null && partolPointTemplate == null)
        {
            pendingTemplate = StructurePacks.getBlueprintFuture(STORAGE_STYLE, "infrastructure/misc/patrolpoint.blueprint");
            return;
        }
        else if (pendingTemplate != null && pendingTemplate.isDone())
        {
            try
            {
                final BlueprintPreviewData tempPreviewData = new BlueprintPreviewData();
                tempPreviewData.setBlueprint(pendingTemplate.get());
                tempPreviewData.setPos(BlockPos.ZERO);
                partolPointTemplate = tempPreviewData;
                pendingTemplate = null;
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
            return;
        }

        if (guardTowerView instanceof AbstractBuildingGuards.View guardTower)
        {
            BlueprintHandler.getInstance().drawAtListOfPositions(partolPointTemplate, guardTower.getPatrolTargets(), ctx.stageEvent);
        }
    }
}
