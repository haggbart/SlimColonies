package no.monopixel.slimcolonies.core.client.render.worldevent;

import no.monopixel.slimcolonies.api.items.IBlockOverlayItem;

import java.util.List;

/**
 * Renders boxes for {@link IBlockOverlayItem}
 */
public class ItemOverlayBoxesRenderer
{
    /**
     * Renders overlay boxes into the client.
     *
     * @param ctx rendering context
     */
    static void render(final WorldEventContext ctx)
    {
        if (ctx.mainHandItem.getItem() instanceof final IBlockOverlayItem overlayItem)
        {
            final List<IBlockOverlayItem.OverlayBox> boxes = overlayItem.getOverlayBoxes(ctx.clientLevel, ctx.clientPlayer, ctx.mainHandItem);

            for (final IBlockOverlayItem.OverlayBox box : boxes)
            {
                ColonyWorldRenderMacros.renderLineBox(ctx.poseStack, ctx.bufferSource, box.bounds(), box.width(), box.color(), box.showThroughBlocks());
            }

            ColonyWorldRenderMacros.endRenderLineBox(ctx.bufferSource);
        }
    }
}
