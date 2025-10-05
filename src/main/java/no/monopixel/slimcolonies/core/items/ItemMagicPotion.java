package no.monopixel.slimcolonies.core.items;

/**
 * Class describing the magic potion item.
 */
public class ItemMagicPotion extends AbstractItemSlimColonies
{
    /**
     * Sets the name, creative tab, and registers the magic potion item.
     *
     * @param properties the properties.
     */
    public ItemMagicPotion(String name, Properties properties)
    {
        super(name, properties.stacksTo(16));
    }
}
