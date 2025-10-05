package no.monopixel.slimcolonies.core.items;

import static no.monopixel.slimcolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class handling Bread Dough.
 */
public class ItemBreadDough extends AbstractItemSlimColonies
{
    /**
     * Sets the name, creative tab, and registers the Bread Dough item.
     *
     * @param properties the properties.
     */
    public ItemBreadDough(final Properties properties)
    {
        super("bread_dough", properties.stacksTo(STACKSIZE));
    }
}
