package no.monopixel.slimcolonies.core.items;

import static no.monopixel.slimcolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class handling Cake Batter.
 */
public class ItemCakeBatter extends AbstractItemSlimColonies
{
    /**
     * Sets the name, creative tab, and registers the Cake Batter item.
     *
     * @param properties the properties.
     */
    public ItemCakeBatter(final Properties properties)
    {
        super("cake_batter", properties.stacksTo(STACKSIZE));
    }
}
